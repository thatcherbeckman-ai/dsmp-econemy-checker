package dev.dsmp.economy.menubackground;

import java.awt.image.BufferedImage;
import java.awt.RenderingHints;

/** Cached CPU image preparation; never runs for a cache hit or changes source files. */
public final class MenuImageEffects {
    private MenuImageEffects(){}
    public static BufferedImage prepare(BufferedImage original,MenuBackgroundOptions options){
        options.validate();int w=original.getWidth(),h=original.getHeight();double scale=Math.min(1,1024d/Math.max(w,h));w=Math.max(1,(int)(w*scale));h=Math.max(1,(int)(h*scale));
        var image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();try{g.setColor(new java.awt.Color(14,16,30));g.fillRect(0,0,w,h);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(original,0,0,w,h,null);}finally{g.dispose();}
        int[] pixels=image.getRGB(0,0,w,h,null,0,w);double brightness=options.brightness/100d,tint=options.tint==0?0:options.tintAmount/100d;int color=MenuBackgroundOptions.TINT_COLORS[options.tint];
        for(int i=0;i<pixels.length;i++){int value=0;for(int shift=0;shift<=16;shift+=8){int channel=(int)Math.round((pixels[i]>>shift&255)*brightness);channel=(int)Math.round(channel*(1-tint)+(color>>shift&255)*tint);value|=Math.clamp(channel,0,255)<<shift;}pixels[i]=value;}
        int radius=new int[]{0,2,4,8}[options.blur];if(radius>0){for(int pass=0;pass<3;pass++){pixels=box(pixels,w,h,radius,true);pixels=box(pixels,w,h,radius,false);}}
        image.setRGB(0,0,w,h,pixels,0,w);return image;
    }
    private static int[] box(int[] source,int w,int h,int radius,boolean horizontal){
        int[] target=new int[source.length];int outer=horizontal?h:w,inner=horizontal?w:h,window=radius*2+1;
        for(int a=0;a<outer;a++){
            int red=0,green=0,blue=0;
            for(int b=-radius;b<=radius;b++){int p=source[horizontal?a*w+Math.clamp(b,0,inner-1):Math.clamp(b,0,inner-1)*w+a];red+=p>>16&255;green+=p>>8&255;blue+=p&255;}
            for(int b=0;b<inner;b++){
                target[horizontal?a*w+b:b*w+a]=(red/window)<<16|(green/window)<<8|blue/window;
                int from=source[horizontal?a*w+Math.clamp(b-radius,0,inner-1):Math.clamp(b-radius,0,inner-1)*w+a],to=source[horizontal?a*w+Math.clamp(b+radius+1,0,inner-1):Math.clamp(b+radius+1,0,inner-1)*w+a];
                red+=(to>>16&255)-(from>>16&255);green+=(to>>8&255)-(from>>8&255);blue+=(to&255)-(from&255);
            }
        }return target;
    }
}
