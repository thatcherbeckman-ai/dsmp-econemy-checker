package dev.dsmp.economy.ui;
import dev.dsmp.economy.appearance.GlassLook;
import net.minecraft.class_332;
public final class GlassPane {
    private GlassPane() {}
    public static void fill(class_332 graphics,int x,int y,int w,int h,int color) {
        for(var r:GlassLook.fill(x,y,w,h,color))graphics.method_25294(r.x1(),r.y1(),r.x2(),r.y2(),r.color());
    }
    public static void border(class_332 graphics,int x,int y,int w,int h,int color) {
        for(var r:GlassLook.border(x,y,w,h,color))graphics.method_25294(r.x1(),r.y1(),r.x2(),r.y2(),r.color());
    }
}
