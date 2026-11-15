import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Guarded auction adapters against the checksum-pinned Minecraft integration. */
public final class PatchAuction implements Opcodes {
    private static final String ROOT="dev/dsmp/economy/",CLIENT=ROOT+"EconomyTrackerClient",STORE=ROOT+"LocalStore",SERVICE=ROOT+"auction/AuctionService";
    private static MethodInsnNode call(int op,String owner,String name,String desc){return new MethodInsnNode(op,owner,name,desc,false);}
    private static AbstractInsnNode next(AbstractInsnNode n){do{n=n.getNext();}while(n!=null&&n.getOpcode()<0);return n;}
    private static void client(ClassNode node){
        String action=ROOT+"commands/AuctionAction";if(node.interfaces.contains(action))throw new IllegalStateException("Already patched auction");node.interfaces.add(action);
        int scopes=0,opens=0,ticks=0,fallbacks=0;
        for(var m:node.methods){
            for(var n:m.instructions.toArray()){
                if(n instanceof FieldInsnNode f&&f.getOpcode()==PUTFIELD&&f.owner.equals(CLIENT)&&f.name.equals("scope")){m.instructions.insert(n,call(INVOKESTATIC,SERVICE,"close","()V"));scopes++;}
                if(m.name.equals("openLedger")&&n.getOpcode()==RETURN){
                    var hook=new InsnList();hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,CLIENT,"scope","Ljava/lang/String;"));hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,CLIENT,"store","L"+STORE+";"));hook.add(call(INVOKEVIRTUAL,STORE,"auctionDirectory","()Ljava/nio/file/Path;"));hook.add(call(INVOKESTATIC,SERVICE,"open","(Ljava/lang/String;Ljava/nio/file/Path;)V"));m.instructions.insertBefore(n,hook);opens++;
                }
                if(n instanceof MethodInsnNode c&&c.owner.equals(ROOT+"core/PaymentDetector$Detection")&&c.name.equals("betChanged")&&next(n) instanceof JumpInsnNode j&&j.getOpcode()==IFNE){c.name="suppressesCustom";fallbacks++;}
            }
            if(m.name.equals("lambda$onInitializeClient$5")||m.name.equals("auctionTick")){
                var hook=new InsnList();hook.add(call(INVOKESTATIC,"java/lang/System","currentTimeMillis","()J"));hook.add(call(INVOKESTATIC,SERVICE,"tick","(J)V"));m.instructions.insert(hook);ticks++;
            }
            m.maxStack+=4;
        }
        if(scopes!=3||opens!=2||ticks!=1||fallbacks!=1)throw new IllegalStateException("Unexpected auction hooks "+scopes+"/"+opens+"/"+ticks+"/"+fallbacks);
        var m=new MethodNode(ASM9,ACC_PUBLIC,"auctionAction","(Ljava/lang/Runnable;)I",null,null);node.methods.add(m);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKEVIRTUAL,CLIENT,"runLocal","(Ljava/lang/Runnable;)I",false);m.visitInsn(IRETURN);m.visitMaxs(2,2);m.visitEnd();
    }
    private static void store(ClassNode node){
        if(node.methods.stream().anyMatch(m->m.name.equals("auctionDirectory")))throw new IllegalStateException("Auction directory already added");
        var m=new MethodNode(ASM9,ACC_PUBLIC,"auctionDirectory","()Ljava/nio/file/Path;",null,null);node.methods.add(m);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitFieldInsn(GETFIELD,STORE,"directory","Ljava/nio/file/Path;");m.visitLdcInsn("auctions");m.visitMethodInsn(INVOKEINTERFACE,"java/nio/file/Path","resolve","(Ljava/lang/String;)Ljava/nio/file/Path;",true);m.visitInsn(ARETURN);m.visitMaxs(2,1);m.visitEnd();
    }
    public static void main(String[] args)throws Exception{for(String name:new String[]{CLIENT,STORE}){var node=new ClassNode(ASM9);new ClassReader(Files.readAllBytes(Path.of(args[0]).resolve(name+".class"))).accept(node,0);if(name.equals(CLIENT))client(node);else store(node);var writer=new ClassWriter(0);node.accept(writer);Path target=Path.of(args[1]).resolve(name+".class");Files.createDirectories(target.getParent());Files.write(target,writer.toByteArray());}System.out.println("Verified auction scope, storage, tick, payment routing and local action guards");}
}
