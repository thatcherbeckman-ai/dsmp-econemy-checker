package dev.dsmp.economy.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Paints behind vanilla's player model, slots, items, recipe controls and tooltips. */
public final class InventoryPainter {
    private static final long START=System.nanoTime();
    private InventoryPainter(){}
    public static double seconds(){return (System.nanoTime()-START)/1e9;}
    public static void background(GuiGraphicsExtractor g,int x,int y,InventoryOptions options){
        var screen=Minecraft.getInstance().gui.screen();
        if(screen!=null)for(var r:HolidayScene.ambient(options,screen.width,screen.height,seconds()))g.fill(r.x(),r.y(),r.x()+r.width(),r.y()+r.height(),r.color());
        panel(g,x,y,options,seconds());
    }
    public static void panel(GuiGraphicsExtractor g,int x,int y,InventoryOptions options,double seconds){
        for(var r:HolidayScene.panel(options,seconds))g.fill(x+r.x(),y+r.y(),x+r.x()+r.width(),y+r.y()+r.height(),r.color());
    }
}
