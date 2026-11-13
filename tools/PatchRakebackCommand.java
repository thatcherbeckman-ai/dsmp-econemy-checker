import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.objectweb.asm.*;

/** Add one client-only command, preserving the original scope/error/save handling. */
public final class PatchRakebackCommand {
    private static final String CLIENT = "dev/dsmp/economy/EconomyTrackerClient";
    private static final String ACTION = "dev/dsmp/economy/commands/RakebackResetAction";
    private static final String COMMANDS = "dev/dsmp/economy/commands/RakebackCommands";
    private static final String LEDGER = "dev/dsmp/economy/core/Ledger";
    public static void main(String[] args) throws Exception {
        var reader = new ClassReader(Files.readAllBytes(Path.of(args[0])));
        var writer = new ClassWriter(reader, 0);
        int[] registrations = {0}, helps = {0};
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override public void visit(int version, int access, String name, String signature, String parent, String[] interfaces) {
                if (!name.equals(CLIENT) || Arrays.asList(interfaces).contains(ACTION)) throw new IllegalStateException("Unexpected client base");
                String[] updated = Arrays.copyOf(interfaces, interfaces.length + 1);
                updated[interfaces.length] = ACTION;
                super.visit(version, access, name, signature, parent, updated);
            }
            @Override public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name.equals("resetAllRakeback") || name.equals("resetAllRakebackData")) throw new IllegalStateException("Reset command already exists");
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, signature, exceptions)) {
                    @Override public void visitMethodInsn(int opcode, String owner, String method, String descriptor, boolean iface) {
                        if (owner.equals("com/mojang/brigadier/CommandDispatcher") && method.equals("register")
                                && descriptor.equals("(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;")) {
                            if ((access & Opcodes.ACC_STATIC) != 0) throw new IllegalStateException("Expected instance registration callback");
                            // Stack is [dispatcher, root]. Attach adds only a local subcommand.
                            super.visitInsn(Opcodes.DUP);
                            super.visitVarInsn(Opcodes.ALOAD, 0);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, COMMANDS, "attach",
                                    "(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;L" + ACTION + ";)V", false);
                            registrations[0]++;
                        }
                        super.visitMethodInsn(opcode, owner, method, descriptor, iface);
                    }
                    @Override public void visitLdcInsn(Object value) {
                        if (value instanceof String s && s.startsWith("Commands: /economy rakeback reset NAME |")) {
                            value = s.replace("reset NAME |", "reset NAME | resetall |"); helps[0]++;
                        }
                        super.visitLdcInsn(value);
                    }
                    @Override public void visitMaxs(int stack, int locals) { super.visitMaxs(stack + 2, locals); }
                };
            }
            @Override public void visitEnd() {
                // public int resetAllRakeback() { return runLocal(this::resetAllRakebackData); }
                var m = super.visitMethod(Opcodes.ACC_PUBLIC, "resetAllRakeback", "()I", null, null);
                m.visitCode(); m.visitVarInsn(Opcodes.ALOAD, 0); m.visitVarInsn(Opcodes.ALOAD, 0);
                m.visitInvokeDynamicInsn("run", "(L" + CLIENT + ";)Ljava/lang/Runnable;",
                        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
                        Type.getMethodType("()V"), new Handle(Opcodes.H_INVOKEVIRTUAL, CLIENT, "resetAllRakebackData", "()V", false), Type.getMethodType("()V"));
                m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLIENT, "runLocal", "(Ljava/lang/Runnable;)I", false);
                m.visitInsn(Opcodes.IRETURN); m.visitMaxs(2, 1); m.visitEnd();
                m = super.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC, "resetAllRakebackData", "()V", null, null);
                m.visitCode(); m.visitVarInsn(Opcodes.ALOAD, 0);
                m.visitFieldInsn(Opcodes.GETFIELD, CLIENT, "ledger", "L" + LEDGER + ";");
                m.visitMethodInsn(Opcodes.INVOKESTATIC, "dev/dsmp/economy/core/RakebackReset", "all", "(L" + LEDGER + ";)I", false);
                m.visitInsn(Opcodes.POP);
                m.visitVarInsn(Opcodes.ALOAD, 0); m.visitVarInsn(Opcodes.ALOAD, 0);
                m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLIENT, "saveLedger", "()Z", false);
                m.visitVarInsn(Opcodes.ALOAD, 0); m.visitFieldInsn(Opcodes.GETFIELD, CLIENT, "status", "Ljava/lang/String;");
                m.visitMethodInsn(Opcodes.INVOKESTATIC, COMMANDS, "resultMessage", "(ZLjava/lang/String;)Ljava/lang/String;", false);
                m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLIENT, "say", "(Ljava/lang/String;)V", false);
                m.visitInsn(Opcodes.RETURN); m.visitMaxs(3, 1); m.visitEnd();
                super.visitEnd();
            }
        }, 0);
        if (registrations[0] != 1 || helps[0] != 1) throw new IllegalStateException("Unexpected registration/help counts");
        Files.write(Path.of(args[1]), writer.toByteArray());
        System.out.println("Verified one rakeback resetall registration and guarded local save path");
    }
}
