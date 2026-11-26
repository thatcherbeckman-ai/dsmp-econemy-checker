package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.menubackground.MenuBackgroundOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Unobstructed composition preview. Capture uses Minecraft's local screenshot folder. */
public final class CharacterPreviewScreen extends Screen {
    private final EconomyTrackerClient mod;private final Screen parent;private final MenuBackgroundOptions draft;private final boolean presentation;private int captureFrames;
    public CharacterPreviewScreen(EconomyTrackerClient mod,Screen parent,MenuBackgroundOptions source){this(mod,parent,source,false);}
    public CharacterPreviewScreen(EconomyTrackerClient mod,Screen parent,MenuBackgroundOptions source,boolean presentation){super(Component.literal(presentation?"Player background":"Character scene preview"));this.mod=mod;this.parent=parent;this.presentation=presentation;draft=source.playerView();}
    public MenuBackgroundOptions previewOptions(){return draft;}
    @Override protected void init(){if(presentation)return;var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.doneX()-16,m.actionY(),m.actionWidth()+16,m.buttonHeight(),"Save PNG",false,mod.config.menuAccent,()->captureFrames=3));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Back",true,mod.config.menuAccent,this::onClose));}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){MenuBackgroundRenderer.background(g,width,height,mod.config,draft);CharacterSceneRenderer.draw(g,0,0,width,height,draft);if(presentation)return;if(captureFrames>0){if(--captureFrames==1)Screenshot.grab(Minecraft.getInstance(),false);}else super.extractRenderState(g,mx,my,delta);}
    @Override public void onClose(){Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
