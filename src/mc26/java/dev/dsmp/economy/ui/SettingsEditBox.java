package dev.dsmp.economy.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Retains native text layout, selection, cursor and hit testing; replaces only the field frame. */
public final class SettingsEditBox extends EditBox {
    private boolean drawingText;
    public SettingsEditBox(Font font,int x,int y,int width,int height,Component label){super(font,x,y,width,height,label);setTextColor(0xFFE5ECF8);setTextColorUneditable(0xFF8390A6);setTextShadow(false);}
    @Override public boolean isBordered(){return !drawingText&&super.isBordered();}
    @Override public int getInnerWidth(){return Math.max(1,getWidth()-8);}
    @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        if(!visible)return;
        UiSurfaces.fill(g,getX(),getY(),getWidth(),getHeight(),dev.dsmp.economy.design.DesignAccess.contrast()?0xFF020812:(FestiveUi.windowed()?0xF5161016:0xF30D1521));
        UiSurfaces.outline(g,getX(),getY(),getWidth(),getHeight(),dev.dsmp.economy.design.DesignAccess.contrast()?0xFFE2EBFF:isFocused()?0xE0000000|FestiveUi.accent():(FestiveUi.windowed()?0x805E3F47:0x8059677D));
        drawingText=true;try{super.extractWidgetRenderState(g,mx,my,delta);}finally{drawingText=false;}
    }
}
