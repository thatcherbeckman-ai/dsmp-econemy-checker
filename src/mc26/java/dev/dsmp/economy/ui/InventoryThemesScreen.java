package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.inventory.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Paginated holiday choices and an isolated live draft; Cancel never applies the draft. */
public final class InventoryThemesScreen extends Screen {
    private static final int PAGE_SIZE=6;
    private final EconomyTrackerClient mod;
    private final Screen parent;
    private InventoryOptions draft=InventoryThemes.current();
    private int page=Arrays.asList(InventoryTheme.values()).indexOf(draft.theme())/PAGE_SIZE;
    private String notice=InventoryThemes.loadError()==null?"Choose a theme. Done applies and saves; Cancel keeps your current look.":InventoryThemes.loadError();
    public InventoryThemesScreen(EconomyTrackerClient mod,Screen parent){super(Component.literal(MenuLayout.BRAND+" / Inventory themes"));this.mod=mod;this.parent=parent;}
    private int formWidth(MenuLayout m){return width>=780?(m.contentWidth()-m.gap()*2)/2:m.contentWidth();}
    private int x(MenuLayout m,int column,int columns){return m.margin()+column*(formWidth(m)+m.gap())/columns;}
    private int cell(MenuLayout m,int columns){return (formWidth(m)+m.gap())/columns-m.gap();}
    private void change(Runnable action){action.run();rebuildWidgets();}
    private void button(int x,int y,int w,int h,String label,boolean selected,Runnable action){addRenderableWidget(new SettingsButton(x,y,w,h,label,selected,mod.config.menuAccent,action));}
    @Override protected void init(){
        var m=MenuLayout.of(width,height);button(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,this::onClose);button(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,()->Minecraft.getInstance().gui.setScreen(parent));
        var themes=InventoryTheme.values();int from=page*PAGE_SIZE;
        for(int i=0;i<PAGE_SIZE&&from+i<themes.length;i++){var theme=themes[from+i];button(x(m,i%2,2),m.row(i/2),cell(m,2),m.buttonHeight(),theme.label,draft.theme()==theme,()->change(()->draft=draft.theme(theme)));}
        button(x(m,0,3),m.row(3),cell(m,3),m.buttonHeight(),"Prev",false,()->change(()->page=Math.floorMod(page-1,pages())));
        button(x(m,1,3),m.row(3),cell(m,3),m.buttonHeight(),"Page "+(page+1)+" / "+pages(),false,()->change(()->page=(page+1)%pages()));
        button(x(m,2,3),m.row(3),cell(m,3),m.buttonHeight(),"Next",false,()->change(()->page=(page+1)%pages()));
        button(x(m,0,2),m.row(4),cell(m,2),m.buttonHeight(),"Animation: "+(draft.animations()?"on":"off"),draft.animations(),()->change(()->draft=draft.animations(!draft.animations())));
        button(x(m,1,2),m.row(4),cell(m,2),m.buttonHeight(),"Effects: "+InventoryOptions.INTENSITIES[draft.intensity()],false,()->change(()->draft=draft.intensity((draft.intensity()+1)%3)));
        button(x(m,0,3),m.row(5),cell(m,3),m.buttonHeight(),"Slower",false,()->change(()->draft=draft.speed(draft.speed()-25)));
        button(x(m,1,3),m.row(5),cell(m,3),m.buttonHeight(),"Speed: "+draft.speed()+"%",false,()->change(()->draft=draft.speed(100)));
        button(x(m,2,3),m.row(5),cell(m,3),m.buttonHeight(),"Faster",false,()->change(()->draft=draft.speed(draft.speed()+25)));
    }
    private int pages(){return (InventoryTheme.values().length+PAGE_SIZE-1)/PAGE_SIZE;}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,"Inventory themes / "+draft.theme().label);
        int left=width>=780?m.margin()+formWidth(m)+m.gap()*2:m.margin(),top=width>=780?m.top():m.row(6)+m.gap();
        int w=width-m.margin()-left,h=height-top-32;
        if(h>=60){
            FestiveUi.panel(g,left,top,w,h,0x625084);FestiveUi.text(g,FestiveUi.fit(draft.theme().label+" / PREVIEW",w-24,.78f),left+12,top+10,0xFFDED5EE,.78f,true);
            float scale=Math.min(2f,Math.min((w-24)/176f,(h-40)/166f));int x=left+(int)((w-176*scale)/2),y=top+30+(int)((h-34-166*scale)/2);
            g.enableScissor(left+4,top+24,left+w-4,top+h-4);
            try{
                if(draft.theme()!=InventoryTheme.VANILLA)for(var r:HolidayScene.ambient(draft,w-8,h-28,InventoryPainter.seconds()))g.fill(left+4+r.x(),top+24+r.y(),left+4+r.x()+r.width(),top+24+r.y()+r.height(),r.color());
                g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(scale);
                try{
                    if(draft.theme()==InventoryTheme.VANILLA)g.blit(RenderPipelines.GUI_TEXTURED,AbstractContainerScreen.INVENTORY_LOCATION,0,0,0,0,176,166,256,256);
                    else InventoryPainter.panel(g,0,0,draft,InventoryPainter.seconds());
                    g.text(Minecraft.getInstance().font,Component.translatable("container.crafting"),97,6,draft.theme()==InventoryTheme.VANILLA?0xFF404040:0xFFE9EDF6,false);
                }finally{g.pose().popMatrix();}
                // Entity extraction consumes absolute GUI bounds, unlike the panel's scaled fills.
                if(Minecraft.getInstance().player!=null)net.minecraft.client.gui.screens.inventory.InventoryScreen.extractEntityInInventoryFollowsMouse(g,x+(int)(26*scale),y+(int)(8*scale),x+(int)(75*scale),y+(int)(78*scale),Math.max(1,(int)(30*scale)),.0625f,x+50*scale,y+25*scale,Minecraft.getInstance().player);
            }finally{g.disableScissor();}
        }
        m.footer(g,notice);super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){try{InventoryThemes.save(draft);Minecraft.getInstance().gui.setScreen(parent);}catch(java.io.IOException ex){notice="Save failed: "+ex.getMessage();}}
    @Override public boolean isPauseScreen(){return false;}
}
