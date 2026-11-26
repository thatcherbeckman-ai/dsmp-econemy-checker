package dev.dsmp.economy.ui;

import dev.dsmp.economy.text.*;

/** One measured layout for styled text, effects, gradients, live previews and HUD bounds. */
final class StyledTextHud {
    private StyledTextHud(){}
    static StyledTextLayout.Layout layout(TextBox box){return StyledTextLayout.measure(box,TextFonts::width);}
    static void draw(net.minecraft.client.gui.GuiGraphicsExtractor g,TextBox box,int width,int height,boolean editing){var l=layout(box);drawAt(g,box,l,TextLayout.bounds(box,new TextLayout.Measure(java.util.List.of(),l.width(),l.height()),width,height,editing));}
    static void preview(net.minecraft.client.gui.GuiGraphicsExtractor g,TextBox box,int x,int y,int width,int height){if(width<1||height<1)return;var l=layout(box);float scale=Math.min(box.size,Math.min(width/(float)(l.width()+8),height/(float)(l.height()+8)));drawAt(g,box,l,new TextLayout.Bounds(x,y,(int)Math.ceil((l.width()+8)*scale),(int)Math.ceil((l.height()+8)*scale),scale));}
    private static void drawAt(net.minecraft.client.gui.GuiGraphicsExtractor g,TextBox box,StyledTextLayout.Layout layout,TextLayout.Bounds bounds){
        TextHud.background(g,box,bounds);var pose=g.pose();pose.pushMatrix();pose.translate(bounds.x(),bounds.y());pose.scale(bounds.scale());pose.translate(4,4);
        try{for(var row:layout.rows()){
            var s=row.style();if(s.shape!=LineStyle.PLAIN&&s.backgroundOpacity>0)rounded(g,0,row.y(),layout.width(),row.height(),s.shape==LineStyle.PILL?row.height()/2:3,(box.visible?s.backgroundOpacity:s.backgroundOpacity/3)<<24|s.backgroundColor);
            if(box.textOpacity==0)continue;
            if(s.bullet)rounded(g,row.x()+7,row.y()+(row.height()-5)/2,5,5,2,color(s.bulletColor,box));
            if(!s.image.isEmpty())TextImages.draw(g,s.image,row.x()+row.imageX(),row.y()+row.imageY(),Math.round(s.iconSize),color(0xFFFFFF,box));
            pose.pushMatrix();pose.translate(row.x()+row.textX(),row.y()+row.textY());pose.scale(row.size());
            try{
                if(box.shadow){pose.pushMatrix();pose.translate(box.shadowOffset,box.shadowOffset);try{run(g,box,row.text(),row.font(),color(box.shadowColor,box),false,row.start(),row.end());}finally{pose.popMatrix();}}
                if(row.outline()){float distance=box.outline?box.outlineWidth:.6f;int rgb=box.outline?box.outlineColor:s.outlineColor;for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(dx!=0||dy!=0){pose.pushMatrix();pose.translate(dx*distance,dy*distance);try{run(g,box,row.text(),row.font(),color(rgb,box),false,row.start(),row.end());}finally{pose.popMatrix();}}}
                if(row.gradient()==TextBox.DOWN){int width=(int)Math.ceil(TextTypography.width(box,row.text(),row.font(),TextFonts::width))+3;for(int band=0;band<TextLayout.GLYPH_HEIGHT;band++){g.enableScissor(-2,band,width,band+1);try{run(g,box,row.text(),row.font(),color(TextBox.blend(row.start(),row.end(),band/(double)(TextLayout.GLYPH_HEIGHT-1)),box),false,row.start(),row.end());}finally{g.disableScissor();}}}
                else run(g,box,row.text(),row.font(),color(row.start(),box),row.gradient()==TextBox.ACROSS,row.start(),row.end());
            }finally{pose.popMatrix();}
            if(!s.note.isEmpty()){pose.pushMatrix();pose.translate(row.x()+row.noteX(),row.y()+(row.height()-8)/2f);pose.scale(.7f);try{g.text(net.minecraft.client.Minecraft.getInstance().font,TextFonts.text(s.note,0),0,0,color(s.noteColor,box),false);}finally{pose.popMatrix();}}
        }}finally{pose.popMatrix();}
    }
    private static void run(net.minecraft.client.gui.GuiGraphicsExtractor g,TextBox box,String text,int font,int solid,boolean gradient,int start,int end){
        if(box.letterSpacing==0&&!gradient){g.text(net.minecraft.client.Minecraft.getInstance().font,component(text,font,box),0,0,solid,false);return;}
        int at=0,index=0,count=text.codePointCount(0,text.length());while(at<text.length()){int next=text.offsetByCodePoints(at,1);String prefix=text.substring(0,at);float x=TextFonts.width(prefix,font)+(box.bold&&font>=2?index:0)+index*box.letterSpacing;int tint=gradient?color(TextBox.blend(start,end,index/(double)Math.max(1,count-1)),box):solid;g.text(net.minecraft.client.Minecraft.getInstance().font,component(text.substring(at,next),font,box),Math.round(x),0,tint,false);at=next;index++;}
    }
    private static net.minecraft.network.chat.Component component(String text,int font,TextBox box){return TextFonts.text(text,font).copy().withStyle(style->style.withItalic(box.italic).withBold(box.bold&&font>=2));}
    private static int color(int rgb,TextBox box){int alpha=box.visible?box.textOpacity:box.textOpacity/3;return (alpha==0?0:Math.max(4,alpha))<<24|rgb&0xFFFFFF;}
    static void rounded(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int w,int h,int radius,int color){radius=Math.min(radius,Math.min(w,h)/2);for(int row=0;row<h;row++){int inset=0;if(row<radius){double dy=radius-row-.5;inset=(int)Math.ceil(radius-Math.sqrt(Math.max(0,radius*radius-dy*dy))-.5);}else if(row>=h-radius){double dy=row-(h-radius)+.5;inset=(int)Math.ceil(radius-Math.sqrt(Math.max(0,radius*radius-dy*dy))-.5);}g.fill(x+inset,y+row,x+w-inset,y+row+1,color);}}
}
