package dev.dsmp.economy.appearance;

import java.util.*;

/** Flat Minecraft-glass pane: cyan/white pixel edges and sparse diagonal flecks. */
public final class GlassLook {
    public record Rect(int x1,int y1,int x2,int y2,int color) {}
    private GlassLook() {}
    private static int color(int alpha,int rgb) { return Math.clamp(alpha,0,255)<<24|rgb; }
    public static List<Rect> fill(int x,int y,int width,int height,int original) {
        if (width<=0||height<=0||x<0) return List.of(); // Skip the legacy offset shadow.
        int alpha=original>>>24,unit=Math.max(1,Math.min(2,Math.min(width,height)/18));
        var result=new ArrayList<Rect>();
        int rgb=original&0xFFFFFF,tint=0;
        for(int shift:new int[]{16,8,0})tint|=((rgb>>shift&255)*4+(0x82DDE2>>shift&255))/5<<shift;
        result.add(new Rect(x,y,x+width,y+height,color(alpha/2,tint)));
        if(width<16||height<16)return List.copyOf(result);
        for(double[] spot:new double[][]{{.2,.2},{.72,.33},{.35,.7},{.84,.83}}) {
            int px=x+(int)((width-10*unit)*spot[0])+3*unit,py=y+(int)((height-10*unit)*spot[1])+3*unit;
            for(int i=0;i<3;i++) result.add(new Rect(px+i*2*unit,py+(2-i)*2*unit,px+i*2*unit+unit,py+(2-i)*2*unit+2*unit,color(alpha/3,0xD9FFFF)));
        }
        return List.copyOf(result);
    }
    public static List<Rect> border(int x,int y,int width,int height,int original) {
        if(width<=0||height<=0)return List.of();
        int a=original>>>24,u=Math.max(1,Math.min(2,Math.min(width,height)/18));
        var result=new ArrayList<Rect>();
        result.add(new Rect(x,y,x+width,y+u,color(a,0xE4FFFF)));result.add(new Rect(x,y,x+u,y+height,color(a,0xC5F4F7)));
        result.add(new Rect(x+width-u,y,x+width,y+height,color(a*3/4,0x83C6CD)));result.add(new Rect(x,y+height-u,x+width,y+height,color(a*3/4,0x77ABB5)));
        if(width>=6*u&&height>=6*u){
            result.add(new Rect(x+u,y+u,x+3*u,y+2*u,color(a,0xFFFFFF)));result.add(new Rect(x+u,y+u,x+2*u,y+3*u,color(a,0xFFFFFF)));
            result.add(new Rect(x+width-4*u,y+u,x+width-2*u,y+2*u,color(a,0xFFFFFF)));
            result.add(new Rect(x+width-2*u,y+height-4*u,x+width-u,y+height-2*u,color(a,0xDEFFFF)));
        }
        return List.copyOf(result);
    }
}
