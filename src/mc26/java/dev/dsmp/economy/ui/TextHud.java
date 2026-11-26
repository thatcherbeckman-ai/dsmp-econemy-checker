package dev.dsmp.economy.ui;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.text.*;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.*;

/** Text boxes draw after the normal panels, independent of their rotation. */
public final class TextHud {
    private record Cached(String text,int font,TextLayout.Measure measure) {}
    private static final Map<TextBox,Cached> CACHE=Collections.synchronizedMap(new WeakHashMap<>());
    private TextHud() {}
    private static int width(String text,int font) { return TextFonts.width(text,font); }
    private static net.minecraft.network.chat.Component component(String text,int font) { return TextFonts.text(text,font); }
    public static TextLayout.Measure measure(TextBox box) {
        if(!box.lineStyles.isEmpty()||TextTypography.active(box)){var layout=StyledTextHud.layout(box);return new TextLayout.Measure(java.util.List.of(),layout.width(),layout.height());}
        Cached cached=CACHE.get(box);
        if(cached==null||!cached.text().equals(box.text)||cached.font()!=box.font) {
            cached=new Cached(box.text,box.font,TextLayout.measure(box,s->width(s,box.font))); CACHE.put(box,cached);
        }
        return cached.measure();
    }
    public static TextLayout.Bounds bounds(TextBox box,int width,int height,boolean editing) { return TextLayout.bounds(box,measure(box),width,height,editing); }
    public static void draw(net.minecraft.client.gui.GuiGraphicsExtractor graphics,TrackerConfig config,int width,int height,boolean editing) {
        for(var box:config.textBoxes)if(box.visible||editing)drawBox(graphics,box,width,height,editing);
    }
    private static void drawBox(net.minecraft.client.gui.GuiGraphicsExtractor graphics,TextBox box,int width,int height,boolean editing) {
        if(!box.lineStyles.isEmpty()||TextTypography.active(box)){StyledTextHud.draw(graphics,box,width,height,editing);return;}
        var measure=measure(box); var bounds=TextLayout.bounds(box,measure,width,height,editing);
        background(graphics,box,bounds);
        net.minecraft.client.gui.Font font=net.minecraft.client.Minecraft.getInstance().font;
        for(int lineIndex=0;lineIndex<measure.lines().size();lineIndex++) {
            var line=measure.lines().get(lineIndex); int x=TextLayout.lineX(box,measure,line),y=TextLayout.PADDING+lineIndex*TextLayout.LINE_HEIGHT;
            if(box.gradient==TextBox.DOWN) {
                for(int band=0;band<TextLayout.GLYPH_HEIGHT;band++) {
                    int y1=(int)Math.floor(bounds.y()+(y+band)*bounds.scale()),y2=(int)Math.ceil(bounds.y()+(y+band+1)*bounds.scale());
                    int color=TextBox.blend(box.startColor,box.endColor,(lineIndex*TextLayout.LINE_HEIGHT+band)/(double)Math.max(1,measure.height()-1));
                    graphics.enableScissor(bounds.x(),y1,bounds.x()+bounds.width(),y2);
                    try { push(graphics,bounds); graphics.text(font,component(line.text(),box.font),x,y,opacity(color,box.visible),box.shadow); }
                    finally { graphics.pose().popMatrix(); graphics.disableScissor(); }
                }
            } else {
                push(graphics,bounds);
                try {
                    if(box.gradient==TextBox.SOLID) graphics.text(font,component(line.text(),box.font),x,y,opacity(0xFF000000|box.startColor,box.visible),box.shadow);
                    else {
                        double first=line.glyphs().isEmpty()?0:line.glyphs().getFirst().advance()/2d;
                        double last=line.glyphs().isEmpty()?0:line.glyphs().getLast().x()+line.glyphs().getLast().advance()/2d;
                        for(var glyph:line.glyphs()) {
                            double fraction=last>first?(glyph.x()+glyph.advance()/2d-first)/(last-first):0;
                            graphics.text(font,component(glyph.text(),box.font),x+glyph.x(),y,opacity(TextBox.blend(box.startColor,box.endColor,fraction),box.visible),box.shadow);
                        }
                    }
                } finally { graphics.pose().popMatrix(); }
            }
        }
    }
    static void background(net.minecraft.client.gui.GuiGraphicsExtractor graphics,TextBox box,TextLayout.Bounds bounds){
        if(box.backgroundStyle!=0&&box.backgroundOpacity>0) {
            int alpha=box.visible?box.backgroundOpacity:box.backgroundOpacity/3;
            if(box.backgroundStyle==1) {
                GlassPane.fill(graphics,bounds.x(),bounds.y(),bounds.width(),bounds.height(),alpha<<24|(box.customBackground?box.backgroundColor:0x17373D));
                GlassPane.border(graphics,bounds.x(),bounds.y(),bounds.width(),bounds.height(),(box.visible?180:60)<<24);
            } else UiSurfaces.card(graphics,bounds.x(),bounds.y(),bounds.width(),bounds.height(),alpha<<24|(box.customBackground?box.backgroundColor:0x0B1018));
        }
    }
    private static int opacity(int color,boolean visible) { return visible?color:color&0xFFFFFF|0x55000000; }
    private static void push(net.minecraft.client.gui.GuiGraphicsExtractor graphics,TextLayout.Bounds bounds) {
        var pose=graphics.pose();pose.pushMatrix();pose.translate(bounds.x(),bounds.y());pose.scale(bounds.scale());
    }
    public static void outline(net.minecraft.client.gui.GuiGraphicsExtractor graphics,TextBox box,int width,int height) {
        if(box==null)return;var b=bounds(box,width,height,true);int color=0xFFE3B0CB;
        graphics.fill(b.x(),b.y(),b.x()+b.width(),b.y()+1,color);
        graphics.fill(b.x(),b.y()+b.height()-1,b.x()+b.width(),b.y()+b.height(),color);
        graphics.fill(b.x(),b.y(),b.x()+1,b.y()+b.height(),color);
        graphics.fill(b.x()+b.width()-1,b.y(),b.x()+b.width(),b.y()+b.height(),color);
    }
}
