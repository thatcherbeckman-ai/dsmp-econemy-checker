package dev.dsmp.economy.ui;

import dev.dsmp.economy.dashboard.ChristmasScene.Rect;
import java.util.*;

/** Original winter ornaments, drawn behind the menu cards and native controls. */
public final class MenuDecorations {
    private MenuDecorations(){}
    private static void fill(List<Rect> out,int w,int h,int x,int y,int width,int height,int color){
        int left=Math.max(0,x),top=Math.max(0,y),right=Math.min(w,x+width),bottom=Math.min(h,y+height);
        if(right>left&&bottom>top)out.add(new Rect(left,top,right,bottom,color));
    }
    public static List<Rect> background(int w,int h,boolean enabled){
        if(!enabled||w<1||h<1)return List.of();var out=new ArrayList<Rect>();
        int[] baubles={0xFFE19BA9,0xFFF0D49C,0xFF9FDAC5,0xFFBEB3EB};
        // A narrow evergreen garland leaves the header and action row free.
        for(int x=0;x<w;x+=6){int y=2+(int)(Math.sin(x*.055)*2);fill(out,w,h,x,y,8,3,0xFF255849);fill(out,w,h,x+2,y+2,5,3,0xFF37755E);}
        int ornamentTop=w>=620&&h>=480?9:1;
        for(int x=40,index=0;x<w-16;x+=68,index++){
            fill(out,w,h,x,Math.max(0,ornamentTop-3),1,3,0xFFDDC99B);fill(out,w,h,x-2,ornamentTop,5,5,baubles[index%baubles.length]);fill(out,w,h,x-1,ornamentTop,2,2,0xCCFFFFFF);
        }
        for(int y=55,index=0;y<h-45;y+=58,index++){
            fill(out,w,h,w-5,y,3,22,0xFF2B5B50);fill(out,w,h,w-8,y+4,6,3,0xFF407D65);
            fill(out,w,h,w-10,y+18,1,5,0xFFBFAE89);fill(out,w,h,w-12,y+23,6,7,baubles[index%baubles.length]);fill(out,w,h,w-11,y+24,2,2,0x99FFFFFF);
        }
        // Wrapped gifts sit in the background above the footer snowbank.
        for(int i=0;i<8;i++){
            int x=20+i*Math.max(24,(w-50)/8),size=12+(i%3)*4,y=h-28-size;
            int color=i%3==0?0xFF9D546F:i%3==1?0xFF417C70:0xFF736299;
            fill(out,w,h,x,y,size,size,color);fill(out,w,h,x+size/2-1,y,2,size,0xFFE9CFA0);fill(out,w,h,x,y+4,size,2,0xFFE9CFA0);
            fill(out,w,h,x+size/2-4,y-3,3,3,0xFFD9B57F);fill(out,w,h,x+size/2+1,y-3,3,3,0xFFD9B57F);
        }
        if(w>=480&&h>=360){
            int cx=w/4,cy=h-145,r=24;
            for(int dy=-r;dy<=r;dy+=3)for(int dx=-r;dx<=r;dx+=3){int distance=dx*dx+dy*dy;if(distance>=14*14&&distance<=r*r)fill(out,w,h,cx+dx,cy+dy,3,3,((dx+dy)/3&1)==0?0xB32A6D56:0xB344876B);}
            for(int i=0;i<8;i++){double angle=i*Math.PI/4;fill(out,w,h,cx+(int)(Math.cos(angle)*19),cy+(int)(Math.sin(angle)*19),3,3,baubles[i%baubles.length]);}
            fill(out,w,h,cx-7,cy+18,7,5,0xFFD48BA1);fill(out,w,h,cx+1,cy+18,7,5,0xFFD48BA1);fill(out,w,h,cx-1,cy+18,3,9,0xFFEDBCCB);
        }
        return List.copyOf(out);
    }
    public static List<Rect> snow(int w,int h,double seconds,boolean enabled,boolean animated){
        if(!enabled||w<1||h<1)return List.of();var out=new ArrayList<Rect>();double time=animated&&Double.isFinite(seconds)?Math.max(0,seconds)%4096:0;
        for(int i=0;i<96;i++){
            int layer=i%3;double phase=(scatter(i,0x4327)+time*(.012+layer*.006))%1;
            int x=(int)(scatter(i,0x1B31)*(w+12))-6+(int)(Math.sin(time*.35+i)*4),y=(int)(phase*(h+12))-6;
            int size=layer==2?2:1;fill(out,w,h,x,y,size,size,layer==2?0x99D9E7F4:0x668EAEC7);
            if(i%12==0){fill(out,w,h,x-2,y+1,5,1,0x66BDD9E6);fill(out,w,h,x,y-1,1,5,0x66BDD9E6);}
        }
        return List.copyOf(out);
    }
    private static double scatter(int index,int salt){int value=index*0x9E3779B9+salt;value=(value^(value>>>16))*0x45D9F3B;return Integer.toUnsignedLong(value^(value>>>16))/4294967296d;}
}
