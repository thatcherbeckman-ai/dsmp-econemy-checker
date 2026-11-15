import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Anchor rendering to normal client ticks and remove owned entities at scope changes. */
public final class PatchAuctionHologram implements Opcodes {
    public static void main(String[] args)throws Exception{
        String root="dev/dsmp/economy/",holo=root+"ui/AuctionHologram",client=root+"EconomyTrackerClient";
        var node=new ClassNode(ASM9);new ClassReader(Files.readAllBytes(Path.of(args[0]).resolve(client+".class"))).accept(node,0);int ticks=0,closes=0;
        for(var m:node.methods){for(var n:m.instructions.toArray())if(n instanceof MethodInsnNode c&&c.owner.equals(root+"auction/AuctionService")){
            if(c.name.equals("tick")&&c.desc.equals("(J)V")){var hook=new InsnList();hook.add(new MethodInsnNode(INVOKESTATIC,"net/minecraft/class_310","method_1551","()Lnet/minecraft/class_310;",false));hook.add(new MethodInsnNode(INVOKESTATIC,holo,"tick","(Lnet/minecraft/class_310;)V",false));m.instructions.insert(n,hook);ticks++;}
            if(c.name.equals("close")&&c.desc.equals("()V")){m.instructions.insert(n,new MethodInsnNode(INVOKESTATIC,holo,"clear","()V",false));closes++;}
        }m.maxStack+=1;}
        if(ticks!=1||closes!=3)throw new IllegalStateException("Unexpected world hologram hooks "+ticks+"/"+closes);
        var writer=new ClassWriter(0);node.accept(writer);Path output=Path.of(args[1]).resolve(client+".class");Files.createDirectories(output.getParent());Files.write(output,writer.toByteArray());System.out.println("Verified auction world hologram tick and scope cleanup hooks");
    }
}
