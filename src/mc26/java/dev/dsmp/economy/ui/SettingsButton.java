package dev.dsmp.economy.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import dev.dsmp.economy.menubackground.MenuBackgrounds;

/** Native interaction/narration with restrained selection and a brief hover transition. */
public final class SettingsButton extends Button {
    private final boolean selected;private final int accent;
    private float hover;private long lastFrame=System.nanoTime();private boolean clipped;private boolean leading;
    public SettingsButton(int x,int y,int width,int height,String label,boolean selected,int accent,Runnable action){
        super(x,y,width,height,Component.literal(label),b->action.run(),DEFAULT_NARRATION);this.selected=selected;this.accent=accent;setTooltipDelay(java.time.Duration.ofMillis(450));
    }
    public SettingsButton navigation(){leading=true;return this;}
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta){
        long now=System.nanoTime();float target=isHovered()?1:0;float elapsed=Math.min(.1f,(now-lastFrame)/1e9f);lastFrame=now;
        hover=MenuBackgrounds.visible().animations?hover+(target-hover)*(1-(float)Math.exp(-elapsed*18)):target;
        boolean bold=selected||getMessage().getString().equals("Done")||getMessage().getString().equals("Save");float scale=getHeight()>=24?.98f:.88f;
        boolean truncated=MenuText.width(getMessage().getString(),bold)*scale>getWidth()-(getWidth()<60?10:20);
        if(truncated!=clipped){clipped=truncated;setTooltip(truncated?Tooltip.create(getMessage()):null);}
        FestiveUi.control(g,getX(),getY(),getWidth(),getHeight(),getMessage().getString(),selected,hover,isFocused(),isActive(),accent,leading);
    }
}
