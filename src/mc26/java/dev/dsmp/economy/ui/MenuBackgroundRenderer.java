package dev.dsmp.economy.ui;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.dashboard.ChristmasScene;
import dev.dsmp.economy.menubackground.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.NativeImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/** Bounded photo textures, cached blur/color work, and live menu-only scenery. */
public final class MenuBackgroundRenderer {
    private record Key(MenuScene scene,String custom,int brightness,int blur,int tint,int tintAmount,int w,int h,int accent,boolean festive,String geometry){}
    private record Texture(Identifier id,int width,int height){}
    private static final Map<Key,Texture> CACHE=new LinkedHashMap<>(8,.75f,true);
    private static final Set<Key> FAILED=new HashSet<>();private static int serial;
    private static final long START=System.nanoTime();
    private MenuBackgroundRenderer(){}
    public static void refresh(){var manager=Minecraft.getInstance().getTextureManager();CACHE.values().forEach(t->manager.release(t.id));CACHE.clear();FAILED.clear();}
    public static void background(GuiGraphicsExtractor g,int w,int h,TrackerConfig config,MenuBackgroundOptions options){
        if(w<1||h<1)return;double seconds=(System.nanoTime()-START)/1e9;
        var key=new Key(options.scene,options.customImage,options.brightness,options.blur,options.tint,options.tintAmount,options.scene==MenuScene.ORIGINAL||CinematicTerrain.usesBlocks(options)?w:0,options.scene==MenuScene.ORIGINAL||CinematicTerrain.usesBlocks(options)?h:0,options.scene==MenuScene.ORIGINAL?config.menuAccent:0,options.scene==MenuScene.ORIGINAL&&config.festiveBackground,CinematicTerrain.usesBlocks(options)?options.character.size+":"+options.character.x+":"+options.character.ground+":"+options.character.platformSize+":"+options.character.platform+":"+options.character.shadow:"");
        Texture texture=CACHE.get(key);
        if(texture==null&&!FAILED.contains(key)){
            try{texture=create(key,options);CACHE.put(key,texture);while(CACHE.size()>6){var oldest=CACHE.keySet().iterator().next();Minecraft.getInstance().getTextureManager().release(CACHE.remove(oldest).id);}}
            catch(Exception ex){if(FAILED.size()<64)FAILED.add(key);org.slf4j.LoggerFactory.getLogger("MenuBackgrounds").warn("Background unavailable: {}",options.scene,ex);}
        }
        g.fill(0,0,w,h,0xFF111624);
        if(texture!=null&&CinematicTerrain.usesBlocks(options)){g.blit(RenderPipelines.GUI_TEXTURED,texture.id,0,0,0,0,w,h,texture.width,texture.height,texture.width,texture.height,0xFFFFFFFF);}
        else if(texture!=null){
            float scale=(options.contain?Math.min(w/(float)texture.width,h/(float)texture.height):Math.max(w/(float)texture.width,h/(float)texture.height))*options.zoom/100f;
            if(options.animations&&options.parallax&&!options.contain&&!CinematicTerrain.usesIllustrated(options))scale*=1.035f;
            int tw=Math.max(1,Math.round(texture.width*scale)),th=Math.max(1,Math.round(texture.height*scale)),x=(w-tw)/2,y=(h-th)/2;
            if(options.animations&&options.parallax&&!options.contain&&!CinematicTerrain.usesIllustrated(options)){double time=options.time(seconds);x+=(int)(Math.sin(time*.13)*Math.max(0,(tw-w)/2)*.6);y+=(int)(Math.cos(time*.11)*Math.max(0,(th-h)/2)*.3);}
            g.blit(RenderPipelines.GUI_TEXTURED,texture.id,x,y,0,0,tw,th,texture.width,texture.height,texture.width,texture.height,0xFFFFFFFF);
        }
        if(options.overlay>0)g.fill(0,0,w,h,(options.overlay*255/100)<<24|0x080C17);
        if(options.vignette>0)for(int i=0;i<12;i++){int px=w*i/100,py=h*i/100,alpha=(12-i)*options.vignette;g.fill(px,py,w-px,py+Math.max(1,h/100),alpha<<24|0x050914);g.fill(px,h-py-Math.max(1,h/100),w-px,h-py,alpha<<24|0x050914);g.fill(px,py,px+Math.max(1,w/100),h-py,alpha<<24|0x050914);g.fill(w-px-Math.max(1,w/100),py,w-px,h-py,alpha<<24|0x050914);}
        boolean decorations=options.decorations==2||options.decorations==0&&(options.scene==MenuScene.WINTER_CABIN||options.scene==MenuScene.ORIGINAL&&config.festiveBackground);
        if(decorations)for(var r:MenuDecorations.background(w,h,true))g.fill(r.x1(),r.y1(),r.x2(),r.y2(),r.color());
        if(options.scene==MenuScene.ORIGINAL&&options.particles==MenuScene.Particles.AUTO){
            double time=options.animations&&config.animateSnow?options.time(seconds):0;
            for(var r:ChristmasScene.weather(w,h,time,config.festiveBackground,true))g.fill(r.x1(),r.y1(),r.x2(),r.y2(),opacity(r.color(),options.particleOpacity));
            if(options.density>=1)for(var r:MenuDecorations.snow(w,h,time,config.festiveBackground,true))g.fill(r.x1(),r.y1(),r.x2(),r.y2(),opacity(r.color(),options.particleOpacity));
            if(options.density>=2)for(var r:MenuDecorations.snow(w,h,time+37,config.festiveBackground,true))g.fill(r.x1(),r.y1(),r.x2(),r.y2(),opacity(r.color(),options.particleOpacity));
        }else for(var r:MenuParticles.frame(options,w,h,seconds))g.fill(r.x(),r.y(),r.x()+r.width(),r.y()+r.height(),r.color());
    }
    private static int opacity(int color,int amount){return Math.min(255,(color>>>24)*amount/65)<<24|color&0xFFFFFF;}
    private static BufferedImage block(String name)throws IOException{try(var input=Minecraft.getInstance().getResourceManager().open(Identifier.fromNamespaceAndPath("minecraft","textures/block/"+name+".png"))){var image=ImageIO.read(input);if(image==null)throw new IOException("Missing terrain texture");return image;}}
    private static Texture create(Key key,MenuBackgroundOptions options)throws IOException{
        BufferedImage source;
        if(CinematicTerrain.usesBlocks(options)){
            double scale=Math.min(1,1024d/Math.max(key.w,key.h));int w=Math.max(1,(int)(key.w*scale)),h=Math.max(1,(int)(key.h*scale));
            var palette=BlockScenery.of(options.scene);var textures=new HashMap<String,BufferedImage>();for(String name:new String[]{palette.top(),palette.side(),palette.rock(),palette.trunk(),palette.leaves()})if(!textures.containsKey(name))textures.put(name,block(name));
            source=CinematicTerrain.create(w,h,options.character,options.scene,textures);
        }else if(key.scene==MenuScene.ORIGINAL||key.scene==MenuScene.GRADIENT){
            int w=key.scene==MenuScene.ORIGINAL?Math.max(1,key.w):960,h=key.scene==MenuScene.ORIGINAL?Math.max(1,key.h):540;
            source=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var painter=source.createGraphics();try{for(var r:ChristmasScene.background(w,h,key.accent,key.scene==MenuScene.ORIGINAL&&key.festive)){painter.setColor(new java.awt.Color(r.color(),true));painter.fillRect(r.x1(),r.y1(),r.x2()-r.x1(),r.y2()-r.y1());}}finally{painter.dispose();}
        }else if(key.scene==MenuScene.CUSTOM){source=ImageIO.read(new ByteArrayInputStream(MenuBackgrounds.store().image(key.custom)));}
        else{
            var id=Identifier.fromNamespaceAndPath("economy_tracker","textures/menu/"+key.scene.atlas+".png");
            try(var stream=Minecraft.getInstance().getResourceManager().open(id)){
                var atlas=ImageIO.read(stream);if(atlas==null||atlas.getWidth()%3!=0||atlas.getHeight()%2!=0)throw new IOException("Invalid scenic atlas");int w=atlas.getWidth()/3,h=atlas.getHeight()/2;source=atlas.getSubimage(key.scene.tile%3*w,key.scene.tile/3*h,w,h);
            }
        }
        if(source==null)throw new IOException("Invalid background PNG");var prepared=MenuImageEffects.prepare(source,options);var bytes=new ByteArrayOutputStream();ImageIO.write(prepared,"PNG",bytes);
        var image=NativeImage.read(new ByteArrayInputStream(bytes.toByteArray()));DynamicTexture nativeTexture=null;
        try{nativeTexture=new DynamicTexture(()->"Menu background",image);var id=Identifier.fromNamespaceAndPath("economy_tracker","menu_background_"+(++serial));Minecraft.getInstance().getTextureManager().register(id,nativeTexture);return new Texture(id,prepared.getWidth(),prepared.getHeight());}
        catch(RuntimeException ex){if(nativeTexture!=null)nativeTexture.close();else image.close();throw ex;}
    }
}
