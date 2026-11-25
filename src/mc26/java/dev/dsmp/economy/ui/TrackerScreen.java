package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.loans.LoanService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

/** Four spaced toolbar rows above the draggable HUD canvas. */
public final class TrackerScreen extends Screen {
    private final EconomyTrackerClient mod;
    private final Ledger sample=Ledger.demo();
    private boolean preview=true;
    private int tab,dragging=-1;
    private double offsetX,offsetY;
    private String notice="Drag a box to move it. Done or Esc saves.";
    public TrackerScreen(EconomyTrackerClient mod){super(Component.literal(MenuLayout.BRAND+" / HUD layout"));this.mod=mod;}
    private int canvasHeight(){return height-24;}
    private void refresh(Runnable action){action.run();rebuildWidgets();}
    @Override protected void init(){
        var m=MenuLayout.of(width,height);
        addRenderableWidget(new SettingsButton(m.cancelX(),8,m.actionWidth(),22,"Done",true,mod.config.menuAccent,this::onClose));
        String[] tabs={"Layout","Trackers","Widgets","Style"};
        for(int i=0;i<4;i++){int target=i;grid(i,4,0,tabs[i],tab==i,()->{if(target==3)Minecraft.getInstance().gui.setScreen(new AppearanceScreen(mod,this));else refresh(()->tab=target);});}
        if(tab==0){
            grid(0,4,1,"Scale -",false,()->mod.config.scale=Math.max(.4f,mod.config.scale-.1f));
            grid(1,4,1,"Scale +",false,()->mod.config.scale=Math.min(1.5f,mod.config.scale+.1f));
            grid(2,4,1,"Reset layout",false,()->{var defaults=new TrackerConfig();for(int i=0;i<3;i++){mod.config.box(i).x=defaults.box(i).x;mod.config.box(i).y=defaults.box(i).y;}mod.config.scale=defaults.scale;});
            grid(3,4,1,"Glass",false,()->mod.config.opacity=mod.config.opacity>=230?80:mod.config.opacity+30);
        }else if(tab==1){
            String[] names={"Nets","Bets","Profit"};for(int i=0;i<3;i++){int id=i;grid(i,4,1,names[i]+(mod.config.panels[i].visible?": on":": off"),mod.config.panels[i].visible,()->refresh(()->mod.config.panels[id].visible=!mod.config.panels[id].visible));}
            grid(3,4,1,LoanService.toggleLabel(mod.config.loansVisible),mod.config.loansVisible,()->refresh(()->mod.config.loansVisible=!mod.config.loansVisible));
        }else{
            grid(0,3,1,"Rakeback: "+(mod.config.rakebackPanel.visible?"on":"off"),mod.config.rakebackPanel.visible,()->refresh(()->mod.config.rakebackPanel.visible=!mod.config.rakebackPanel.visible));
            grid(1,3,1,"Balance: "+(mod.config.balancePanel.visible?"on":"off"),mod.config.balancePanel.visible,()->refresh(()->mod.config.balancePanel.visible=!mod.config.balancePanel.visible));
            grid(2,3,1,"Read bal",false,mod::syncBalance);
        }
        grid(0,3,2,preview?"Sample preview":"Your records",preview,()->refresh(()->preview=!preview));
        grid(1,3,2,"Reload settings",false,()->refresh(()->{mod.reloadConfig();notice=mod.status;}));
        if(tab==1)grid(2,3,2,"Read bal: "+(mod.config.scoreboardBalance?"on":"off"),mod.config.scoreboardBalance,()->refresh(()->mod.config.scoreboardBalance=!mod.config.scoreboardBalance));
        if(tab==2)grid(2,3,2,"Text boxes",false,()->Minecraft.getInstance().gui.setScreen(new TextBoxesScreen(mod,this)));
        grid(0,3,3,"Next view",false,()->mod.viewCycle.next(mod.config.enabledViews(),System.nanoTime()/1000000L));
        grid(1,3,3,mod.config.autoCycle?"Cycle: 8s":"Cycle: paused",mod.config.autoCycle,()->refresh(()->mod.config.autoCycle=!mod.config.autoCycle));
        grid(2,3,3,"Auto bets: "+(mod.config.autoDetectPayments?"on":"off"),mod.config.autoDetectPayments,()->refresh(()->mod.config.autoDetectPayments=!mod.config.autoDetectPayments));
    }
    private void grid(int col,int cols,int row,String label,boolean selected,Runnable action){var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.x(col,cols),42+row*30,m.cellWidth(col,cols),22,label,selected,mod.config.menuAccent,action));}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        FestiveUi.background(g,width,height,mod.config);var m=MenuLayout.of(width,height);
        int room=m.cancelX()-m.margin()-8;float scale=Math.min(.9f,room/(float)MenuText.width(MenuLayout.BRAND,true));FestiveUi.text(g,MenuLayout.BRAND,m.margin(),12,0xFFF2EDF9,scale,true);
        TrackerRenderer.drawAll(g,mod.config,preview?sample:mod.ledger,mod.viewCycle,width,canvasHeight(),true,preview,mod.wallet.estimated());
        if(dragging>=0){var b=TrackerRenderer.bounds(mod.config,dragging,width,canvasHeight(),true);g.fill(b.x(),b.y(),b.x()+b.width(),b.y()+1,0xFFE3B0CB);}
        m.footer(g,notice);super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        if(super.mouseClicked(event,doubleClick))return true;
        if(event.button()!=InputConstants.MOUSE_BUTTON_LEFT||event.y()<160||event.y()>=canvasHeight())return false;
        for(int id=2;id>=0;id--){var b=TrackerRenderer.bounds(mod.config,id,width,canvasHeight(),true);if(!mod.config.boxVisible(id)||!b.contains(event.x(),event.y()))continue;dragging=id;offsetX=event.x()-b.x();offsetY=event.y()-b.y();return true;}return false;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
        if(dragging<0)return super.mouseDragged(event,dx,dy);var b=TrackerRenderer.bounds(mod.config,dragging,width,canvasHeight(),true);
        mod.config.box(dragging).x=(float)Math.clamp((event.x()-offsetX)/Math.max(1,width-b.width()),0,1);
        mod.config.box(dragging).y=(float)Math.clamp((event.y()-offsetY-160)/Math.max(1,canvasHeight()-160-b.height()),0,1);return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){if(dragging>=0){dragging=-1;return true;}return super.mouseReleased(event);}
    @Override public void onClose(){if(mod.saveConfig())DashboardNavigation.open(mod);else{notice="Save failed. Retry Done or Esc.";}}
    @Override public boolean isPauseScreen(){return false;}
}
