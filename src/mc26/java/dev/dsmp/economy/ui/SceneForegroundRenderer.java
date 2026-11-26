package dev.dsmp.economy.ui;

import dev.dsmp.economy.menubackground.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/** A bounded foreground cache; the original illustrated collection remains the backdrop. */
final class SceneForegroundRenderer {
    private record Key(MenuScene scene,int w,int h,int size,int x,int ground,int framing,CharacterComposition.Platform platform,boolean shadow,boolean light,int direction,int strength,int softness){}
    private static final Map<Key,Identifier> CACHE=new LinkedHashMap<>(8,.75f,true);
    private static final Set<Key> FAILED=new HashSet<>();private static int serial;
    private SceneForegroundRenderer(){}
    static void draw(GuiGraphicsExtractor g,int x,int y,int w,int h,MenuBackgroundOptions options){
        if(w<24||h<40)return;var c=options.character;var key=new Key(options.scene,w,h,c.size,c.x,c.ground,c.platformSize,c.platform,c.shadow,c.matchLighting,c.lightDirection,c.lightStrength,c.shadowSoftness);var id=CACHE.get(key);
        if(id==null&&!FAILED.contains(key))try{
            var p=BlockScenery.foreground(options.scene);var textures=new HashMap<String,BufferedImage>();
            for(var name:new String[]{p.top(),p.side(),p.rock()})if(!textures.containsKey(name))try(var input=Minecraft.getInstance().getResourceManager().open(Identifier.fromNamespaceAndPath("minecraft","textures/block/"+name+".png"))){var texture=ImageIO.read(input);if(texture==null)throw new IOException("Invalid foreground texture");textures.put(name,texture);}
            double scale=Math.min(1,1024d/Math.max(w,h));var image=CinematicTerrain.foreground(Math.max(1,(int)(w*scale)),Math.max(1,(int)(h*scale)),c,options.scene,textures);
            var bytes=new ByteArrayOutputStream();ImageIO.write(image,"PNG",bytes);var nativeImage=NativeImage.read(new ByteArrayInputStream(bytes.toByteArray()));var texture=new DynamicTexture(()->"Cinematic scene foreground",nativeImage);
            try{id=Identifier.fromNamespaceAndPath("economy_tracker","scene_foreground_"+(++serial));Minecraft.getInstance().getTextureManager().register(id,texture);}catch(RuntimeException ex){texture.close();throw ex;}
            CACHE.put(key,id);while(CACHE.size()>6){var oldest=CACHE.keySet().iterator().next();Minecraft.getInstance().getTextureManager().release(CACHE.remove(oldest));}
        }catch(Exception ex){if(FAILED.size()>=32)FAILED.clear();FAILED.add(key);org.slf4j.LoggerFactory.getLogger("SceneForeground").warn("Foreground unavailable",ex);}
        if(id!=null)g.blit(RenderPipelines.GUI_TEXTURED,id,x,y,0,0,w,h,w,h,w,h,0xFFFFFFFF);
    }
}
