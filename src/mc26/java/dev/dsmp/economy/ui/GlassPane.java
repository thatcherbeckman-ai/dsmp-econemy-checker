package dev.dsmp.economy.ui;
import dev.dsmp.economy.appearance.GlassLook;
import net.minecraft.client.gui.GuiGraphicsExtractor;
public final class GlassPane {
    private GlassPane() {}
    public static void fill(net.minecraft.client.gui.GuiGraphicsExtractor graphics,int x,int y,int w,int h,int color) {
        for(var r:GlassLook.fill(x,y,w,h,color))graphics.fill(r.x1(),r.y1(),r.x2(),r.y2(),r.color());
    }
    public static void border(net.minecraft.client.gui.GuiGraphicsExtractor graphics,int x,int y,int w,int h,int color) {
        for(var r:GlassLook.border(x,y,w,h,color))graphics.fill(r.x1(),r.y1(),r.x2(),r.y2(),r.color());
    }
}
