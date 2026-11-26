package dev.dsmp.economy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/** Three tiny antialiased masks, uploaded once and stretched with protected rounded corners. */
final class UiSurfaces {
    private static final Identifier[] MASKS=new Identifier[6];
    private static boolean failed;
    private UiSurfaces(){}
    static void fill(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){draw(g,0,x,y,w,h,color);}
    static void card(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){draw(g,4,x,y,w,h,color);}
    static void cardOutline(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){draw(g,5,x,y,w,h,color);}
    static void outline(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){draw(g,1,x,y,w,h,color);}
    static void corners(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){draw(g,3,x,y,w,h,color);}
    static void shadow(GuiGraphicsExtractor g,int x,int y,int w,int h){draw(g,2,x-2,y,w+4,h+4,0x60000000);}
    private static void initialize()throws IOException{
        for(int i=0;i<MASKS.length;i++)if(MASKS[i]==null){
            var hi=new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);var p=hi.createGraphics();
            try{
                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.scale(4,4);p.setColor(Color.WHITE);
                if(i==4)p.fill(new RoundRectangle2D.Double(.5,.5,31,31,8,8));
                else if(i==5){p.setStroke(new BasicStroke(1));p.draw(new RoundRectangle2D.Double(.5,.5,31,31,8,8));}
                else if(i==0)p.fill(new RoundRectangle2D.Double(.5,.5,31,31,14,14));
                else if(i==1){p.setStroke(new BasicStroke(1));p.draw(new RoundRectangle2D.Double(.5,.5,31,31,14,14));}
                else if(i==3){p.fillRect(0,0,32,32);p.setComposite(AlphaComposite.Clear);p.fill(new RoundRectangle2D.Double(.5,.5,31,31,14,14));}
                else for(int spread=3;spread>=0;spread--){p.setColor(new Color(255,255,255,30+spread*5));p.fill(new RoundRectangle2D.Double(4-spread,4-spread,24+spread*2,24+spread*2,12+spread*2,12+spread*2));}
            }finally{p.dispose();}
            var image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);var smaller=image.createGraphics();try{smaller.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);smaller.drawImage(hi,0,0,32,32,null);}finally{smaller.dispose();}
            var bytes=new ByteArrayOutputStream();ImageIO.write(image,"PNG",bytes);var nativeImage=NativeImage.read(new ByteArrayInputStream(bytes.toByteArray()));DynamicTexture texture=null;
            try{texture=new DynamicTexture(()->"Menu surface",nativeImage);var id=Identifier.fromNamespaceAndPath("economy_tracker","menu_surface_"+i);Minecraft.getInstance().getTextureManager().register(id,texture);MASKS[i]=id;}
            catch(RuntimeException ex){if(texture!=null)texture.close();else nativeImage.close();throw ex;}
        }
    }
    private static void draw(GuiGraphicsExtractor g,int kind,int x,int y,int w,int h,int color){
        if(w<1||h<1||(color>>>24)==0)return;
        if(!failed&&MASKS[kind]==null)try{initialize();}catch(Exception ex){failed=true;org.slf4j.LoggerFactory.getLogger("MenuSurfaces").warn("Using simple menu surfaces",ex);}
        if(MASKS[kind]==null){if(kind==0)g.fill(x,y,x+w,y+h,color);return;}
        int border=Math.min(8,Math.min(w,h)/2);if(border<1){g.fill(x,y,x+w,y+h,color);return;}
        int[] destX={x,x+border,x+w-border},destY={y,y+border,y+h-border},destW={border,w-border*2,border},destH={border,h-border*2,border},source={0,8,24},size={8,16,8};
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)if(destW[col]>0&&destH[row]>0)g.blit(RenderPipelines.GUI_TEXTURED,MASKS[kind],destX[col],destY[row],source[col],source[row],destW[col],destH[row],size[col],size[row],32,32,color);
    }
}
