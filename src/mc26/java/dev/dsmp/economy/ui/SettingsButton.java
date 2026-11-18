package dev.dsmp.economy.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/** Uses the native button's focus, keyboard, narration and click handling with our menu skin. */
public final class SettingsButton extends Button {
    private final boolean selected;
    private final int accent;
    public SettingsButton(int x,int y,int width,int height,String label,boolean selected,int accent,Runnable action){
        super(x,y,width,height,Component.literal(label),b->action.run(),DEFAULT_NARRATION);
        this.selected=selected;this.accent=accent;
        setTooltip(Tooltip.create(Component.literal(label)));
    }
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta){
        FestiveUi.control(g,getX(),getY(),getWidth(),getHeight(),getMessage().getString(),selected,isHoveredOrFocused(),accent);
    }
}
