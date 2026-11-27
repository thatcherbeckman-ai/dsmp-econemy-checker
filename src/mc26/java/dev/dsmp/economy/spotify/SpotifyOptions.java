package dev.dsmp.economy.spotify;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.*;

/** Public preferences only. OAuth credentials deliberately never enter this model. */
public record SpotifyOptions(String clientId, boolean enabled, boolean hideIdle, int position, int scale, int palette) {
    public static final String[] PALETTES={"Spotify green","Lavender","Ocean","Rose","Gold","Ice"};
    public static final int[] COLORS={0xFF1ED760,0xFFB69AFF,0xFF51C9E8,0xFFFF98B7,0xFFFFCE72,0xFFC4EFFF};
    public SpotifyOptions {
        clientId=clientId==null?"":clientId.trim();
        if(!clientId.isEmpty()&&!clientId.matches("[a-fA-F0-9]{32}"))throw new IllegalArgumentException("Enter the 32-character Spotify Client ID.");
        position=Math.clamp(position,0,8);scale=Math.clamp(scale,70,150);palette=Math.clamp(palette,0,PALETTES.length-1);
    }
    public static SpotifyOptions defaults(){return new SpotifyOptions("",false,true,6,100,0);}
    public static SpotifyOptions load(Path file)throws IOException{
        if(!Files.exists(file,LinkOption.NOFOLLOW_LINKS))return defaults();
        if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>8192)throw new IOException("Invalid Spotify preferences file.");
        try{var value=new Gson().fromJson(Files.readString(file),SpotifyOptions.class);if(value==null)throw new IllegalArgumentException();return value;}
        catch(RuntimeException ex){throw new IOException("Could not read Spotify preferences.",ex);}
    }
    public void save(Path file)throws IOException{
        if(Files.isSymbolicLink(file))throw new IOException("Spotify preferences cannot be a symbolic link.");
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path stage=Files.createTempFile(file.toAbsolutePath().getParent(),"spotify-",".tmp");
        try{Files.writeString(stage,new Gson().toJson(this)+"\n");try{Files.move(stage,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(stage,file,StandardCopyOption.REPLACE_EXISTING);}}
        finally{Files.deleteIfExists(stage);}
    }
}
