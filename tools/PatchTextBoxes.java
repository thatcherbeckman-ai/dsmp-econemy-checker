import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Adds custom-text rendering/editor access and the Minecraft-glass design. */
public final class PatchTextBoxes implements Opcodes {
    private static final String ROOT="dev/dsmp/economy/",SCREEN=ROOT+"ui/TrackerScreen",CLIENT=ROOT+"EconomyTrackerClient",INK=ROOT+"ui/TrackerRenderer$Ink",STYLE=ROOT+"appearance/TabAppearance";
    private static final Handle LAMBDA=new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false);
    private static MethodNode method(ClassNode n,String name){return n.methods.stream().filter(m->m.name.equals(name)).findFirst().orElseThrow();}
    private static MethodInsnNode call(int opcode,String owner,String name,String desc){return new MethodInsnNode(opcode,owner,name,desc,false);}
    private static void screen(ClassNode node){
        var init=method(node,"method_25426");int returns=0;
        for(var n:init.instructions.toArray())if(n.getOpcode()==RETURN){var list=new InsnList();list.add(new VarInsnNode(ALOAD,0));list.add(call(INVOKEVIRTUAL,SCREEN,"addTextBoxesButton","()V"));init.instructions.insertBefore(n,list);returns++;}
        if(returns!=1)throw new IllegalStateException("Unexpected editor init");
        var m=new MethodNode(ASM9,ACC_PRIVATE,"addTextBoxesButton","()V",null,null);node.methods.add(m);m.visitCode();var done=new Label();
        m.visitVarInsn(ALOAD,0);m.visitFieldInsn(GETFIELD,SCREEN,"tab","I");m.visitInsn(ICONST_2);m.visitJumpInsn(IF_ICMPNE,done);
        m.visitVarInsn(ALOAD,0);m.visitIntInsn(SIPUSH,206);m.visitIntInsn(BIPUSH,78);m.visitIntInsn(BIPUSH,94);m.visitLdcInsn("Text boxes");m.visitVarInsn(ALOAD,0);
        m.visitInvokeDynamicInsn("run","(L"+SCREEN+";)Ljava/lang/Runnable;",LAMBDA,Type.getMethodType("()V"),new Handle(H_INVOKEVIRTUAL,SCREEN,"openTextBoxes","()V",false),Type.getMethodType("()V"));
        m.visitMethodInsn(INVOKEVIRTUAL,SCREEN,"button","(IIILjava/lang/String;Ljava/lang/Runnable;)V",false);
        m.visitLabel(done);m.visitFrame(F_SAME,0,null,0,null);m.visitInsn(RETURN);m.visitMaxs(6,1);m.visitEnd();
        m=new MethodNode(ASM9,ACC_PRIVATE,"openTextBoxes","()V",null,null);node.methods.add(m);m.visitCode();
        m.visitMethodInsn(INVOKESTATIC,"net/minecraft/class_310","method_1551","()Lnet/minecraft/class_310;",false);
        m.visitTypeInsn(NEW,ROOT+"ui/TextBoxesScreen");m.visitInsn(DUP);m.visitVarInsn(ALOAD,0);m.visitFieldInsn(GETFIELD,SCREEN,"mod","L"+CLIENT+";");m.visitVarInsn(ALOAD,0);
        m.visitMethodInsn(INVOKESPECIAL,ROOT+"ui/TextBoxesScreen","<init>","(L"+CLIENT+";Lnet/minecraft/class_437;)V",false);
        m.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/class_310","method_1507","(Lnet/minecraft/class_437;)V",false);m.visitInsn(RETURN);m.visitMaxs(5,1);m.visitEnd();
    }
    private static void renderer(ClassNode node){
        var draw=method(node,"drawAll");int hooks=0;
        for(var n:draw.instructions.toArray())if(n.getOpcode()==RETURN){var list=new InsnList();list.add(new VarInsnNode(ALOAD,0));list.add(new VarInsnNode(ALOAD,1));list.add(new VarInsnNode(ILOAD,4));list.add(new VarInsnNode(ILOAD,5));list.add(new VarInsnNode(ILOAD,6));list.add(call(INVOKESTATIC,ROOT+"ui/TextHud","draw","(Lnet/minecraft/class_332;L"+ROOT+"TrackerConfig;IIZ)V"));draw.instructions.insertBefore(n,list);hooks++;}
        if(hooks!=1)throw new IllegalStateException("Unexpected HUD draw hooks");draw.maxStack+=5;
    }
    private static void ink(ClassNode node){
        for(String name:new String[]{"rounded","border"}){
            var m=method(node,name);var hook=new InsnList();var original=new LabelNode();
            hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,INK,"appearance","L"+STYLE+";"));hook.add(new VarInsnNode(ILOAD,3));hook.add(new VarInsnNode(ILOAD,4));
            hook.add(call(INVOKESTATIC,ROOT+"appearance/RenderTheme","minecraftGlass","(L"+STYLE+";II)Z"));hook.add(new JumpInsnNode(IFEQ,original));
            hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,INK,"graphics","Lnet/minecraft/class_332;"));
            for(int i=1;i<=4;i++)hook.add(new VarInsnNode(ILOAD,i));
            hook.add(new VarInsnNode(ALOAD,0));hook.add(new VarInsnNode(ILOAD,5));hook.add(call(INVOKEVIRTUAL,INK,"fade","(I)I"));
            hook.add(call(INVOKESTATIC,ROOT+"ui/GlassPane",name.equals("rounded")?"fill":"border","(Lnet/minecraft/class_332;IIIII)V"));hook.add(new InsnNode(RETURN));
            hook.add(original);hook.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(hook);m.maxStack=Math.max(m.maxStack+3,8);
        }
    }
    public static void main(String[] args)throws Exception{
        Path input=Path.of(args[0]),output=Path.of(args[1]);
        for(String name:new String[]{SCREEN,ROOT+"ui/TrackerRenderer",INK}){
            var node=new ClassNode(ASM9);new ClassReader(Files.readAllBytes(input.resolve(name+".class"))).accept(node,0);
            if(name.equals(SCREEN))screen(node);else if(name.equals(INK))ink(node);else renderer(node);
            var writer=new ClassWriter(0);node.accept(writer);Path path=output.resolve(name+".class");Files.createDirectories(path.getParent());Files.write(path,writer.toByteArray());
        }
        System.out.println("Verified custom text HUD, editor entry and Minecraft-glass adapters");
    }
}
