package dev.dsmp.economy.design;
import dev.dsmp.economy.menubackground.CharacterComposition;
public final class SceneDrag {
    private SceneDrag(){}
    public static int percent(double coordinate,int extent){return Math.clamp((int)Math.round(coordinate*100/Math.max(1,extent)),0,100);}
    public static int snap(int percent,int extent,boolean enabled,int... guides){if(enabled)for(int target:guides)if(Math.abs(percent-target)*extent/100d<=7)return target;return percent;}
    public static void character(CharacterComposition c,double x,double feet,int w,int h){c.x=snap(percent(x,w),w,c.snapGuides,25,50,75);c.ground=Math.clamp(snap(percent(feet,h),h,c.snapGuides,55,78,83,90),55,90);}
    public static void caption(CharacterComposition c,double centerX,double top,int w,int h){c.freeCaption=true;c.captionX=snap(percent(centerX,w),w,c.snapGuides,25,50,75);c.captionY=snap(percent(top,h),h,c.snapGuides,10,50,90);}
}
