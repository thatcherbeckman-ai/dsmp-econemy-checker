package dev.dsmp.economy.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Iris sets its parent screen directly. Restore the original embedded page on the next UI tick. */
public final class IrisReturnScreen extends Screen {
    private final Screen parent;private boolean returned;
    public IrisReturnScreen(Screen parent){super(Component.literal("Returning to Shaders"));this.parent=parent;}
    @Override public void tick(){if(!returned&&minecraft.gui.screen()==this){returned=true;MenuNavigation.open(parent);}}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){g.fill(0,0,width,height,0x40000000);}
    @Override public boolean isPauseScreen(){return false;}
}
