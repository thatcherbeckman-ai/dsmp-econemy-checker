package dev.dsmp.economy.ui;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.dashboard.ChristmasScene;
import net.minecraft.*;
import java.util.*;

/** Shared winter background and dark purple controls for every settings screen. */
public final class FestiveUi {
    private record Backdrop(int width,int height,int accent,boolean festive){}
    private static Backdrop key;
    private static List<ChristmasScene.Rect> background=List.of();
    private FestiveUi(){}
    public static void background(class_332 g,int width,int height,TrackerConfig config){
        var next=new Backdrop(width,height,config.menuAccent,config.festiveBackground);
        if(!next.equals(key)){key=next;background=ChristmasScene.background(width,height,config.menuAccent,config.festiveBackground);}
        for(var r:background)g.method_25294(r.x1(),r.y1(),r.x2(),r.y2(),r.color());
        for(var r:ChristmasScene.weather(width,height,System.nanoTime()/1e9,config.festiveBackground,config.animateSnow))g.method_25294(r.x1(),r.y1(),r.x2(),r.y2(),r.color());
    }
    public static String fit(String text,int width,float scale){return HudFont.fit(text,Math.max(0,(int)(width/scale)));}
    public static void text(class_332 g,String value,int x,int y,int color,float scale,boolean bold){
        var pose=g.method_51448();pose.pushMatrix();pose.translate(x,y);pose.scale(scale);
        try{g.method_51439(class_310.method_1551().field_1772,HudFont.text(value,bold),0,0,color,false);}finally{pose.popMatrix();}
    }
    public static void panel(class_332 g,int x,int y,int w,int h,int accent){
        g.method_25294(x+1,y,x+w-1,y+h,0xED101522);g.method_25294(x,y+1,x+w,y+h-1,0xED101522);
        g.method_25294(x,y,x+w,y+1,0xFF000000|accent);g.method_25294(x,y+h-1,x+w,y+h,0x803F3F64);
    }
    public static void control(class_332 g,int x,int y,int w,int h,String label,boolean active,boolean hovered,int accent){
        int fill=active?0xFF000000|accent:hovered?0xFF34314C:0xFF1B2031;
        g.method_25294(x,y,x+w,y+h,fill);g.method_25294(x,y,x+2,y+h,active?0xFFDFD6FF:0xFF45435D);
        String shown=fit(label,w-8,.8f);int tw=(int)(HudFont.width(shown)*.8f);
        text(g,shown,x+Math.max(4,(w-tw)/2),y+Math.max(2,(h-8)/2),active?0xFFFFFFFF:0xFFE3E5F3,.8f,active);
    }
}
