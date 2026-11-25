package dev.dsmp.economy.menubackground;

import java.util.*;

public final class MenuParticles {
    public record Rect(int x,int y,int width,int height,int color){}
    private MenuParticles(){}
    private static void put(List<Rect> out,int w,int h,int x,int y,int width,int height,int color){int l=Math.max(0,x),t=Math.max(0,y),r=Math.min(w,x+width),b=Math.min(h,y+height);if(r>l&&b>t)out.add(new Rect(l,t,r-l,b-t,color));}
    public static List<Rect> frame(MenuBackgroundOptions options,int w,int h,double seconds){
        var kind=options.effectiveParticles();if(kind==MenuScene.Particles.NONE||kind==MenuScene.Particles.AUTO||w<1||h<1)return List.of();
        var out=new ArrayList<Rect>();double time=options.time(seconds);int count=32+options.density*48,alpha=options.particleOpacity*255/100;
        for(int i=0;i<count;i++){
            int layer=i%3;double phase=(random(i,0x7341)+time*(.015+layer*.009))%1;
            int x=(int)(random(i,0x1331)*(w+16))-8+(int)(Math.sin(time*.4+i)*(layer+1)*3),y=(int)(phase*(h+16))-8;
            int color=switch(kind){case PETALS->(i&1)==0?0xFFD1E3:0xE9A9D4;case LEAVES->(i&1)==0?0xDF9D5C:0xC77A54;case FIREFLIES->0xE5DB8D;case CONFETTI->new int[]{0xE8A1D2,0x92D6D3,0xF2D28D}[i%3];default->0xC5DAEE;};
            color=alpha<<24|color;
            switch(kind){
                case PETALS,LEAVES->{put(out,w,h,x,y,3+layer,2,color);put(out,w,h,x+1,y+2,2,1,color);}
                case SNOW,STARS->{put(out,w,h,x,y,1+layer/2,1+layer/2,color);if(i%7==0){put(out,w,h,x-2,y,5,1,color);put(out,w,h,x,y-2,1,5,color);}}
                case FIREFLIES->{put(out,w,h,x-1,y-1,4,4,Math.max(8,alpha/6)<<24|0xE5DB8D);put(out,w,h,x,y,2,2,color);}
                case RAIN->{put(out,w,h,x,y,1,5+layer*2,color);put(out,w,h,x-1,y+4,1,3,color);}
                case CONFETTI->put(out,w,h,x,y,(i&1)==0?2:3,(i&1)==0?4:2,color);
                default->{}
            }
        }return List.copyOf(out);
    }
    private static double random(int i,int salt){int n=i*0x9E3779B9+salt;n=(n^(n>>>16))*0x45D9F3B;return Integer.toUnsignedLong(n^(n>>>16))/4294967296d;}
}
