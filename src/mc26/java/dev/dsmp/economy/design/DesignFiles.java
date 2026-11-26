package dev.dsmp.economy.design;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Stages every file before replacement and rolls back completed replacements on I/O failure. */
public final class DesignFiles {
    private DesignFiles(){}
    public static void write(Path file,String text)throws IOException{commit(Map.of(file,text));}
    public static void commit(Map<Path,String> content)throws IOException{
        var before=new LinkedHashMap<Path,byte[]>();var staged=new LinkedHashMap<Path,Path>();var changed=new ArrayList<Path>();
        try{
            for(var entry:content.entrySet()){
                Path file=entry.getKey();Files.createDirectories(file.toAbsolutePath().getParent());if(Files.isSymbolicLink(file)||Files.isSymbolicLink(file.toAbsolutePath().getParent()))throw new IOException("Settings path cannot be a symbolic link");
                boolean exists=Files.exists(file,LinkOption.NOFOLLOW_LINKS);if(exists&&(!Files.isRegularFile(file)||Files.size(file)>2_097_152))throw new IOException("Invalid settings file");before.put(file,exists?Files.readAllBytes(file):null);
                Path tmp=Files.createTempFile(file.toAbsolutePath().getParent(),"design-",".tmp");staged.put(file,tmp);Files.writeString(tmp,entry.getValue()+"\n");
            }
            for(var entry:staged.entrySet()){move(entry.getValue(),entry.getKey());changed.add(entry.getKey());}
        }catch(IOException ex){Collections.reverse(changed);for(Path file:changed)try{byte[] bytes=before.get(file);if(bytes==null)Files.deleteIfExists(file);else{Path tmp=Files.createTempFile(file.toAbsolutePath().getParent(),"rollback-",".tmp");try{Files.write(tmp,bytes);move(tmp,file);}finally{Files.deleteIfExists(tmp);}}}catch(IOException rollback){ex.addSuppressed(rollback);}throw ex;}
        finally{for(Path tmp:staged.values())Files.deleteIfExists(tmp);}
    }
    private static void move(Path source,Path target)throws IOException{try{Files.move(source,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(source,target,StandardCopyOption.REPLACE_EXISTING);}}
}
