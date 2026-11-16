import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;

/** Compiler-only accessibility changes matching the runtime access widener. Never shipped. */
public final class WidenCompileApi {
 public static void main(String[] args)throws Exception{
  Map<String,Set<String>> targets=new HashMap<>();for(String line:Files.readAllLines(Path.of(args[2])))if(line.startsWith("accessible method ")){var p=line.split(" ");targets.computeIfAbsent(p[2]+".class",k->new HashSet<>()).add(p[3]+p[4]);}
  int[] changed={0};try(var input=new ZipFile(args[0]);var out=new ZipOutputStream(Files.newOutputStream(Path.of(args[1])))){
   for(var e:Collections.list(input.entries())){byte[] data=input.getInputStream(e).readAllBytes();var methods=targets.get(e.getName());
    if(methods!=null){var writer=new ClassWriter(0);new ClassReader(data).accept(new ClassVisitor(Opcodes.ASM9,writer){@Override public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions){if(methods.contains(name+desc)){access=access&~(Opcodes.ACC_PRIVATE|Opcodes.ACC_PROTECTED)|Opcodes.ACC_PUBLIC;changed[0]++;}return super.visitMethod(access,name,desc,signature,exceptions);}},0);data=writer.toByteArray();}
    out.putNextEntry(new ZipEntry(e.getName()));out.write(data);out.closeEntry();
   }
  }if(changed[0]!=5)throw new IllegalStateException("Expected five retained display setters, found "+changed[0]);
 }
}
