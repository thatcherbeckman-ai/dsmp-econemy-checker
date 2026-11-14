import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Render the No Loan section using the existing loan chart layout and font. */
public final class PatchNoLoanLabels implements Opcodes {
    public static void main(String[] args) throws Exception {
        var node = new ClassNode(ASM9); new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node, 0);
        var method = node.methods.stream().filter(m -> m.name.equals("loans")).findFirst().orElseThrow();
        String service = "dev/dsmp/economy/loans/LoanService";
        int amounts = 0, names = 0, colors = 0;
        for (var insn : method.instructions.toArray()) {
            if (!(insn instanceof MethodInsnNode call)) continue;
            if (call.owner.equals("dev/dsmp/economy/core/Money") && call.name.equals("format") && call.desc.equals("(JZ)Ljava/lang/String;")) {
                var flag = previous(insn); if (flag.getOpcode() != ICONST_0) throw new IllegalStateException("Unexpected loan amount flag");
                method.instructions.remove(flag); call.owner = service; call.name = "rowAmount"; call.desc = "(J)Ljava/lang/String;"; amounts++;
            }
            if (call.owner.equals(node.name) && call.name.equals("color") && call.desc.equals("(J)I")) {
                call.owner = service; call.name = "rowAmountColor"; colors++;
            }
            if (call.owner.equals(node.name) && call.name.equals("text")) {
                var color = previous(insn);
                if (!(color instanceof LdcInsnNode c) || !Integer.valueOf(-1380105).equals(c.cst)) throw new IllegalStateException("Unexpected loan name color");
                var hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 4));
                hook.add(new MethodInsnNode(INVOKEVIRTUAL, "dev/dsmp/economy/core/Ledger$PlayerNet", "cents", "()J", false));
                hook.add(new MethodInsnNode(INVOKESTATIC, service, "rowNameColor", "(J)I", false));
                method.instructions.insertBefore(color, hook); method.instructions.remove(color); names++;
            }
        }
        if (amounts != 1 || names != 1 || colors != 1) throw new IllegalStateException("Unexpected loan display hooks " + amounts + "/" + names + "/" + colors);
        method.maxStack += 3;
        var writer = new ClassWriter(0); node.accept(writer); Path target = Path.of(args[1]); Files.createDirectories(target.getParent()); Files.write(target, writer.toByteArray());
        System.out.println("Verified No Loan section labels and row colors, isolated to Loans view");
    }
    private static AbstractInsnNode previous(AbstractInsnNode node) {
        do { node = node.getPrevious(); } while (node != null && node.getOpcode() < 0); return node;
    }
}
