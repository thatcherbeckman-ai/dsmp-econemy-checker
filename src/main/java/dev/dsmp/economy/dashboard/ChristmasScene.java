package dev.dsmp.economy.dashboard;

import java.util.*;

/** Original procedural winter scenery, with optional gentle snow animation. */
public final class ChristmasScene {
    public record Rect(int x1,int y1,int x2,int y2,int color){}
    private ChristmasScene(){}
    private static void rect(List<Rect> list,int w,int h,int x,int y,int width,int height,int color){int x1=Math.clamp(x,0,w),y1=Math.clamp(y,0,h),x2=Math.clamp(x+width,0,w),y2=Math.clamp(y+height,0,h);if(x2>x1&&y2>y1)list.add(new Rect(x1,y1,x2,y2,color));}
    private static void triangle(List<Rect> list,int w,int h,int x,int top,int size,int color){for(int y=0;y<size;y+=3){int half=Math.max(1,y*2/3);rect(list,w,h,x-half,top+y,half*2+1,3,color);}}
    public static List<Rect> background(int w,int h,int accent,boolean festive){
        var out=new ArrayList<Rect>();int band=Math.max(1,h/48);
        for(int y=0;y<h;y+=band){double t=y/(double)Math.max(1,h);int r=(int)(14+t*21),g=(int)(16+t*9),b=(int)(30+t*42);r=(int)(r*(1-t*.15)+(accent>>16&255)*t*.15);g=(int)(g*(1-t*.15)+(accent>>8&255)*t*.15);b=(int)(b*(1-t*.15)+(accent&255)*t*.15);rect(out,w,h,0,y,w,band,0xFF000000|r<<16|g<<8|b);}
        if(!festive)return List.copyOf(out);
        for(int i=0;i<28;i++)rect(out,w,h,(i*131+19)%Math.max(1,w),(i*79+17)%Math.max(1,h*3/4),1,1,0x668A9FC7);
        int ground=h-12;
        for(int i=0;i<11;i++){int x=i*w/10+13,size=Math.max(16,h/7+(i%3)*6),top=ground-size;
            rect(out,w,h,x-2,top+size/2,4,size/2,0xFF293646);
            triangle(out,w,h,x,top,size/2,0xFF24394D);triangle(out,w,h,x,top+size/5,size*3/5,0xFF254249);triangle(out,w,h,x,top+size/6,size/7,0xFFCBDCE9);
        }
        for(int i=0;i<18;i++)rect(out,w,h,i*w/18,h-9-(i%4)*2,w/18+2,12,0xAA9DB7D5);
        for(int i=0;i<5;i++){int x=w/5+i*w/7,y=h-20-(i%2)*3,color=i%2==0?0xFFC8516C:0xFF3C9E87;rect(out,w,h,x,y,14,14,color);rect(out,w,h,x+6,y,2,14,0xFFE3CCA0);rect(out,w,h,x,y+4,14,2,0xFFE3CCA0);}
        rect(out,w,h,0,5,w,1,0xFF2B3545);return List.copyOf(out);
    }
    public static List<Rect> weather(int w,int h,double seconds,boolean festive,boolean animated){
        if(!festive)return List.of();double t=animated?seconds:0;var out=new ArrayList<Rect>();
        int[] colors={0xD44B63,0x55C591,0xE3BC66,0x6BC7E7,0xA382E0};
        for(int x=12,index=0;x<w;x+=22,index++){int rgb=colors[index%colors.length],alpha=190+(int)(45*Math.sin(t*.9+index));rect(out,w,h,x-2,5,7,10,0x22000000|rgb);rect(out,w,h,x,7,3,6,alpha<<24|rgb);rect(out,w,h,x+1,7,1,2,0xBFFFFFFF);}
        for(int i=0;i<48;i++){int x=(int)((i*137+19)%Math.max(1,w)+Math.sin(t*.3+i)*5),y=(int)(((i*83+31)%Math.max(1,h)+t*(3+i%7))%Math.max(1,h));int size=i%7==0?2:1;rect(out,w,h,x,y,size,size,0x779EBFD9);}
        return List.copyOf(out);
    }
}
