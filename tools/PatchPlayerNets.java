import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.*;

/** Redirect just the two Player Nets reads; preserve all other renderer methods. */
public final class PatchPlayerNets {
    public static void main(String[] args) throws Exception {
        ClassReader reader = new ClassReader(Files.readAllBytes(Path.of(args[0])));
        ClassWriter writer = new ClassWriter(reader, 0);
        int[] replaced = {0};
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!name.equals("nets") || !descriptor.equals("(Ldev/dsmp/economy/ui/TrackerRenderer$Ink;Ldev/dsmp/economy/core/Ledger;)V")) return method;
                return new MethodVisitor(Opcodes.ASM9, method) {
                    @Override public void visitMethodInsn(int opcode, String owner, String methodName, String desc, boolean isInterface) {
                        if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("dev/dsmp/economy/core/Ledger")
                                && methodName.equals("playerNets") && desc.equals("()Ljava/util/List;")) {
                            replaced[0]++;
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, "dev/dsmp/economy/ui/PlayerNetRows", "forPanel",
                                    "(Ldev/dsmp/economy/core/Ledger;)Ljava/util/List;", false);
                        } else super.visitMethodInsn(opcode, owner, methodName, desc, isInterface);
                    }
                    @Override public void visitLdcInsn(Object value) {
                        super.visitLdcInsn("No recorded bets yet".equals(value) ? "No players up or down yet" : value);
                    }
                };
            }
        }, 0);
        if (replaced[0] != 2) throw new IllegalStateException("Expected two Player Nets reads, got " + replaced[0]);
        byte[] result = writer.toByteArray();
        int[] hooks = {0};
        new ClassReader(result).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String owner, String methodName, String desc, boolean isInterface) {
                        if (owner.equals("dev/dsmp/economy/ui/PlayerNetRows")) {
                            if (!name.equals("nets") || opcode != Opcodes.INVOKESTATIC) throw new IllegalStateException("Hook outside Player Nets");
                            hooks[0]++;
                        }
                    }
                };
            }
        }, 0);
        if (hooks[0] != 2) throw new IllegalStateException("Missing Player Nets hooks");
        Files.write(Path.of(args[1]), result);
        System.out.println("Verified two display-only Player Nets hooks");
    }
}
