package dev.dsmp.economy.privacy;
import dev.dsmp.economy.design.DesignFiles;
import dev.dsmp.economy.design.DesignLook;
import java.io.*;
import java.nio.file.*;
public final class TrackingStore {
    private final Path file;public TrackingStore(Path file){this.file=file;}
    public TrackingOptions load()throws IOException{if(!Files.exists(file,LinkOption.NOFOLLOW_LINKS))return new TrackingOptions();if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>4096)throw new IOException("Invalid tracking controls file");try{var value=DesignLook.JSON.fromJson(Files.readString(file),TrackingOptions.class);if(value==null)throw new IllegalArgumentException("Empty controls");return value;}catch(RuntimeException ex){throw new IOException("Could not read tracking controls; original retained",ex);}}
    public void save(TrackingOptions options)throws IOException{if(options==null)throw new IllegalArgumentException("Missing tracking controls");DesignFiles.write(file,DesignLook.JSON.toJson(options));}
}
