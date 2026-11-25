package dev.dsmp.economy.menubackground;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Bounded local PNG imports and atomic cosmetic preference saves. */
public final class MenuBackgroundStore {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path folder;
    public MenuBackgroundStore(Path folder){this.folder=folder;}
    public MenuBackgroundOptions load()throws IOException{
        Path file=folder.resolve("menu-background.json");if(!Files.exists(file,LinkOption.NOFOLLOW_LINKS))return new MenuBackgroundOptions();
        if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>32_768)throw new IOException("Invalid menu background preferences file");
        try{var options=JSON.fromJson(Files.readString(file),MenuBackgroundOptions.class);if(options==null)throw new IllegalArgumentException("Empty preferences");options.validate();return options;}catch(RuntimeException ex){throw new IOException("Could not read menu background preferences",ex);}
    }
    public void save(MenuBackgroundOptions options)throws IOException{
        options.validate();Files.createDirectories(folder);Path file=folder.resolve("menu-background.json");if(Files.isSymbolicLink(file))throw new IOException("Background preferences cannot be a symbolic link");
        Path tmp=Files.createTempFile(folder,"menu-background-",".tmp");try{Files.writeString(tmp,JSON.toJson(options)+"\n");move(tmp,file);}finally{Files.deleteIfExists(tmp);}
    }
    public String importPng(Path source)throws IOException{
        if(Files.isSymbolicLink(source)||!Files.isRegularFile(source)||Files.size(source)>8*1024*1024)throw new IOException("Choose a PNG no larger than 8 MB");
        byte[] bytes=Files.readAllBytes(source);checkPng(bytes);
        String key;try{key=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))+".png";}catch(NoSuchAlgorithmException ex){throw new IOException(ex);}
        Path images=folder.resolve("menu-backgrounds");Files.createDirectories(images);Path target=images.resolve(key);if(Files.exists(target,LinkOption.NOFOLLOW_LINKS)){image(key);return key;}
        try(var files=Files.list(images)){if(files.count()>=24)throw new IOException("Background image limit reached (24 PNGs)");}
        Path tmp=Files.createTempFile(images,"import-",".tmp");try{Files.write(tmp,bytes);move(tmp,target);}finally{Files.deleteIfExists(tmp);}return key;
    }
    public byte[] image(String key)throws IOException{
        if(key==null||!key.matches("[0-9a-f]{64}\\.png"))throw new IOException("Invalid background image name");
        Path file=folder.resolve("menu-backgrounds").resolve(key);if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>8*1024*1024)throw new IOException("Background PNG is missing or invalid");byte[] bytes=Files.readAllBytes(file);checkPng(bytes);return bytes;
    }
    private static void checkPng(byte[] bytes)throws IOException{
        byte[] signature={(byte)137,80,78,71,13,10,26,10};if(bytes.length<24||!Arrays.equals(signature,Arrays.copyOf(bytes,8)))throw new IOException("The image must be a PNG");
        try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IOException("Invalid PNG");var reader=readers.next();try{reader.setInput(input);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1||h<1||w>4096||h>4096||(long)w*h>12_000_000)throw new IOException("PNG must fit within 4096 x 4096 and 12 million pixels");}finally{reader.dispose();}}
    }
    private static void move(Path from,Path to)throws IOException{try{Files.move(from,to,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(from,to,StandardCopyOption.REPLACE_EXISTING);}}
}
