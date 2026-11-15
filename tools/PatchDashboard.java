import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Open the dashboard from O and /economy; keep the drag editor as a sub-page. */
public final class PatchDashboard implements Opcodes {
    private static final String ROOT="dev/dsmp/economy/",CLIENT=ROOT+"EconomyTrackerClient",OLD=ROOT+"ui/TrackerScreen",DASH=ROOT+"ui/DashboardScreen";
    private static MethodInsnNode call(int op,String owner,String name,String desc){return new MethodInsnNode(op,owner,name,desc,false);}
    private static void client(ClassNode node){int allocations=0,constructors=0;for(var m:node.methods)for(var n:m.instructions.toArray()){
        if(n instanceof TypeInsnNode t&&t.getOpcode()==NEW&&t.desc.equals(OLD)){t.desc=DASH;allocations++;}
        if(n instanceof MethodInsnNode c&&c.owner.equals(OLD)&&c.name.equals("<init>")&&c.desc.equals("(L"+CLIENT+";)V")){c.owner=DASH;constructors++;}
    }if(allocations!=2||constructors!=2)throw new IllegalStateException("Expected O and command menu entry points");}
    private static void screen(ClassNode node){int backgrounds=0,closes=0;for(var m:node.methods){
        if(m.name.equals("method_25394")){
            var hook=new InsnList();hook.add(new VarInsnNode(ALOAD,1));hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,OLD,"field_22789","I"));hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,OLD,"field_22790","I"));hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,OLD,"mod","L"+CLIENT+";"));hook.add(new FieldInsnNode(GETFIELD,CLIENT,"config","L"+ROOT+"TrackerConfig;"));hook.add(call(INVOKESTATIC,ROOT+"ui/FestiveUi","background","(Lnet/minecraft/class_332;IIL"+ROOT+"TrackerConfig;)V"));m.instructions.insert(hook);m.maxStack+=4;
            for(var n:m.instructions.toArray())if(n instanceof LdcInsnNode l&&Integer.valueOf(-301396199).equals(l.cst)){l.cst=0x110B1018;backgrounds++;}
        }
        if(m.name.equals("method_25419"))for(var n:m.instructions.toArray())if(n instanceof MethodInsnNode c&&c.getOpcode()==INVOKESPECIAL&&c.owner.equals("net/minecraft/class_437")&&c.name.equals("method_25419")){
            m.instructions.insertBefore(n,new FieldInsnNode(GETFIELD,OLD,"mod","L"+CLIENT+";"));m.instructions.set(n,call(INVOKESTATIC,ROOT+"ui/DashboardNavigation","open","(L"+CLIENT+";)V"));closes++;
        }
    }if(backgrounds!=1||closes!=2)throw new IllegalStateException("Unexpected layout-editor background/return paths "+backgrounds+"/"+closes);}
    public static void main(String[] args)throws Exception{Path input=Path.of(args[0]),output=Path.of(args[1]);for(String name:new String[]{CLIENT,OLD}){var node=new ClassNode(ASM9);new ClassReader(Files.readAllBytes(input.resolve(name+".class"))).accept(node,0);if(name.equals(CLIENT))client(node);else screen(node);var writer=new ClassWriter(0);node.accept(writer);Path target=output.resolve(name+".class");Files.createDirectories(target.getParent());Files.write(target,writer.toByteArray());}System.out.println("Verified dashboard entry points, winter layout backdrop and return navigation");}
}
