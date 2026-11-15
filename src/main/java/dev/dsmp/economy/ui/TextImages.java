package dev.dsmp.economy.ui;

import dev.dsmp.economy.text.TextImageStore;
import net.minecraft.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;
import java.io.*;

/** Built-in icons and bounded, locally imported PNG textures; no remote image requests. */
public final class TextImages {
    public static final List<String> BUILT_INS=List.of("cards","roulette","dice","joker","gem","arrows","coin","heart");
    private record Texture(class_2960 id,int width,int height){}
    private static final Map<String,Texture> CACHE=new LinkedHashMap<>(16,.75f,true);
    private static final Set<String> FAILED=new HashSet<>();
    private TextImages(){}
    public static TextImageStore store(){return new TextImageStore(FabricLoader.getInstance().getConfigDir().resolve("economy-tracker/text-images"));}
    public static List<String> choices(){var out=new ArrayList<String>();out.add("");BUILT_INS.forEach(name->out.add("builtin:"+name));try{out.addAll(store().list());}catch(IOException ignored){}return List.copyOf(out);}
    public static String label(String key){if(key==null||key.isEmpty())return "No image";if(key.startsWith("builtin:")){String name=key.substring(8);return Character.toUpperCase(name.charAt(0))+name.substring(1);}return key.replaceFirst("^file:","").replaceFirst("^[a-f0-9]{24}-","");}
    public static String importPng(Path path)throws IOException{String key=store().importPng(path);refresh();return key;}
    public static void refresh(){var manager=class_310.method_1551().method_1531();for(var texture:CACHE.values())manager.method_4615(texture.id);CACHE.clear();FAILED.clear();}
    public static boolean draw(class_332 g,String key,int x,int y,int size,int color){
        if(key==null||key.isEmpty())return true;
        Texture texture;
        if(key.startsWith("builtin:")&&BUILT_INS.contains(key.substring(8)))texture=new Texture(class_2960.method_60655("economy_tracker","textures/text_icons/"+key.substring(8)+".png"),64,64);
        else {
            texture=CACHE.get(key);if(texture==null&&!FAILED.contains(key)){
                try{var png=store().load(key);var image=class_1011.method_4309(new ByteArrayInputStream(png.bytes()));class_1043 nativeTexture=null;
                    try{nativeTexture=new class_1043(()->"Economy text image",image);var id=class_2960.method_60655("economy_tracker","text_image_"+java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8))));class_310.method_1551().method_1531().method_4616(id,nativeTexture);texture=new Texture(id,png.width(),png.height());CACHE.put(key,texture);
                        while(CACHE.size()>128){String oldest=CACHE.keySet().iterator().next();var removed=CACHE.remove(oldest);class_310.method_1551().method_1531().method_4615(removed.id);}
                    }catch(Exception ex){if(nativeTexture!=null)nativeTexture.close();else image.close();throw ex;}
                }catch(Exception ex){if(FAILED.size()<1024)FAILED.add(key);}
            }
        }
        if(texture==null){g.method_25294(x,y,x+size,y+1,0xFFAA6677);g.method_25294(x,y+size-1,x+size,y+size,0xFFAA6677);g.method_25294(x,y,x+1,y+size,0xFFAA6677);g.method_25294(x+size-1,y,x+size,y+size,0xFFAA6677);return false;}
        float fit=size/(float)Math.max(texture.width,texture.height);int w=Math.max(1,Math.round(texture.width*fit)),h=Math.max(1,Math.round(texture.height*fit));
        g.method_25293(class_10799.field_56883,texture.id,x+(size-w)/2,y+(size-h)/2,0,0,w,h,texture.width,texture.height,texture.width,texture.height,color);return true;
    }
}
