package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.shaders.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.Supplier;

/** A normal menu page with optional Iris controls; Iris owns shader pack choice, application and options. */
public final class ShaderSettingsScreen extends WindowedScreen {
    private final EconomyTrackerClient mod;private final Screen parent;private final Supplier<IrisBridge> discovery;
    private IrisBridge bridge;private IrisBridge.State state;private String notice="Iris saves shader changes. Its settings apply immediately; menu Cancel does not undo them.";
    public ShaderSettingsScreen(EconomyTrackerClient mod,Screen parent){this(mod,parent,IrisIntegration::current);}
    public ShaderSettingsScreen(EconomyTrackerClient mod,Screen parent,Supplier<IrisBridge> discovery){super(Component.literal("Shaders / Iris"));this.mod=mod;this.parent=parent;this.discovery=discovery;refresh();}
    private void refresh(){bridge=discovery.get();readState();}
    private void readState(){state=null;if(bridge.ready())try{state=bridge.state();}catch(IrisBridge.Unavailable ex){notice="Iris could not report its state. Check the installed Iris version or open its settings.";}}
    private void button(MenuLayout m,int row,int col,int columns,String text,boolean selected,boolean active,Runnable action){var b=new SettingsButton(m.x(col,columns),m.row(row),m.cellWidth(col,columns),m.buttonHeight(),text,selected,mod.config.menuAccent,action);b.active=active;addRenderableWidget(b);}
    @Override protected void initMenu(){
        readState();var m=MenuLayout.of(width,height);
        addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,mod.config.menuAccent,this::onClose));
        addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,this::onClose));
        button(m,1,0,2,"Shader packs & options",true,bridge.ready(),this::openIris);
        button(m,1,1,2,"Refresh status",false,true,()->{refresh();rebuildWidgets();});
        button(m,2,0,2,"Shaders on",state!=null&&state.enabled(),bridge.ready(),()->toggle(true));
        button(m,2,1,2,"Shaders off",state!=null&&!state.enabled(),bridge.ready(),()->toggle(false));
        button(m,4,0,1,"Get Iris",false,true,()->com.mojang.blaze3d.Blaze3D.openUri(java.net.URI.create("https://irisshaders.dev/download")));
    }
    private void toggle(boolean enabled){try{bridge.setEnabled(enabled);readState();notice=enabled?"Shaders enabled in Iris. Choose a shader pack in Shader packs & options.":"Shaders disabled in Iris.";}catch(IrisBridge.Unavailable ex){notice="Iris could not apply the change. Check Iris's own settings and any shader errors.";}rebuildWidgets();}
    private void openIris(){
        try{Object result=bridge.screen(new IrisReturnScreen(this));if(!(result instanceof Screen screen))throw new IrisBridge.Unavailable();notice="Iris saves shader changes. Menu Cancel does not undo them.";minecraft.gui.setScreen(screen);}
        catch(IrisBridge.Unavailable ex){notice="Could not open Iris controls. Check that Iris matches Minecraft 26.3, then Refresh status.";}
    }
    @Override protected void drawMenu(GuiGraphicsExtractor g,int mx,int my,float delta){
        var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,"Shaders / Iris");
        String title=switch(bridge.availability()){case ABSENT->"IRIS NOT INSTALLED";case UNAVAILABLE->"IRIS CONTROLS UNAVAILABLE";case READY->"IRIS "+bridge.version();};
        FestiveUi.textFit(g,title,m.margin(),m.row(0)+4,m.contentWidth(),bridge.ready()?0xFF7BDFA6:0xFFE6B582,.85f,true);
        String status=state==null?(bridge.ready()?"Status unavailable. Open Iris's own settings to check shaders.":"Install a compatible Iris version to use these controls."):"Shaders: "+(state.enabled()?"ON":"OFF")+"   /   "+(state.rendering()?"Rendering active":"No shader currently rendering");
        FestiveUi.textFit(g,status,m.margin(),m.row(3)+5,m.contentWidth(),0xFFBDC9DA,.76f,false);
        int y=m.row(5)+6;String[] help=bridge.ready()?new String[]{"Shader packs & options opens Iris's own full-screen shader selector.","Choose an installed pack, then use Iris's Apply / Done controls.","Iris's Done or Esc returns to this menu section.","Iris also provides its shaderpacks folder and each pack's own settings.","Join a world to see shaders. Pack errors and GPU support are handled by Iris."}:new String[]{"Install Iris for Minecraft 26.3 and its required Sodium version, then restart.","Get Iris opens the official Iris download website.","This mod works normally without Iris. Shader controls stay disabled.","If Iris is already installed, check its version and use Refresh status."};
        for(int i=0;i<help.length;i++)FestiveUi.textFit(g,help[i],m.margin(),y+i*15,m.contentWidth(),0xFFAEB9CB,.7f,false);
        m.footer(g,notice);super.drawMenu(g,mx,my,delta);
    }
    @Override public void onClose(){MenuNavigation.open(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
