package dev.dsmp.economy.text;

import java.util.*;
import java.util.function.ToIntFunction;

/** Geometry shared by text rendering and mouse hit/drag handling. */
public final class TextLayout {
    public static final int LINE_HEIGHT = 14, GLYPH_HEIGHT = 12, PADDING = 4, EDITOR_TOP = 160;
    public record Glyph(String text, int x, int advance) {}
    public record Line(String text, int width, List<Glyph> glyphs) {}
    public record Measure(List<Line> lines, int width, int height) {}
    public record Bounds(int x, int y, int width, int height, float scale) {
        public boolean contains(double px, double py) { return px>=x && py>=y && px<=x+width && py<=y+height; }
    }
    private TextLayout() {}
    public static Measure measure(TextBox box, ToIntFunction<String> width) {
        var lines=new ArrayList<Line>(); int maxWidth=0;
        for(String line : box.lines()) {
            var glyphs=new ArrayList<Glyph>(); int index=0,previous=0;
            while(index<line.length()) {
                int next=line.offsetByCodePoints(index,1),end=width.applyAsInt(line.substring(0,next));
                glyphs.add(new Glyph(line.substring(index,next),previous,end-previous)); previous=end; index=next;
            }
            lines.add(new Line(line,previous,List.copyOf(glyphs))); maxWidth=Math.max(maxWidth,previous);
        }
        return new Measure(List.copyOf(lines),maxWidth,Math.max(GLYPH_HEIGHT,(lines.size()-1)*LINE_HEIGHT+GLYPH_HEIGHT));
    }
    public static Bounds bounds(TextBox box, Measure measure, int width, int height, boolean editing) {
        int top=editing?EDITOR_TOP:0,rawWidth=measure.width()+PADDING*2,rawHeight=measure.height()+PADDING*2;
        float scale=Math.min(box.size, Math.max(.01f,(width-8f)/rawWidth));
        scale=Math.min(scale,Math.max(.01f,(height-top-8f)/rawHeight));
        int w=(int)Math.ceil(rawWidth*scale),h=(int)Math.ceil(rawHeight*scale);
        return new Bounds(Math.round(box.x*Math.max(0,width-w)),top+Math.round(box.y*Math.max(0,height-top-h)),w,h,scale);
    }
    public static int lineX(TextBox box, Measure measure, Line line) { return PADDING + (box.align==1?(measure.width()-line.width())/2:box.align==2?measure.width()-line.width():0); }
    public static void drag(TextBox box, Bounds bounds, double mouseX, double mouseY, double offsetX, double offsetY, int width, int height, boolean editing) {
        int top=editing?EDITOR_TOP:0;
        box.x=(float)Math.clamp((mouseX-offsetX)/Math.max(1,width-bounds.width()),0,1);
        box.y=(float)Math.clamp((mouseY-offsetY-top)/Math.max(1,height-top-bounds.height()),0,1);
    }
}
