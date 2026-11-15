package dev.dsmp.economy.ui;

import dev.dsmp.economy.text.*;
import net.minecraft.*;

/** Rounded line rows, inline images/notes, bubble outlines and independent gradients. */
final class StyledTextHud {
    private StyledTextHud(){}
    static StyledTextLayout.Layout layout(TextBox box){return StyledTextLayout.measure(box,TextFonts::width);}
    static void draw(class_332 g,TextBox box,int width,int height,boolean editing){var layout=layout(box);var measure=new TextLayout.Measure(java.util.List.of(),layout.width(),layout.height());drawAt(g,box,layout,TextLayout.bounds(box,measure,width,height,editing));}
    static void preview(class_332 g,TextBox box,int x,int y,int width,int height){if(height<1||width<1)return;var layout=layout(box);float scale=Math.min(box.size,Math.min(width/(float)(layout.width()+8),height/(float)(layout.height()+8)));drawAt(g,box,layout,new TextLayout.Bounds(x,y,(int)Math.ceil((layout.width()+8)*scale),(int)Math.ceil((layout.height()+8)*scale),scale));}
    private static void drawAt(class_332 g,TextBox box,StyledTextLayout.Layout layout,TextLayout.Bounds b){
        TextHud.background(g,box,b);var pose=g.method_51448();pose.pushMatrix();pose.translate(b.x(),b.y());pose.scale(b.scale());pose.translate(4,4);
        try{for(var row:layout.rows()){
            var s=row.style();if(s.shape!=LineStyle.PLAIN&&s.backgroundOpacity>0)rounded(g,0,row.y(),layout.width(),row.height(),s.shape==LineStyle.PILL?row.height()/2:3,alpha(s.backgroundOpacity,box.visible)<<24|s.backgroundColor);
            if(s.bullet)rounded(g,row.x()+7,row.y()+(row.height()-5)/2,5,5,2,opacity(s.bulletColor,box.visible));
            if(!s.image.isEmpty())TextImages.draw(g,s.image,row.x()+row.imageX(),row.y()+row.imageY(),Math.round(s.iconSize),opacity(0xFFFFFF,box.visible));
            int x=row.x()+row.textX(),y=row.y()+row.textY();
            if(row.outline()){pose.pushMatrix();pose.translate(x,y);pose.scale(row.size());try{for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(dx!=0||dy!=0){pose.pushMatrix();pose.translate(dx*.6f,dy*.6f);try{text(g,row.text(),row.font(),0,0,opacity(s.outlineColor,box.visible),false);}finally{pose.popMatrix();}}}finally{pose.popMatrix();}}
            if(row.gradient()==TextBox.DOWN){for(int band=0;band<TextLayout.GLYPH_HEIGHT;band++){
                int top=(int)Math.floor(b.y()+(4+y+band*row.size())*b.scale()),bottom=(int)Math.ceil(b.y()+(4+y+(band+1)*row.size())*b.scale());pose.pushMatrix();try{pose.identity();g.method_44379(b.x(),top,b.x()+b.width(),bottom);}finally{pose.popMatrix();}
                try{pose.pushMatrix();pose.translate(x,y);pose.scale(row.size());text(g,row.text(),row.font(),0,0,opacity(TextBox.blend(row.start(),row.end(),band/(double)(TextLayout.GLYPH_HEIGHT-1)),box.visible),box.shadow);}
                finally{pose.popMatrix();g.method_44380();}
            }}else{pose.pushMatrix();pose.translate(x,y);pose.scale(row.size());try{
                if(row.gradient()==TextBox.SOLID)text(g,row.text(),row.font(),0,0,opacity(row.start(),box.visible),box.shadow);
                else{int count=row.text().codePointCount(0,row.text().length()),at=0,index=0;while(at<row.text().length()){int next=row.text().offsetByCodePoints(at,1),advance=TextFonts.width(row.text().substring(0,at),row.font());text(g,row.text().substring(at,next),row.font(),advance,0,opacity(TextBox.blend(row.start(),row.end(),index/(double)Math.max(1,count-1)),box.visible),box.shadow);at=next;index++;}}
            }finally{pose.popMatrix();}}
            if(!s.note.isEmpty()){pose.pushMatrix();pose.translate(row.x()+row.noteX(),row.y()+(row.height()-8)/2f);pose.scale(.7f);try{text(g,s.note,0,0,0,opacity(s.noteColor,box.visible),false);}finally{pose.popMatrix();}}
        }}finally{pose.popMatrix();}
    }
    private static int alpha(int value,boolean visible){return visible?value:value/3;}
    private static int opacity(int color,boolean visible){return (visible?0xFF000000:0x55000000)|color&0xFFFFFF;}
    private static void text(class_332 g,String text,int font,int x,int y,int color,boolean shadow){g.method_51439(class_310.method_1551().field_1772,TextFonts.text(text,font),x,y,color,shadow);}
    static void rounded(class_332 g,int x,int y,int w,int h,int radius,int color){radius=Math.min(radius,Math.min(w,h)/2);for(int row=0;row<h;row++){int inset=0;if(row<radius){double dy=radius-row-.5;inset=(int)Math.ceil(radius-Math.sqrt(Math.max(0,radius*radius-dy*dy))-.5);}else if(row>=h-radius){double dy=row-(h-radius)+.5;inset=(int)Math.ceil(radius-Math.sqrt(Math.max(0,radius*radius-dy*dy))-.5);}g.method_25294(x+inset,y+row,x+w-inset,y+row+1,color);}}
}
