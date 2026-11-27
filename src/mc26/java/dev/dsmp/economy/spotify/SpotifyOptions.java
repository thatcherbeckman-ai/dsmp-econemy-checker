package dev.dsmp.economy.spotify;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.*;

/** Public preferences only. OAuth credentials deliberately never enter this model. */
public record SpotifyOptions(String clientId, boolean enabled, boolean hideIdle, int position, int scale, int palette, SpotifySource source, boolean freePosition, double x, double y) {
    public static final String[] PALETTES={"Spotify green","Lavender","Ocean","Rose","Gold","Ice"};
    public static final int[] COLORS={0xFF1ED760,0xFFB69AFF,0xFF51C9E8,0xFFFF98B7,0xFFFFCE72,0xFFC4EFFF};
    public SpotifyOptions {
        source=source==null?SpotifySource.WEB_API:source;
        clientId=clientId==null?"":clientId.trim();
        if(!clientId.isEmpty()&&!clientId.matches("[a-fA-F0-9]{32}"))throw new IllegalArgumentException("Enter the 32-character Spotify Client ID.");
        position=Math.clamp(position,0,8);scale=Math.clamp(scale,25,150);palette=Math.clamp(palette,0,PALETTES.length-1);
        x=Double.isFinite(x)?Math.clamp(x,0,1):0;y=Double.isFinite(y)?Math.clamp(y,0,1):0;
    }
    public SpotifyOptions(String clientId,boolean enabled,boolean hideIdle,int position,int scale,int palette,SpotifySource source){this(clientId,enabled,hideIdle,position,scale,palette,source,false,0,0);}
    public SpotifyOptions(String clientId,boolean enabled,boolean hideIdle,int position,int scale,int palette){this(clientId,enabled,hideIdle,position,scale,palette,SpotifySource.WEB_API);}
    public static SpotifyOptions defaults(){return new SpotifyOptions("",false,true,6,100,0,SpotifySource.DESKTOP);}
    public SpotifyOptions appearance(boolean enabled,boolean hideIdle,int scale,int palette){return new SpotifyOptions(clientId,enabled,hideIdle,position,scale,palette,source,freePosition,x,y);}
    public SpotifyOptions size(int value){return appearance(enabled,hideIdle,value,palette);}
    public SpotifyOptions enabled(boolean value){return appearance(value,hideIdle,scale,palette);}
    public SpotifyOptions source(SpotifySource value){return new SpotifyOptions(clientId,enabled,hideIdle,position,scale,palette,value,freePosition,x,y);}
    public SpotifyOptions clientId(String value){return new SpotifyOptions(value,enabled,hideIdle,position,scale,palette,source,freePosition,x,y);}
    public SpotifyOptions anchor(int value){return new SpotifyOptions(clientId,enabled,hideIdle,value,scale,palette,source,false,x,y);}
    public SpotifyOptions at(double x,double y){return new SpotifyOptions(clientId,enabled,hideIdle,position,scale,palette,source,true,x,y);}
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
