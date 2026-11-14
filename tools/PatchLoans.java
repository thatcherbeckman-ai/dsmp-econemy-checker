import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Narrow adapters around the retained Minecraft integration. */
public final class PatchLoans implements Opcodes {
    private static final String ROOT = "dev/dsmp/economy/";
    private static final String CLIENT = ROOT + "EconomyTrackerClient", CONFIG = ROOT + "TrackerConfig";
    private static final String STORE = ROOT + "LocalStore", SCREEN = ROOT + "ui/TrackerScreen", RENDER = ROOT + "ui/TrackerRenderer";
    private static final String INK = RENDER + "$Ink", LEDGER = ROOT + "core/Ledger";
    private static final String SERVICE = ROOT + "loans/LoanService", ACTION = ROOT + "commands/LoanAction";
    private static final Handle LAMBDA = new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
    private static MethodInsnNode call(int opcode, String owner, String name, String desc) { return new MethodInsnNode(opcode, owner, name, desc, false); }
    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(m -> m.name.equals(name)).findFirst().orElseThrow(() -> new IllegalStateException("Missing " + name));
    }
    private static MethodNode add(ClassNode node, int access, String name, String desc) {
        if (node.methods.stream().anyMatch(m -> m.name.equals(name))) throw new IllegalStateException("Already patched: " + name);
        var m = new MethodNode(ASM9, access, name, desc, null, null); node.methods.add(m); m.visitCode(); return m;
    }
    private static void end(MethodNode method, int stack, int locals) { method.visitMaxs(stack, locals); method.visitEnd(); }
    private static void config(MethodVisitor m) {
        m.visitVarInsn(ALOAD, 0); m.visitFieldInsn(GETFIELD, SCREEN, "mod", "L" + CLIENT + ";"); m.visitFieldInsn(GETFIELD, CLIENT, "config", "L" + CONFIG + ";");
    }
    private static void client(ClassNode node) {
        if (node.interfaces.contains(ACTION)) throw new IllegalStateException("Loans already added");
        node.interfaces.add(ACTION); int registrations = 0, scopes = 0, opens = 0;
        for (var m : List.copyOf(node.methods)) {
            for (var insn : m.instructions.toArray()) {
                if (insn instanceof MethodInsnNode c && c.owner.equals("com/mojang/brigadier/CommandDispatcher") && c.name.equals("register")) {
                    var hook = new InsnList(); hook.add(new InsnNode(DUP)); hook.add(new VarInsnNode(ALOAD, 0));
                    hook.add(call(INVOKESTATIC, ROOT + "commands/LoanCommands", "attach", "(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;L" + ACTION + ";)V"));
                    m.instructions.insertBefore(insn, hook); registrations++;
                }
                if (insn instanceof FieldInsnNode f && f.getOpcode() == PUTFIELD && f.owner.equals(CLIENT) && f.name.equals("scope")) {
                    m.instructions.insert(insn, call(INVOKESTATIC, SERVICE, "close", "()V")); scopes++;
                }
                if (m.name.equals("openLedger") && insn.getOpcode() == RETURN) {
                    var hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new FieldInsnNode(GETFIELD, CLIENT, "scope", "Ljava/lang/String;"));
                    hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new FieldInsnNode(GETFIELD, CLIENT, "store", "L" + STORE + ";"));
                    hook.add(call(INVOKEVIRTUAL, STORE, "loanDirectory", "()Ljava/nio/file/Path;"));
                    hook.add(call(INVOKESTATIC, SERVICE, "open", "(Ljava/lang/String;Ljava/nio/file/Path;)V"));
                    m.instructions.insertBefore(insn, hook); opens++;
                }
            }
            m.maxStack += 3;
        }
        if (registrations != 1 || scopes != 3 || opens != 2) throw new IllegalStateException("Unexpected client hooks " + registrations + "/" + scopes + "/" + opens);
        var m = add(node, ACC_PUBLIC, "loanCommand", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I");
        m.visitVarInsn(ALOAD, 0); m.visitVarInsn(ALOAD, 0); m.visitVarInsn(ALOAD, 1); m.visitVarInsn(ALOAD, 2); m.visitVarInsn(ALOAD, 3);
        m.visitInvokeDynamicInsn("run", "(L" + CLIENT + ";Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Runnable;", LAMBDA,
                Type.getMethodType("()V"), new Handle(H_INVOKEVIRTUAL, CLIENT, "loanCommandData", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false), Type.getMethodType("()V"));
        m.visitMethodInsn(INVOKEVIRTUAL, CLIENT, "runLocal", "(Ljava/lang/Runnable;)I", false); m.visitInsn(IRETURN); end(m, 5, 4);
        m = add(node, ACC_PRIVATE | ACC_SYNTHETIC, "loanCommandData", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
        m.visitVarInsn(ALOAD, 0); m.visitVarInsn(ALOAD, 1); m.visitVarInsn(ALOAD, 2); m.visitVarInsn(ALOAD, 3);
        m.visitMethodInsn(INVOKESTATIC, SERVICE, "execute", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
        m.visitMethodInsn(INVOKEVIRTUAL, CLIENT, "say", "(Ljava/lang/String;)V", false); m.visitInsn(RETURN); end(m, 4, 4);
    }
    private static void store(ClassNode node) {
        var m = add(node, ACC_PUBLIC, "loanDirectory", "()Ljava/nio/file/Path;");
        m.visitVarInsn(ALOAD, 0); m.visitFieldInsn(GETFIELD, STORE, "directory", "Ljava/nio/file/Path;"); m.visitLdcInsn("loans");
        m.visitMethodInsn(INVOKEINTERFACE, "java/nio/file/Path", "resolve", "(Ljava/lang/String;)Ljava/nio/file/Path;", true);
        m.visitInsn(ARETURN); end(m, 2, 1);
    }
    private static void renderer(ClassNode node) {
        MethodNode original = method(node, "nets");
        var loans = new MethodNode(ASM9, original.access, "loans", original.desc, null, null); original.accept(loans);
        int reads = 0, headings = 0, captions = 0, money = 0;
        for (var insn : loans.instructions.toArray()) {
            if (insn instanceof MethodInsnNode c && c.owner.equals(ROOT + "ui/PlayerNetRows") && c.name.equals("forPanel")) { c.owner = SERVICE; c.name = "rows"; reads++; }
            if (insn instanceof LdcInsnNode c && "PLAYER NETS".equals(c.cst)) { c.cst = "LOANS"; headings++; }
            if (insn instanceof LdcInsnNode c && c.cst instanceof String text && text.startsWith("+ means they're up")) {
                loans.instructions.set(insn, call(INVOKESTATIC, SERVICE, "caption", "()Ljava/lang/String;")); captions++;
            }
            if (insn instanceof LdcInsnNode c && "No players up or down yet".equals(c.cst)) c.cst = "Add loans with /economy loan set";
            if (insn instanceof MethodInsnNode c && c.owner.equals(ROOT + "core/Money") && c.name.equals("format")) {
                var prev = previous(insn); if (prev.getOpcode() != ICONST_1) throw new IllegalStateException("Unexpected amount format");
                loans.instructions.set(prev, new InsnNode(ICONST_0)); money++;
            }
        }
        if (reads != 2 || headings != 1 || captions != 1 || money != 1) throw new IllegalStateException("Unexpected loans template");
        node.methods.add(loans);
        var view = method(node, "drawView"); var hook = new InsnList(); var old = new LabelNode();
        hook.add(new VarInsnNode(ILOAD, 2)); hook.add(new InsnNode(ICONST_3)); hook.add(new JumpInsnNode(IF_ICMPNE, old));
        hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new VarInsnNode(ALOAD, 1)); hook.add(call(INVOKESTATIC, RENDER, "loans", original.desc)); hook.add(new InsnNode(RETURN));
        hook.add(old); hook.add(new FrameNode(F_SAME, 0, null, 0, null)); view.instructions.insert(hook); view.maxStack += 2;
    }
    private static AbstractInsnNode previous(AbstractInsnNode insn) {
        do { insn = insn.getPrevious(); } while (insn != null && insn.getOpcode() < 0); return insn;
    }
    private static int integer(AbstractInsnNode insn) {
        if (insn instanceof IntInsnNode i) return i.operand;
        if (insn.getOpcode() >= ICONST_0 && insn.getOpcode() <= ICONST_5) return insn.getOpcode() - ICONST_0;
        throw new IllegalStateException("Expected integer constant");
    }
    private static void screen(ClassNode node) {
        var init = method(node, "method_25426"); int toggles = 0, returns = 0;
        for (var insn : init.instructions.toArray()) {
            if (insn instanceof MethodInsnNode c && c.owner.equals(SCREEN) && c.name.equals("toggle")) {
                var panel = previous(insn); int index = integer(panel); var name = (LdcInsnNode) previous(panel);
                var width = previous(name); var y = previous(width); var x = previous(y);
                if (integer(width) != 94 || integer(y) != 54 || index < 0 || index > 2) throw new IllegalStateException("Unexpected toggle layout");
                init.instructions.set(width, new IntInsnNode(BIPUSH, 70)); init.instructions.set(x, new IntInsnNode(SIPUSH, 10 + 74 * index));
                if (index == 0) name.cst = "Nets"; if (index == 1) name.cst = "Bets"; toggles++;
            }
            if (insn.getOpcode() == RETURN) {
                var hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0)); hook.add(call(INVOKEVIRTUAL, SCREEN, "addLoanToggle", "()V")); init.instructions.insertBefore(insn, hook); returns++;
            }
        }
        if (toggles != 3 || returns != 1) throw new IllegalStateException("Unexpected editor layout");
        int warnings = 0;
        for (var m : node.methods) for (var insn : m.instructions.toArray()) {
            if (insn instanceof MethodInsnNode c && c.owner.equals("java/util/stream/IntStream") && c.name.equals("noneMatch")) {
                var hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new FieldInsnNode(GETFIELD, SCREEN, "mod", "L" + CLIENT + ";"));
                hook.add(new FieldInsnNode(GETFIELD, CLIENT, "config", "L" + CONFIG + ";"));
                hook.add(call(INVOKESTATIC, ROOT + "loans/LoanUi", "viewsOff", "(ZL" + CONFIG + ";)Z"));
                m.instructions.insert(insn, hook); m.maxStack += 2; warnings++;
            }
        }
        if (warnings != 1) throw new IllegalStateException("Missing disabled-views hint");
        var m = add(node, ACC_PRIVATE, "addLoanToggle", "()V"); var done = new Label();
        m.visitVarInsn(ALOAD, 0); m.visitFieldInsn(GETFIELD, SCREEN, "tab", "I"); m.visitInsn(ICONST_1); m.visitJumpInsn(IF_ICMPNE, done);
        m.visitVarInsn(ALOAD, 0); m.visitIntInsn(SIPUSH, 232); m.visitIntInsn(BIPUSH, 54); m.visitIntInsn(BIPUSH, 70);
        config(m); m.visitFieldInsn(GETFIELD, CONFIG, "loansVisible", "Z"); m.visitMethodInsn(INVOKESTATIC, SERVICE, "toggleLabel", "(Z)Ljava/lang/String;", false);
        m.visitVarInsn(ALOAD, 0); m.visitInvokeDynamicInsn("run", "(L" + SCREEN + ";)Ljava/lang/Runnable;", LAMBDA,
                Type.getMethodType("()V"), new Handle(H_INVOKEVIRTUAL, SCREEN, "toggleLoanVisibility", "()V", false), Type.getMethodType("()V"));
        m.visitMethodInsn(INVOKEVIRTUAL, SCREEN, "button", "(IIILjava/lang/String;Ljava/lang/Runnable;)V", false);
        m.visitLabel(done); m.visitFrame(F_SAME, 0, null, 0, null); m.visitInsn(RETURN); end(m, 7, 1);
        m = add(node, ACC_PRIVATE | ACC_SYNTHETIC, "toggleLoanVisibility", "()V"); config(m); m.visitInsn(DUP);
        m.visitFieldInsn(GETFIELD, CONFIG, "loansVisible", "Z"); m.visitInsn(ICONST_1); m.visitInsn(IXOR); m.visitFieldInsn(PUTFIELD, CONFIG, "loansVisible", "Z");
        m.visitVarInsn(ALOAD, 0); m.visitMethodInsn(INVOKEVIRTUAL, SCREEN, "method_41843", "()V", false); m.visitInsn(RETURN); end(m, 3, 1);
    }
    public static void main(String[] args) throws Exception {
        Path original = Path.of(args[0]), output = Path.of(args[1]);
        for (String name : new String[]{CLIENT, STORE, RENDER, SCREEN}) {
            var node = new ClassNode(ASM9); new ClassReader(Files.readAllBytes(original.resolve(name + ".class"))).accept(node, 0);
            if (name.equals(CLIENT)) client(node); else if (name.equals(STORE)) store(node); else if (name.equals(RENDER)) renderer(node); else screen(node);
            var writer = new ClassWriter(0); node.accept(writer); Path path = output.resolve(name + ".class"); Files.createDirectories(path.getParent()); Files.write(path, writer.toByteArray());
        }
        System.out.println("Verified loan adapters: scoped commands/storage, fourth view, caption/rows and editor toggle");
    }
}
