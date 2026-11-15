import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Independent style adapters around the retained Minecraft renderer and editor. */
public final class PatchAppearance implements Opcodes {
    private static final String ROOT = "dev/dsmp/economy/";
    private static final String THEME = ROOT + "appearance/RenderTheme", STYLE = ROOT + "appearance/TabAppearance";
    private static final String CONFIG = ROOT + "TrackerConfig", RENDER = ROOT + "ui/TrackerRenderer", INK = RENDER + "$Ink";
    private static final String SCREEN = ROOT + "ui/TrackerScreen", CLIENT = ROOT + "EconomyTrackerClient";
    private static final Handle LAMBDA = new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
    private static MethodInsnNode call(int op, String owner, String name, String desc) { return new MethodInsnNode(op, owner, name, desc, false); }
    private static MethodNode method(ClassNode node, String name) { return node.methods.stream().filter(m -> m.name.equals(name)).findFirst().orElseThrow(() -> new IllegalStateException("Missing " + name)); }
    private static AbstractInsnNode previous(AbstractInsnNode node) { do { node = node.getPrevious(); } while (node != null && node.getOpcode() < 0); return node; }
    private static int integer(AbstractInsnNode n) { if (n instanceof IntInsnNode i) return i.operand; if (n.getOpcode() >= ICONST_0 && n.getOpcode() <= ICONST_5) return n.getOpcode() - ICONST_0; throw new IllegalStateException("Expected constant"); }
    private static MethodNode add(ClassNode node, int access, String name, String desc) {
        if (node.methods.stream().anyMatch(m -> m.name.equals(name))) throw new IllegalStateException("Already patched " + name);
        var m = new MethodNode(ASM9, access, name, desc, null, null); node.methods.add(m); m.visitCode(); return m;
    }
    private static void client(ClassNode node) {
        int hooks = 0;
        for (var method : node.methods) for (var n : method.instructions.toArray())
            if (n instanceof TypeInsnNode t && t.getOpcode() == INSTANCEOF && t.desc.equals(SCREEN)) {
                method.instructions.set(n, call(INVOKESTATIC, THEME, "isEditor", "(Ljava/lang/Object;)Z")); hooks++;
            }
        if (hooks != 1) throw new IllegalStateException("Expected one HUD/editor gate");
    }
    private static void screen(ClassNode node) {
        var init = method(node, "method_25426"); int labels = 0, ends = 0;
        for (var n : init.instructions.toArray()) {
            if (n instanceof LdcInsnNode l && l.cst instanceof String name && List.of("Layout", "Trackers", "Widgets").contains(name)) {
                var width = previous(n); var y = previous(width); var x = previous(y);
                if (integer(width) != 64 || integer(y) != 28) throw new IllegalStateException("Unexpected navigation layout");
                int index = List.of("Layout", "Trackers", "Widgets").indexOf(name);
                init.instructions.set(width, new IntInsnNode(BIPUSH, index == 0 ? 50 : 56));
                init.instructions.set(x, new IntInsnNode(BIPUSH, new int[]{10,64,124}[index])); labels++;
            }
            if (n.getOpcode() == RETURN) {
                var hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0)); hook.add(call(INVOKEVIRTUAL, SCREEN, "addAppearanceButton", "()V"));
                init.instructions.insertBefore(n, hook); ends++;
            }
        }
        if (labels != 3 || ends != 1) throw new IllegalStateException("Unexpected settings navigation");
        for (var method : node.methods) for (var n : method.instructions.toArray()) {
            if (n instanceof IntInsnNode i && i.operand == 128) i.operand = 160;
            if (n instanceof LdcInsnNode l && Double.valueOf(128).equals(l.cst)) l.cst = 160d;
        }
        var m = add(node, ACC_PRIVATE, "addAppearanceButton", "()V");
        m.visitVarInsn(ALOAD, 0); m.visitIntInsn(SIPUSH, 184); m.visitIntInsn(BIPUSH, 28); m.visitIntInsn(BIPUSH, 62); m.visitLdcInsn("Style");
        m.visitVarInsn(ALOAD, 0); m.visitInvokeDynamicInsn("run", "(L" + SCREEN + ";)Ljava/lang/Runnable;", LAMBDA,
                Type.getMethodType("()V"), new Handle(H_INVOKEVIRTUAL, SCREEN, "openAppearance", "()V", false), Type.getMethodType("()V"));
        m.visitMethodInsn(INVOKEVIRTUAL, SCREEN, "button", "(IIILjava/lang/String;Ljava/lang/Runnable;)V", false); m.visitInsn(RETURN); m.visitMaxs(6,1); m.visitEnd();
        m = add(node, ACC_PRIVATE, "openAppearance", "()V");
        m.visitMethodInsn(INVOKESTATIC, "net/minecraft/class_310", "method_1551", "()Lnet/minecraft/class_310;", false);
        m.visitTypeInsn(NEW, ROOT + "ui/AppearanceScreen"); m.visitInsn(DUP); m.visitVarInsn(ALOAD, 0);
        m.visitFieldInsn(GETFIELD, SCREEN, "mod", "L" + CLIENT + ";"); m.visitVarInsn(ALOAD, 0);
        m.visitMethodInsn(INVOKESPECIAL, ROOT + "ui/AppearanceScreen", "<init>", "(L" + CLIENT + ";Lnet/minecraft/class_437;)V", false);
        m.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/class_310", "method_1507", "(Lnet/minecraft/class_437;)V", false);
        m.visitInsn(RETURN); m.visitMaxs(5, 1); m.visitEnd();
    }
    private static void renderer(ClassNode node) {
        for (var field : node.fields) if (field.name.equals("EDITOR_TOP")) field.value = 160;
        int frame = 0, scale = 0;
        var draw = method(node, "drawAll"); var begin = new InsnList(); begin.add(new VarInsnNode(ALOAD, 1)); begin.add(call(INVOKESTATIC, THEME, "begin", "(L" + CONFIG + ";)V")); draw.instructions.insert(begin);
        for (var n : draw.instructions.toArray()) {
            if (n instanceof MethodInsnNode c && c.owner.equals(ROOT + "core/ViewCycle") && c.name.equals("frame")) {
                draw.instructions.insert(n, call(INVOKESTATIC, THEME, "frame", "(L" + ROOT + "core/ViewCycle$Frame;)L" + ROOT + "core/ViewCycle$Frame;")); frame++;
            }
            if (n.getOpcode() == RETURN) draw.instructions.insertBefore(n, call(INVOKESTATIC, THEME, "end", "()V"));
        }
        draw.maxStack += 2;
        for (var bounds : node.methods) if (bounds.name.equals("bounds") && bounds.desc.contains(";IIIZ)")) {
            for (var n : bounds.instructions.toArray()) {
                if (n instanceof FieldInsnNode f && f.owner.equals(CONFIG) && f.name.equals("scale")) {
                    bounds.instructions.insertBefore(n, new VarInsnNode(ILOAD, 1));
                    bounds.instructions.set(n, call(INVOKESTATIC, THEME, "scale", "(L" + CONFIG + ";I)F")); scale++;
                }
                if (n instanceof IntInsnNode i && i.operand == 128) i.operand = 160;
            }
            bounds.maxStack += 1;
        }
        if (frame != 1 || scale != 1) throw new IllegalStateException("Expected one frame and geometry style hook: " + frame + "/" + scale);
        var box = method(node, "beginBox"); var hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 1)); hook.add(new VarInsnNode(ILOAD, 2));
        hook.add(call(INVOKESTATIC, THEME, "widget", "(L" + CONFIG + ";I)V")); box.instructions.insert(hook); box.maxStack += 2;
        var view = method(node, "drawView"); hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new VarInsnNode(ILOAD, 2));
        hook.add(call(INVOKESTATIC, THEME, "page", "(I)L" + STYLE + ";")); hook.add(new FieldInsnNode(PUTFIELD, INK, "appearance", "L" + STYLE + ";"));
        view.instructions.insert(hook); view.maxStack += 2;
    }
    private static void ink(ClassNode node) {
        if (node.fields.stream().anyMatch(f -> f.name.equals("appearance"))) throw new IllegalStateException("Already styled Ink");
        node.fields.add(new FieldNode(ACC_PRIVATE, "appearance", "L" + STYLE + ";", null, null));
        var constructor = method(node, "<init>"); int constructors = 0;
        for (var n : constructor.instructions.toArray()) if (n.getOpcode() == RETURN) {
            var hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0)); hook.add(call(INVOKESTATIC, THEME, "currentStyle", "()L" + STYLE + ";"));
            hook.add(new FieldInsnNode(PUTFIELD, INK, "appearance", "L" + STYLE + ";")); constructor.instructions.insertBefore(n, hook); constructors++;
        }
        if (constructors != 1) throw new IllegalStateException("Unexpected Ink constructor"); constructor.maxStack += 2;
        var fade = method(node, "fade"); var hook = new InsnList(); hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new FieldInsnNode(GETFIELD, INK, "appearance", "L" + STYLE + ";"));
        hook.add(new VarInsnNode(ILOAD, 1)); hook.add(call(INVOKESTATIC, THEME, "color", "(L" + STYLE + ";I)I")); hook.add(new VarInsnNode(ISTORE, 1));
        fade.instructions.insert(hook); fade.maxStack += 2;
        for (String name : new String[]{"rounded", "border"}) {
            var method = method(node, name); hook = new InsnList(); var next = new LabelNode(); var original = new LabelNode();
            hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new FieldInsnNode(GETFIELD, INK, "appearance", "L" + STYLE + ";")); hook.add(new VarInsnNode(ILOAD, 3)); hook.add(new VarInsnNode(ILOAD, 4));
            hook.add(call(INVOKESTATIC, THEME, name.equals("rounded") ? "suppressFill" : "suppressBorder", "(L" + STYLE + ";II)Z"));
            hook.add(new JumpInsnNode(IFEQ, next)); hook.add(new InsnNode(RETURN)); hook.add(next); hook.add(new FrameNode(F_SAME,0,null,0,null));
            hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new FieldInsnNode(GETFIELD, INK, "appearance", "L" + STYLE + ";"));
            hook.add(call(INVOKESTATIC, THEME, "square", "(L" + STYLE + ";)Z")); hook.add(new JumpInsnNode(IFEQ, original));
            hook.add(new VarInsnNode(ALOAD, 0)); hook.add(new VarInsnNode(ILOAD, 1)); hook.add(new VarInsnNode(ILOAD, 2));
            if (name.equals("rounded")) {
                hook.add(new VarInsnNode(ILOAD, 1)); hook.add(new VarInsnNode(ILOAD, 3)); hook.add(new InsnNode(IADD));
                hook.add(new VarInsnNode(ILOAD, 2)); hook.add(new VarInsnNode(ILOAD, 4)); hook.add(new InsnNode(IADD));
            } else { hook.add(new VarInsnNode(ILOAD,3)); hook.add(new VarInsnNode(ILOAD,4)); }
            hook.add(new VarInsnNode(ILOAD,5)); hook.add(call(INVOKEVIRTUAL, INK, name.equals("rounded") ? "fill" : "rectBorder", "(IIIII)V"));
            hook.add(new InsnNode(RETURN)); hook.add(original); hook.add(new FrameNode(F_SAME,0,null,0,null));
            method.instructions.insert(hook); method.maxStack += 4;
        }
        var m = add(node, ACC_PRIVATE, "rectBorder", "(IIIII)V");
        // Four 1-pixel rectangles, with the same alpha/color path as rounded borders.
        for (int edge = 0; edge < 4; edge++) {
            m.visitVarInsn(ALOAD,0);
            m.visitVarInsn(ILOAD,1); if (edge == 3) { m.visitVarInsn(ILOAD,3); m.visitInsn(IADD); m.visitInsn(ICONST_1); m.visitInsn(ISUB); }
            m.visitVarInsn(ILOAD,2); if (edge == 1) { m.visitVarInsn(ILOAD,4); m.visitInsn(IADD); m.visitInsn(ICONST_1); m.visitInsn(ISUB); }
            m.visitVarInsn(ILOAD,1); if (edge == 2) m.visitInsn(ICONST_1); else m.visitVarInsn(ILOAD,3); m.visitInsn(IADD);
            m.visitVarInsn(ILOAD,2); if (edge == 0) m.visitInsn(ICONST_1); else m.visitVarInsn(ILOAD,4); m.visitInsn(IADD);
            m.visitVarInsn(ILOAD,5); m.visitMethodInsn(INVOKEVIRTUAL,INK,"fill","(IIIII)V",false);
        }
        m.visitInsn(RETURN); m.visitMaxs(8,6); m.visitEnd();
    }
    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]), output = Path.of(args[1]);
        for (String name : new String[]{CLIENT, SCREEN, RENDER, INK}) {
            var node = new ClassNode(ASM9); new ClassReader(Files.readAllBytes(input.resolve(name + ".class"))).accept(node,0);
            if (name.equals(CLIENT)) client(node); else if (name.equals(SCREEN)) screen(node); else if (name.equals(RENDER)) renderer(node); else ink(node);
            var writer = new ClassWriter(0); node.accept(writer); Path target = output.resolve(name + ".class"); Files.createDirectories(target.getParent()); Files.write(target,writer.toByteArray());
        }
        System.out.println("Verified independent appearance hooks: editor navigation, geometry, per-view color, backgrounds, borders and preview gating");
    }
}
