package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

/** A full-window draft canvas, including on small GUI sizes; Back never saves the draft. */
public final class SceneCanvasScreen extends Screen {
    private final EconomyTrackerClient mod;private final CharacterSceneScreen parent;private final SceneDragController drag;
    public SceneCanvasScreen(EconomyTrackerClient mod,CharacterSceneScreen parent){super(Component.literal("Drag character and captions"));this.mod=mod;this.parent=parent;drag=new SceneDragController(parent::previewOptions,before->parent.recordDrag(before));}
    private SceneDragController.Viewport viewport(){return new SceneDragController.Viewport(0,0,width,height);}
    @Override protected void init(){var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Back",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Undo",false,mod.config.menuAccent,()->parent.undo(false)));addRenderableWidget(new SettingsButton(m.doneX()-m.actionWidth()-m.gap(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Redo",false,mod.config.menuAccent,()->parent.undo(true)));}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){var draft=parent.previewOptions().copy();draft.character.enabled=true;MenuBackgroundRenderer.background(g,width,height,mod.config,draft);CharacterSceneRenderer.draw(g,0,0,width,height,draft);drag.guides(g,viewport());g.fill(0,0,width,Math.max(34,MenuLayout.of(width,height).actionY()+MenuLayout.of(width,height).buttonHeight()+4),0x9C07101B);MenuLayout.of(width,height).footer(g,"Drag your character or captions. Guides snap nearby positions. Back returns to editing.");super.extractRenderState(g,mx,my,delta);}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubled){if(super.mouseClicked(event,doubled))return true;return event.button()==InputConstants.MOUSE_BUTTON_LEFT&&drag.click(viewport(),event.x(),event.y());}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){return drag.drag(viewport(),event.x(),event.y())||super.mouseDragged(event,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent event){return drag.release()||super.mouseReleased(event);}
    @Override public void onClose(){drag.release();Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
