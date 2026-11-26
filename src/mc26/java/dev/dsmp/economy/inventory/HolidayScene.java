package dev.dsmp.economy.inventory;

import java.util.*;

/** Bounded original pixel artwork. Motion changes positions, never rapidly flashes brightness. */
public final class HolidayScene {
    public static final int WIDTH=176,HEIGHT=166;
    public record Rect(int x,int y,int width,int height,int color){}
    private HolidayScene(){}
    private static final class Canvas {
        final int width,height;final List<Rect> rects=new ArrayList<>();
        Canvas(int width,int height){this.width=width;this.height=height;}
        void fill(int x,int y,int w,int h,int color){int left=Math.max(0,x),top=Math.max(0,y),right=Math.min(width,x+w),bottom=Math.min(height,y+h);if(right>left&&bottom>top)rects.add(new Rect(left,top,right-left,bottom-top,color));}
        void solid(int x,int y,int w,int h,int color){fill(x,y,w,h,0xFF000000|color);}
        void border(int x,int y,int w,int h,int color){solid(x,y,w,1,color);solid(x,y+h-1,w,1,color);solid(x,y,1,h,color);solid(x+w-1,y,1,h,color);}
    }
    public static List<Rect> panel(InventoryOptions options,double seconds){
        if(options.theme()==InventoryTheme.VANILLA)return List.of();
        var t=options.theme();var c=new Canvas(WIDTH,HEIGHT);int bg=t.background,a=t.accent,b=t.secondary;
        c.solid(1,0,174,166,blend(bg,0x080D16,.2));c.solid(0,1,176,164,bg);
        for(int y=2;y<164;y+=4)c.solid(2,y,172,Math.min(4,164-y),blend(bg,0x080D16,y/166d*.3));
        c.border(1,1,174,164,blend(a,b,.35));c.border(3,3,170,160,blend(bg,a,.25));
        for(int x=5;x<172;x+=8)c.solid(x,3,4,2,(x/8)%2==0?a:b);
        c.solid(25,7,51,72,blend(bg,0x020611,.6));c.border(25,7,51,72,blend(bg,a,.35));
        for(int i=0;i<4;i++)slot(c,7,7+i*18,t);
        for(int y=0;y<2;y++)for(int x=0;x<2;x++)slot(c,97+x*18,17+y*18,t);
        slot(c,153,27,t);slot(c,76,61,t);
        for(int y=0;y<3;y++)for(int x=0;x<9;x++)slot(c,7+x*18,83+y*18,t);
        for(int x=0;x<9;x++)slot(c,7+x*18,141,t);
        c.solid(134,32,9,2,blend(a,b,.5));c.solid(140,30,2,6,blend(a,b,.5));c.solid(142,31,2,4,blend(a,b,.5));c.solid(144,32,1,2,blend(a,b,.5));
        motif(c,t,46,53,120); // Subtle emblem behind the real player model.
        double time=options.time(seconds);
        int count=16+options.intensity()*12;
        for(int i=0;i<count;i++){
            double phase=(i*.61803398875+time*(t.motif==InventoryTheme.Motif.FIREWORK?.12:.055))%1;
            int x=29+(i*19)%42,y=11+(int)(phase*62);
            c.fill(x,y,1+(i%5==0?1:0),1+(i%5==0?1:0),0x88000000|((i&1)==0?a:b));
        }
        // Decorative rivets stay outside all item hit boxes.
        for(int x:new int[]{3,171})for(int y:new int[]{3,160})c.solid(x,y,2,2,b);
        return List.copyOf(c.rects);
    }
    private static void slot(Canvas c,int x,int y,InventoryTheme t){
        c.solid(x,y,18,18,blend(t.background,t.accent,.45));c.solid(x+1,y+1,16,16,blend(t.background,0x02050A,.55));
        c.solid(x+1,y+17,17,1,blend(t.background,t.secondary,.65));
    }
    public static List<Rect> ambient(InventoryOptions options,int width,int height,double seconds){
        if(options.theme()==InventoryTheme.VANILLA||width<=0||height<=0)return List.of();
        var c=new Canvas(width,height);var t=options.theme();double time=options.time(seconds);int count=32+options.intensity()*48;
        for(int i=0;i<count;i++){
            int layer=i%3;
            double fraction=(scatter(i,0x7823)+time*(.018+layer*.008))%1;
            int x=(int)(scatter(i,0x1431)*(width+20))-10,y=(int)(fraction*(height+20))-10;
            x+=(int)(Math.sin(time*(.35+layer*.1)+i)*(4+layer*2));
            if(t.motif==InventoryTheme.Motif.TREE){
                int snow=0x909FE3D6;c.fill(x+2,y,1,5,snow);c.fill(x,y+2,5,1,snow);c.fill(x+1,y+1,3,3,0x409FE3D6);
            }else if(t.motif==InventoryTheme.Motif.FIREWORK){
                int centerX=(int)(scatter(i,0x32A9)*width),centerY=(int)(scatter(i,0x68B5)*height);
                double radius=3+fraction*15;
                for(int ray=0;ray<8;ray++){double angle=ray*Math.PI/4;c.fill(centerX+(int)(Math.cos(angle)*radius),centerY+(int)(Math.sin(angle)*radius),1,1,0x90000000|((ray&1)==0?t.accent:t.secondary));}
                // Confetti falls between the expanding fireworks.
                if((i&3)==0)c.fill(x,y,2,2,0xA0000000|((i&4)==0?t.accent:t.secondary));
            }else motif(c,t,x,y,100);
        }
        return List.copyOf(c.rects);
    }
    public static List<Rect> header(InventoryOptions options,int width){if(options.theme()==InventoryTheme.VANILLA||width<1)return List.of();var canvas=new Canvas(width,12);for(int x=12;x<width-8;x+=36)motif(canvas,options.theme(),x,1,220);return List.copyOf(canvas.rects);}
    private static double scatter(int index,int salt){
        int value=index*0x9E3779B9+salt;
        value=(value^(value>>>16))*0x45D9F3B;value=(value^(value>>>16))*0x45D9F3B;
        return Integer.toUnsignedLong(value^(value>>>16))/4294967296d;
    }
    private static void motif(Canvas c,InventoryTheme t,int x,int y,int alpha){
        int a=alpha<<24|t.accent,b=alpha<<24|t.secondary;
        switch(t.motif){
            case TREE->{c.fill(x+3,y,1,2,a);c.fill(x+2,y+2,3,2,a);c.fill(x+1,y+4,5,2,a);c.fill(x,y+6,7,2,a);c.fill(x+3,y+8,1,2,b);c.fill(x+1,y+6,1,1,b);c.fill(x+4,y+4,1,1,b);}
            case PUMPKIN->{c.fill(x+3,y,2,2,b);c.fill(x+1,y+2,6,6,a);c.fill(x,y+3,8,4,a);c.fill(x+2,y+3,1,2,0xCC171323);c.fill(x+5,y+3,1,2,0xCC171323);c.fill(x+2,y+6,4,1,0xCC171323);}
            case HEART->{c.fill(x+1,y,2,1,a);c.fill(x+4,y,2,1,a);c.fill(x,y+1,7,2,a);c.fill(x+1,y+3,5,1,a);c.fill(x+2,y+4,3,1,a);c.fill(x+3,y+5,1,1,a);}
            case CLOVER->{c.fill(x+2,y,3,3,a);c.fill(x,y+2,3,3,a);c.fill(x+4,y+2,3,3,a);c.fill(x+3,y+4,1,4,b);}
            case EGG->{c.fill(x+2,y,3,1,a);c.fill(x+1,y+1,5,2,a);c.fill(x,y+3,7,4,a);c.fill(x+1,y+7,5,1,a);c.fill(x+1,y+3,5,1,b);c.fill(x+1,y+5,5,1,b);}
            case LANTERN->{c.fill(x+2,y,3,1,b);c.fill(x+1,y+1,5,6,a);c.fill(x+3,y+1,1,6,b);c.fill(x+2,y+7,3,1,b);c.fill(x+3,y+8,1,2,b);}
            case CRESCENT->{c.fill(x+2,y,3,1,a);c.fill(x+1,y+1,2,1,a);c.fill(x,y+2,2,4,a);c.fill(x+1,y+6,2,1,a);c.fill(x+2,y+7,3,1,a);c.fill(x+5,y+6,1,1,a);c.fill(x+6,y+1,1,1,b);}
            case LAMP->{c.fill(x+3,y,1,2,b);c.fill(x+2,y+2,3,2,a);c.fill(x,y+4,7,1,b);c.fill(x+1,y+5,5,2,a);c.fill(x+2,y+7,3,1,a);}
            case CANDLE->{for(int i=0;i<3;i++){c.fill(x+i*3,y+1,1,5,a);c.fill(x+i*3,y-1,1,1,b);}c.fill(x,y+6,7,1,a);c.fill(x+3,y+7,1,2,a);}
            case LEAF->{c.fill(x+3,y,1,1,b);c.fill(x+2,y+1,3,2,a);c.fill(x,y+3,7,2,a);c.fill(x+1,y+5,5,1,a);c.fill(x+3,y+5,1,4,b);}
            case GIFT->{c.fill(x+1,y+1,5,2,b);c.fill(x,y+3,7,2,a);c.fill(x+1,y+5,5,4,a);c.fill(x+3,y+1,1,8,b);}
            case FLOWER->{c.fill(x+2,y,3,2,a);c.fill(x,y+2,7,3,a);c.fill(x+2,y+5,3,2,a);c.fill(x+2,y+2,3,3,b);}
            case SUN->{c.fill(x+2,y+2,5,5,a);c.fill(x+4,y-1,1,2,b);c.fill(x+4,y+8,1,2,b);c.fill(x-1,y+4,2,1,b);c.fill(x+8,y+4,2,1,b);}
            case STAR->{c.fill(x+3,y,1,7,a);c.fill(x,y+3,7,1,a);c.fill(x+2,y+2,3,3,b);}
            case FIREWORK,SNOW->{c.fill(x+3,y,1,7,a);c.fill(x,y+3,7,1,a);for(int i:new int[]{0,6})for(int j:new int[]{0,6})c.fill(x+i,y+j,1,1,b);}
        }
    }
    private static int blend(int a,int b,double t){int color=0;for(int shift:new int[]{0,8,16})color|=(int)Math.round((a>>shift&255)*(1-t)+(b>>shift&255)*t)<<shift;return color;}
}
