package dev.dsmp.economy.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import dev.dsmp.economy.menubackground.MenuBackgrounds;

/** Native interaction/narration with restrained selection and a brief hover transition. */
public final class SettingsButton extends Button {
    private final boolean selected;private final int accent;private final int cellX,cellY,cellHeight;
    private float hover;private long lastFrame=System.nanoTime();private boolean clipped;private boolean leading;
    private static int compactWidth(int width){return Math.min(width,Math.max(20,Math.min(240,Math.round(width*.90f))));}
    private static int compactHeight(int height){return Math.min(height,Math.max(12,Math.round(height*.82f)));}
    public SettingsButton(int x,int y,int width,int height,String label,boolean selected,int accent,Runnable action){
        super(x+(width-compactWidth(width))/2,y+(height-compactHeight(height))/2,compactWidth(width),compactHeight(height),Component.literal(label),b->action.run(),DEFAULT_NARRATION);this.cellX=x;this.cellY=y;this.cellHeight=height;this.selected=selected;this.accent=accent;setTooltipDelay(java.time.Duration.ofMillis(450));
    }
    public SettingsButton navigation(){leading=true;setX(cellX);setWidth(Math.min(142,getWidth()));setHeight(Math.min(18,getHeight()));setY(cellY+(cellHeight-getHeight())/2);return this;}
    public dev.dsmp.economy.design.FullLabel.Fit labelLayout(){return FestiveUi.labelLayout(getMessage().getString(),getWidth(),getHeight(),selected||getMessage().getString().equals("Save")||getMessage().getString().equals("Done"),leading);}
    public boolean fullLabelFits(){var fit=labelLayout();boolean bold=selected||getMessage().getString().equals("Save")||getMessage().getString().equals("Done");int padding=leading?28:getWidth()<60?8:14;for(String line:fit.lines())if(MenuText.width(line,bold)*fit.scale()>Math.max(1,getWidth()-padding)+.1)return false;return fit.lines().size()*fit.lineHeight()*fit.scale()<=Math.max(1,getHeight()-4)+.1;}
    @Override public void playDownSound(net.minecraft.client.sounds.SoundManager sounds){
        if(net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof WindowedScreen&&(leading||java.util.Set.of("Scenes","Look","Motion","Custom","Colors","Themes","Fonts","Gradient","Effects","Spacing","Presets","Scenery","Player","Text","Layout","Light","Profiles","Backups","Access","Search","Row","Image").contains(getMessage().getString())))return;
        super.playDownSound(sounds);
    }
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta){
        long now=System.nanoTime();float target=isHovered()?1:0;float elapsed=Math.min(.1f,(now-lastFrame)/1e9f);lastFrame=now;
        hover=MenuBackgrounds.visible().animations&&!dev.dsmp.economy.design.DesignAccess.reduced()?hover+(target-hover)*(1-(float)Math.exp(-elapsed*18)):target;
        float scale=getHeight()>=24?.92f:.82f;
        var layout=labelLayout();boolean truncated=layout.scale()<scale*.75f;
        if(truncated!=clipped){clipped=truncated;setTooltip(truncated?Tooltip.create(getMessage()):null);}
        FestiveUi.control(g,getX(),getY(),getWidth(),getHeight(),getMessage().getString(),selected,hover,isFocused(),isActive(),accent,leading);
    }
}
