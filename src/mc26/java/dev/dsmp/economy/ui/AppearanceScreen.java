package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.appearance.AppearanceEditor;
import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.core.ViewCycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The existing appearance controller with a roomy grid and a separate sample preview. */
public final class AppearanceScreen extends Screen {
    private final EconomyTrackerClient mod;
    private final Screen parent;
    private final AppearanceEditor editor;
    private final Ledger sample=Ledger.demo();
    private final ViewCycle cycle=new ViewCycle();
    public AppearanceScreen(EconomyTrackerClient mod,Screen parent){
        super(Component.literal(MenuLayout.BRAND+" / Appearance"));this.mod=mod;this.parent=parent;editor=new AppearanceEditor(mod.config);
    }
    private int formWidth(MenuLayout m){return width>=780?Math.min(460,(m.contentWidth()-m.gap()*2)/2):m.contentWidth();}
    @Override protected void init(){
        var m=MenuLayout.of(width,height);int form=formWidth(m);
        for(var c:editor.controls(this::rebuildWidgets,this::onClose,this::cancel)){
            int x,y,w;
            if(c.label().equals("Done")||c.label().equals("Cancel")){
                x=c.label().equals("Done")?m.doneX():m.cancelX();y=m.actionY();w=m.actionWidth();
            }else{
                int row=(c.y()-28)/24,col=Math.clamp((c.x()-10)/98,0,2),cols=3;
                if(editor.colorRole()<0&&row==0){col=0;cols=1;}
                if(editor.colorRole()<0&&row==4){col=c.x()<100?0:1;cols=2;}
                x=m.margin()+col*(form+m.gap())/cols;w=(form+m.gap())/cols-m.gap();y=m.row(row);
            }
            addRenderableWidget(new SettingsButton(x,y,w,m.buttonHeight(),c.label(),c.label().equals("Done"),mod.config.menuAccent,c.action()));
        }
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,editor.heading());
        int x=width>=780?m.margin()+formWidth(m)+m.gap()*2:m.margin();
        int y=width>=780?m.top():m.row(5)+m.gap();
        int w=width-x-m.margin(),h=height-y-36;
        if(w>0&&h>=24){
            FestiveUi.panel(g,x,y,w,h,0x625084);FestiveUi.text(g,"SAMPLE PREVIEW",x+12,y+10,0xFFB7ABC9,.7f,true);
            g.enableScissor(x+4,y+26,x+w-4,y+h-4);g.pose().pushMatrix();g.pose().translate(x+6,y+28);
            try{TrackerRenderer.drawAll(g,editor.previewConfig(),sample,cycle,w-12,h-34,false,true,false);}
            finally{g.pose().popMatrix();g.disableScissor();}
        }
        m.footer(g,editor.notice());super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){
        if(mod.saveConfig()){if(parent instanceof DashboardScreen dashboard)dashboard.checkpoint();Minecraft.getInstance().gui.setScreen(parent);}else editor.failedSave();
    }
    private void cancel(){editor.cancel();Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
