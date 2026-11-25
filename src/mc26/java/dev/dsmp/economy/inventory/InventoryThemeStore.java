package dev.dsmp.economy.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Bounded local JSON and atomic replacement; no tracked economy file is opened here. */
public final class InventoryThemeStore {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    public InventoryThemeStore(Path file){this.file=file;}
    public InventoryOptions load()throws IOException {
        if(!Files.exists(file,LinkOption.NOFOLLOW_LINKS))return InventoryOptions.defaults();
        if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>16_384)throw new IOException("Invalid inventory theme settings file");
        try{var options=JSON.fromJson(Files.readString(file,StandardCharsets.UTF_8),InventoryOptions.class);if(options==null)throw new IllegalArgumentException("Empty theme settings");return options;}
        catch(RuntimeException ex){throw new IOException("Could not read inventory theme settings",ex);}
    }
    public void save(InventoryOptions options)throws IOException {
        if(options==null)throw new IllegalArgumentException("Missing inventory settings");
        if(Files.isSymbolicLink(file))throw new IOException("Inventory settings cannot be a symbolic link");
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temporary=Files.createTempFile(file.toAbsolutePath().getParent(),"inventory-themes-",".tmp");
        try{
            Files.writeString(temporary,JSON.toJson(options)+"\n",StandardCharsets.UTF_8);
            try{Files.move(temporary,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException ex){Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temporary);}
    }
}
