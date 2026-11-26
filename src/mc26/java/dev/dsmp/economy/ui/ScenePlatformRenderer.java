package dev.dsmp.economy.ui;

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

/** Loads the game's current block textures; no game assets are copied into the mod. */
final class ScenePlatformRenderer {
    private record Key(CharacterComposition.Platform platform,CharacterComposition.Base base,MenuScene scene,boolean lighting,boolean shadow,int height,int direction,int strength,int softness){}
    private record Texture(Identifier id,int height){}
    private static final Map<Key,Texture> CACHE=new LinkedHashMap<>(12,.75f,true);private static final Set<Key> FAILED=new HashSet<>();private static int serial;
    private ScenePlatformRenderer(){}
    static void draw(GuiGraphicsExtractor g,int cx,int feet,int viewportBottom,int size,MenuScene scene,CharacterComposition options){
        if(options.platform==CharacterComposition.Platform.NONE||size<4)return;
        int width=options.base==CharacterComposition.Base.BLOCK?size:(int)(size*(options.base==CharacterComposition.Base.DECK?3.2:2.7));
        float scale=width/512f;int top=feet-Math.round(PlatformSurface.FOOT_Y*scale);
        int height=options.base==CharacterComposition.Base.BLOCK?512:Math.clamp(((int)Math.ceil((viewportBottom-top)/scale+32)+127)/128*128,512,2048);
        var key=new Key(options.platform,options.base,scene,options.matchLighting,options.shadow,height,options.lightDirection,options.lightStrength,options.shadowSoftness);var texture=CACHE.get(key);
        if(texture==null&&!FAILED.contains(key))try{texture=create(key,options);CACHE.put(key,texture);while(CACHE.size()>10){var first=CACHE.keySet().iterator().next();Minecraft.getInstance().getTextureManager().release(CACHE.remove(first).id);}}
        catch(Exception ex){if(FAILED.size()>=32)FAILED.clear();FAILED.add(key);org.slf4j.LoggerFactory.getLogger("CharacterPlatform").warn("Platform texture unavailable",ex);}
        if(texture!=null)g.blit(RenderPipelines.GUI_TEXTURED,texture.id,cx-width/2,top,0,0,width,Math.round(texture.height*scale),512,texture.height,512,texture.height,0xFFFFFFFF);
    }
    private static Texture create(Key key,CharacterComposition options)throws IOException{
        String top=switch(key.platform){case GRASS->"grass_block_top";case STONE->"stone_bricks";case OAK->"oak_planks";case CHERRY->"cherry_planks";case SNOW->"snow";case MOSS->"moss_block";case SANDSTONE->"sandstone_top";case QUARTZ->"quartz_block_top";case DEEPSLATE->"deepslate_tiles";case BAMBOO->"bamboo_planks";default->"stone";};
        String side=switch(key.platform){case GRASS->key.base==CharacterComposition.Base.BLOCK?"grass_block_side":"stone";case MOSS->key.base==CharacterComposition.Base.BLOCK?"moss_block":"stone";case SNOW->"snow";case SANDSTONE->"sandstone";case QUARTZ->"quartz_block_side";default->top;};
        var topImage=read(top);var sideImage=read(side);
        if(key.platform==CharacterComposition.Platform.GRASS)topImage=tint(topImage,0x86AC64);
        var surface=PlatformSurface.create(topImage,sideImage,options,key.scene,key.height);var bytes=new ByteArrayOutputStream();ImageIO.write(surface,"PNG",bytes);var nativeImage=NativeImage.read(new ByteArrayInputStream(bytes.toByteArray()));DynamicTexture nativeTexture=null;
        try{nativeTexture=new DynamicTexture(()->"Character scene platform",nativeImage);var id=Identifier.fromNamespaceAndPath("economy_tracker","scene_platform_"+(++serial));Minecraft.getInstance().getTextureManager().register(id,nativeTexture);return new Texture(id,key.height);}catch(RuntimeException ex){if(nativeTexture!=null)nativeTexture.close();else nativeImage.close();throw ex;}
    }
    private static BufferedImage read(String name)throws IOException{try(var stream=Minecraft.getInstance().getResourceManager().open(Identifier.fromNamespaceAndPath("minecraft","textures/block/"+name+".png"))){var image=ImageIO.read(stream);if(image==null)throw new IOException("Invalid block texture");return image;}}
    private static BufferedImage tint(BufferedImage image,int color){var result=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int source=image.getRGB(x,y),rgb=source&0xFF000000;for(int shift=0;shift<=16;shift+=8)rgb|=((source>>shift&255)*(color>>shift&255)/255)<<shift;result.setRGB(x,y,rgb);}return result;}
}
