package dev.dsmp.economy.design;
import java.nio.file.*;
import java.io.*;
public final class AccessStore {
    private final Path file;public AccessStore(Path file){this.file=file;}
    public AccessOptions load()throws IOException{if(!Files.exists(file,LinkOption.NOFOLLOW_LINKS))return new AccessOptions();if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>4096)throw new IOException("Invalid accessibility settings");try{var value=DesignLook.JSON.fromJson(Files.readString(file),AccessOptions.class);if(value==null)throw new IllegalArgumentException();value.validate();return value;}catch(RuntimeException ex){throw new IOException("Accessibility settings unreadable; original retained",ex);}}
    public void save(AccessOptions options)throws IOException{options.validate();DesignFiles.write(file,DesignLook.JSON.toJson(options));}
}
