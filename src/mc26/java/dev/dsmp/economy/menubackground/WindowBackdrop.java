package dev.dsmp.economy.menubackground;

import java.awt.image.BufferedImage;

/** The real menu and its editor use the same base pixels and the same color/effect pipeline. */
public final class WindowBackdrop {
    public static final String[] NAMES={"Burgundy","Black","Charcoal","Navy","Teal","Forest","Purple","Rose","Sand"};
    public static final int[] COLORS={0x301316,0x08080B,0x24252A,0x121D38,0x103433,0x173022,0x30203D,0x492536,0x6D5E49};
    private WindowBackdrop(){}
    public static BufferedImage create(int w,int h,MenuBackgroundOptions options){
        if(w<1||h<1||w>2048||h>2048)throw new IllegalArgumentException("Invalid menu image size");options.validate();var image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<h;y++){double tone=options.scene==MenuScene.SOLID?1:1.14-.65*y/Math.max(1d,h-1);int base=options.scene==MenuScene.HOLIDAY?options.menuTheme.background:options.solidColor;int rgb=0;for(int shift=0;shift<=16;shift+=8)rgb|=Math.clamp((int)Math.round((base>>shift&255)*tone),0,255)<<shift;for(int x=0;x<w;x++)image.setRGB(x,y,rgb);}return image;
    }
    public static void holiday(MenuBackgroundOptions options,dev.dsmp.economy.inventory.InventoryTheme theme){if(theme==null)throw new IllegalArgumentException("Choose a menu theme");int previous=options.solidColor;solid(options,previous);options.menuTheme=theme;if(theme==dev.dsmp.economy.inventory.InventoryTheme.VANILLA)options.solidColor=0x301316;options.scene=theme==dev.dsmp.economy.inventory.InventoryTheme.VANILLA?MenuScene.ORIGINAL:MenuScene.HOLIDAY;}
    public static dev.dsmp.economy.inventory.InventoryOptions animation(MenuBackgroundOptions options){return new dev.dsmp.economy.inventory.InventoryOptions(options.menuTheme,options.animations,options.speed,options.density);}
    public static void solid(MenuBackgroundOptions options,int color){if((color&0xFF000000)!=0)throw new IllegalArgumentException("Use a six-digit RGB color");options.scene=MenuScene.SOLID;options.solidColor=color;options.overlay=0;options.brightness=100;options.blur=0;options.tint=0;options.vignette=0;options.parallax=false;options.particles=MenuScene.Particles.AUTO;}
}
