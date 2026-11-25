package dev.dsmp.economy.ui;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.dashboard.ChristmasScene;

import java.util.*;

/** Shared winter background and dark purple controls for every settings screen. */
public final class FestiveUi {
    private FestiveUi(){}
    public static void background(net.minecraft.client.gui.GuiGraphicsExtractor g,int width,int height,TrackerConfig config){MenuBackgroundRenderer.background(g,width,height,config,dev.dsmp.economy.menubackground.MenuBackgrounds.visible());}
    public static String fit(String text,int width,float scale){return HudFont.fit(text,Math.max(0,(int)(width/scale)));}
    public static void text(net.minecraft.client.gui.GuiGraphicsExtractor g,String value,int x,int y,int color,float scale,boolean bold){
        var pose=g.pose();pose.pushMatrix();pose.translate(x,y);pose.scale(scale);
        try{g.text(net.minecraft.client.Minecraft.getInstance().font,HudFont.text(value,bold),0,0,color,false);}finally{pose.popMatrix();}
    }
    public static void panel(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int w,int h,int accent){
        g.fill(x+1,y,x+w-1,y+h,dev.dsmp.economy.menubackground.MenuBackgrounds.visible().cards*255/100<<24|0x101522);g.fill(x,y+1,x+w,y+h-1,dev.dsmp.economy.menubackground.MenuBackgrounds.visible().cards*255/100<<24|0x101522);
        g.fill(x,y,x+w,y+1,0xFF000000|accent);g.fill(x,y+h-1,x+w,y+h,0x803F3F64);
    }
    private static void rounded(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int w,int h,int color){
        g.fill(x+3,y,x+w-3,y+h,color);g.fill(x+1,y+1,x+w-1,y+h-1,color);g.fill(x,y+3,x+w,y+h-3,color);
    }
    public static void control(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int w,int h,String label,boolean active,boolean hovered,int accent){
        int border=hovered?0xFFBFA8EF:active?0xFF000000|accent:0xFF393B50;
        int fill=active?0xFF000000|accent:hovered?0xFF302D46:0xFF191E2C;
        rounded(g,x,y,w,h,border);rounded(g,x+1,y+1,w-2,h-2,fill);
        String shown=fit(label,w-12,.84f);int tw=(int)(HudFont.width(shown,active)*.84f);
        text(g,shown,x+Math.max(6,(w-tw)/2),y+Math.max(3,(h-8)/2),active?0xFFFFFFFF:0xFFE3E5F3,.84f,active);
    }
}
