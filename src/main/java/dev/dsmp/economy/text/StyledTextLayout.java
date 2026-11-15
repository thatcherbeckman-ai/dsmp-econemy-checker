package dev.dsmp.economy.text;

import java.util.*;
import java.util.function.ToIntBiFunction;

/** One source of line heights/positions for styled HUD drawing, fitting and drag bounds. */
public final class StyledTextLayout {
    public record Row(int index,String text,LineStyle style,int font,int gradient,int start,int end,boolean outline,float size,int x,int y,int width,int height,int textX,int textY,int noteX,int imageX,int imageY){}
    public record Layout(List<Row> rows,int width,int height){}
    private StyledTextLayout(){}
    public static Layout measure(TextBox box,ToIntBiFunction<String,Integer> width){
        var rows=new ArrayList<Row>();String[] lines=box.lines();int max=0,y=0;
        for(int i=0;i<lines.length;i++){
            var s=box.style(i);int font=s.custom?s.font:box.font,gradient=s.custom?s.gradient:box.gradient,start=s.custom?s.startColor:box.startColor,end=s.custom?s.endColor:box.endColor;
            int textW=(int)Math.ceil(width.applyAsInt(lines[i],font)*s.textSize),noteW=s.note.isEmpty()?0:(int)Math.ceil(width.applyAsInt(s.note,0)*.7f)+8;
            int pad=s.shape==LineStyle.PLAIN?0:8,dot=s.bullet?10:0,image=s.image.isEmpty()?0:(int)Math.ceil(s.iconSize)+6;
            int contentH=(int)Math.ceil(Math.max(TextLayout.GLYPH_HEIGHT*s.textSize,s.image.isEmpty()?0:s.iconSize));int h=contentH+(s.shape==LineStyle.PLAIN?2:8),w=pad*2+dot+image+textW+noteW;
            int textX=pad+dot+image,textY=(int)Math.round((h-TextLayout.GLYPH_HEIGHT*s.textSize)/2);
            rows.add(new Row(i,lines[i],s,font,gradient,start,end,s.custom&&s.outline,s.textSize,0,y,w,h,textX,textY,textX+textW+8,pad+dot,(h-Math.round(s.iconSize))/2));max=Math.max(max,w);y+=h+3;
        }
        var aligned=new ArrayList<Row>();for(var r:rows){int offset=box.align==1?(max-r.width)/2:box.align==2?max-r.width:0;aligned.add(new Row(r.index,r.text,r.style,r.font,r.gradient,r.start,r.end,r.outline,r.size,offset,r.y,r.width,r.height,r.textX,r.textY,r.noteX,r.imageX,r.imageY));}
        return new Layout(List.copyOf(aligned),Math.max(1,max),Math.max(1,y-3));
    }
}
