package dev.dsmp.economy.ui;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.text.*;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import java.util.*;

/** Text boxes draw after the normal panels, independent of their rotation. */
public final class TextHud {
    private record Cached(String text,int font,TextLayout.Measure measure) {}
    private static final Map<TextBox,Cached> CACHE=Collections.synchronizedMap(new WeakHashMap<>());
    private TextHud() {}
    private static int width(String text,int font) { return font==2?class_310.method_1551().field_1772.method_1727(text):HudFont.width(text,font==1); }
    private static class_2561 component(String text,int font) { return font==2?class_2561.method_43470(text):HudFont.text(text,font==1); }
    public static TextLayout.Measure measure(TextBox box) {
        Cached cached=CACHE.get(box);
        if(cached==null||!cached.text().equals(box.text)||cached.font()!=box.font) {
            cached=new Cached(box.text,box.font,TextLayout.measure(box,s->width(s,box.font))); CACHE.put(box,cached);
        }
        return cached.measure();
    }
    public static TextLayout.Bounds bounds(TextBox box,int width,int height,boolean editing) { return TextLayout.bounds(box,measure(box),width,height,editing); }
    public static void draw(class_332 graphics,TrackerConfig config,int width,int height,boolean editing) {
        for(var box:config.textBoxes)if(box.visible||editing)drawBox(graphics,box,width,height,editing);
        AuctionHud.draw(graphics,width,height,editing);
    }
    private static void drawBox(class_332 graphics,TextBox box,int width,int height,boolean editing) {
        var measure=measure(box); var bounds=TextLayout.bounds(box,measure,width,height,editing);
        if(box.backgroundStyle!=0&&box.backgroundOpacity>0) {
            int alpha=box.visible?box.backgroundOpacity:box.backgroundOpacity/3;
            if(box.backgroundStyle==1) {
                GlassPane.fill(graphics,bounds.x(),bounds.y(),bounds.width(),bounds.height(),alpha<<24|0x17373D);
                GlassPane.border(graphics,bounds.x(),bounds.y(),bounds.width(),bounds.height(),(box.visible?180:60)<<24);
            } else graphics.method_25294(bounds.x(),bounds.y(),bounds.x()+bounds.width(),bounds.y()+bounds.height(),alpha<<24|0x0B1018);
        }
        class_327 font=class_310.method_1551().field_1772;
        for(int lineIndex=0;lineIndex<measure.lines().size();lineIndex++) {
            var line=measure.lines().get(lineIndex); int x=TextLayout.lineX(box,measure,line),y=TextLayout.PADDING+lineIndex*TextLayout.LINE_HEIGHT;
            if(box.gradient==TextBox.DOWN) {
                for(int band=0;band<TextLayout.GLYPH_HEIGHT;band++) {
                    int y1=(int)Math.floor(bounds.y()+(y+band)*bounds.scale()),y2=(int)Math.ceil(bounds.y()+(y+band+1)*bounds.scale());
                    int color=TextBox.blend(box.startColor,box.endColor,(lineIndex*TextLayout.LINE_HEIGHT+band)/(double)Math.max(1,measure.height()-1));
                    graphics.method_44379(bounds.x(),y1,bounds.x()+bounds.width(),y2);
                    try { push(graphics,bounds); graphics.method_51439(font,component(line.text(),box.font),x,y,opacity(color,box.visible),box.shadow); }
                    finally { graphics.method_51448().popMatrix(); graphics.method_44380(); }
                }
            } else {
                push(graphics,bounds);
                try {
                    if(box.gradient==TextBox.SOLID) graphics.method_51439(font,component(line.text(),box.font),x,y,opacity(0xFF000000|box.startColor,box.visible),box.shadow);
                    else {
                        double first=line.glyphs().isEmpty()?0:line.glyphs().getFirst().advance()/2d;
                        double last=line.glyphs().isEmpty()?0:line.glyphs().getLast().x()+line.glyphs().getLast().advance()/2d;
                        for(var glyph:line.glyphs()) {
                            double fraction=last>first?(glyph.x()+glyph.advance()/2d-first)/(last-first):0;
                            graphics.method_51439(font,component(glyph.text(),box.font),x+glyph.x(),y,opacity(TextBox.blend(box.startColor,box.endColor,fraction),box.visible),box.shadow);
                        }
                    }
                } finally { graphics.method_51448().popMatrix(); }
            }
        }
    }
    private static int opacity(int color,boolean visible) { return visible?color:color&0xFFFFFF|0x55000000; }
    private static void push(class_332 graphics,TextLayout.Bounds bounds) {
        var pose=graphics.method_51448();pose.pushMatrix();pose.translate(bounds.x(),bounds.y());pose.scale(bounds.scale());
    }
    public static void outline(class_332 graphics,TextBox box,int width,int height) {
        if(box==null)return;var b=bounds(box,width,height,true);int color=0xFFE3B0CB;
        graphics.method_25294(b.x(),b.y(),b.x()+b.width(),b.y()+1,color);
        graphics.method_25294(b.x(),b.y()+b.height()-1,b.x()+b.width(),b.y()+b.height(),color);
        graphics.method_25294(b.x(),b.y(),b.x()+1,b.y()+b.height(),color);
        graphics.method_25294(b.x()+b.width()-1,b.y(),b.x()+b.width(),b.y()+b.height(),color);
    }
}
