package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.privacy.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Off controls are separate from balance reading and preserve all existing history. */
public final class TrackingControlsScreen extends WindowedScreen {
    private final EconomyTrackerClient mod;private final Screen parent;private final TrackingOptions draft=TrackingControls.current();
    private String notice="Save applies these changes. Cancel keeps your current controls.";
    public TrackingControlsScreen(EconomyTrackerClient mod,Screen parent){super(Component.literal(MenuLayout.BRAND+" / Tracking controls"));this.mod=mod;this.parent=parent;}
    private void off(int row,String label,boolean disabled,Runnable action){var m=MenuLayout.of(width,height);var button=new SettingsButton(m.margin(),m.row(row),m.contentWidth(),m.buttonHeight(),label,disabled,mod.config.menuAccent,()->{action.run();rebuildWidgets();});button.active=!disabled;addRenderableWidget(button);}
    @Override protected void initMenu(){var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Save",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,()->Minecraft.getInstance().gui.setScreen(parent)));
        off(1,draft.betRecordingOff?"Bet recording: off":"Turn off bet recording",draft.betRecordingOff,()->draft.betRecordingOff=true);
        off(3,draft.profitHidden?"Profit: hidden":"Hide profit",draft.profitHidden,()->draft.profitHidden=true);
        off(5,draft.playerResultsHidden?"Player results: hidden":"Hide player results",draft.playerResultsHidden,()->draft.playerResultsHidden=true);
        off(6,"Turn off all results",draft.betRecordingOff&&draft.profitHidden&&draft.playerResultsHidden,draft::turnOffAll);
    }
    @Override protected void drawMenu(GuiGraphicsExtractor g,int mx,int my,float delta){var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config,false);m.header(g,"Tracking controls / recording and visibility");FestiveUi.surface(g,m.margin()-5,m.top()-4,m.contentWidth()+10,m.row(7)-m.top(),0xEE0B1422);
        FestiveUi.textFit(g,"Stops new bet records. Balance reading remains separate.",m.margin()+8,m.row(0)+5,m.contentWidth()-16,0xFFE3EAF4,.75f,false);
        FestiveUi.textFit(g,"Hide profit totals and result-related In/Out figures in the HUD and overview.",m.margin()+8,m.row(2)+5,m.contentWidth()-16,0xFFCED9EB,.7f,false);
        FestiveUi.textFit(g,"Hide player nets, win/loss lists, recent results and rakeback.",m.margin()+8,m.row(4)+5,m.contentWidth()-16,0xFFCED9EB,.7f,false);
        m.footer(g,TrackingControls.error()==null?notice:TrackingControls.error());super.drawMenu(g,mx,my,delta);}
    @Override public void onClose(){try{TrackingControls.save(draft);mod.clearResultIntents();Minecraft.getInstance().gui.setScreen(parent);}catch(Exception ex){notice=ex.getMessage();}}
    @Override public boolean isPauseScreen(){return false;}
}
