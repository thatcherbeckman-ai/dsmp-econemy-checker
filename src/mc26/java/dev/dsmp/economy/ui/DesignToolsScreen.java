package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.design.*;
import dev.dsmp.economy.menubackground.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Local appearance library, recovery, accessibility and searchable design settings. */
public final class DesignToolsScreen extends Screen {
    private static final String[] TABS={"Profiles","Backups","Access","Search"};
    private final EconomyTrackerClient mod;private final Screen parent;private AccessOptions access=DesignAccess.current();
    private int tab,page;private String name="My look",query="",selected="",notice="Profiles and restores save immediately. Accessibility saves with Done.";private EditBox input;
    private List<DesignArchive.Summary> entries=List.of();
    public DesignToolsScreen(EconomyTrackerClient mod,Screen parent){this(mod,parent,0);}
    public DesignToolsScreen(EconomyTrackerClient mod,Screen parent,int tab){super(Component.literal(MenuLayout.BRAND+" / Design tools"));this.mod=mod;this.parent=parent;this.tab=tab;DesignManager.initialize(mod);}
    public AccessOptions accessOptions(){return access;}
    private void action(Runnable operation){try{operation.run();}catch(RuntimeException ex){notice=ex.getMessage()==null?"Could not update the design":ex.getMessage();}rebuildWidgets();}
    private void io(Throwing operation){try{operation.run();}catch(Exception ex){notice=ex.getMessage()==null?"Could not update the design":ex.getMessage();}rebuildWidgets();}
    private interface Throwing{void run()throws Exception;}
    private void button(int col,int cols,int row,String label,boolean selected,Runnable action){var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.x(col,cols),m.row(row),m.cellWidth(col,cols),m.buttonHeight(),label,selected,mod.config.menuAccent,action));}
    @Override protected void init(){
        var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,()->Minecraft.getInstance().gui.setScreen(parent)));
        for(int i=0;i<TABS.length;i++){int target=i;button(i,4,0,TABS[i],tab==i,()->{tab=target;page=0;selected="";rebuildWidgets();});}
        input=null;
        if(tab==0||tab==1){
            try{entries=DesignManager.archive().list(tab==1);}catch(Exception ex){entries=List.of();notice=ex.getMessage();}int pages=Math.max(1,(entries.size()+2)/3);page=Math.clamp(page,0,pages-1);
            if(tab==0){input=new SettingsEditBox(font,m.margin(),m.row(1),m.contentWidth(),m.buttonHeight(),Component.literal("Profile name"));input.setMaxLength(40);input.setValue(name);input.setResponder(value->name=value);addRenderableWidget(input);button(0,3,2,"Save profile",true,()->io(()->{var saved=DesignManager.archive().saveProfile(name,DesignLook.capture(mod.config,MenuBackgrounds.current(),dev.dsmp.economy.inventory.InventoryThemes.current(),access));selected=saved.id();notice="Saved "+saved.name();}));button(1,3,2,"Apply selected",false,()->apply(false,selected));button(2,3,2,"Delete selected",false,()->io(()->{DesignManager.archive().deleteProfile(selected);selected="";notice="Profile removed. Your active design is unchanged.";}));}
            else{button(0,3,1,"Backup now",true,()->io(()->{DesignManager.archive().backup("Manual appearance backup",DesignManager.capture(mod));notice="Appearance backup saved.";}));button(1,3,1,"Restore selected",false,()->apply(true,selected));button(2,3,1,"Refresh",false,this::rebuildWidgets);}
            for(int i=0;i<3&&page*3+i<entries.size();i++){var entry=entries.get(page*3+i);button(0,2,3+i,tab==1?java.time.format.DateTimeFormatter.ofPattern("MMM d HH:mm").withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.ofEpochMilli(entry.created()))+" "+entry.name():entry.name(),entry.id().equals(selected),()->{selected=entry.id();if(tab==0&&entry.valid())name=entry.name();rebuildWidgets();});button(1,2,3+i,tab==0?"Use "+(page*3+i+1):"Restore "+(page*3+i+1),false,()->apply(tab==1,entry.id()));}
            button(0,3,6,"Prev",false,()->action(()->page=Math.floorMod(page-1,pages)));button(1,3,6,"Page "+(page+1)+" / "+pages,false,()->action(()->page=(page+1)%pages));button(2,3,6,"Next",false,()->action(()->page=(page+1)%pages));
        }else if(tab==2){
            button(0,3,1,"Text -",false,()->action(()->access.textScale=Math.max(100,access.textScale-15)));button(1,3,1,"Menu text: "+access.textScale+"%",true,()->action(()->access.textScale=100));button(2,3,1,"Text +",false,()->action(()->access.textScale=Math.min(130,access.textScale+15)));
            button(0,1,2,"High contrast: "+(access.highContrast?"on":"off"),access.highContrast,()->action(()->access.highContrast=!access.highContrast));button(0,1,3,"Reduced motion: "+(access.reducedMotion?"on":"off"),access.reducedMotion,()->action(()->access.reducedMotion=!access.reducedMotion));button(0,1,5,"Reset accessibility",false,()->action(()->access=new AccessOptions()));
        }else{
            input=new SettingsEditBox(font,m.margin(),m.row(1),m.contentWidth(),m.buttonHeight(),Component.literal("Search settings"));input.setMaxLength(80);input.setHint(Component.literal("Search fonts, lighting, colors, drag..."));input.setValue(query);input.setResponder(value->{query=value;page=0;updateSearch();});addRenderableWidget(input);updateSearch();
        }
    }
    private final List<net.minecraft.client.gui.components.AbstractWidget> results=new ArrayList<>();
    private void updateSearch(){if(tab!=3)return;for(var widget:results)removeWidget(widget);results.clear();var found=SettingSearch.find(query);int pages=Math.max(1,(found.size()+3)/4);page=Math.clamp(page,0,pages-1);var m=MenuLayout.of(width,height);
        for(int i=0;i<4&&page*4+i<found.size();i++){var item=found.get(page*4+i);var b=new SettingsButton(m.margin(),m.row(2+i),m.contentWidth(),m.buttonHeight(),item.label(),false,mod.config.menuAccent,()->open(item));addRenderableWidget(b);results.add(b);}
        for(int i=0;i<3;i++){int which=i;var b=new SettingsButton(m.x(i,3),m.row(6),m.cellWidth(i,3),m.buttonHeight(),i==0?"Prev":i==1?"Page "+(page+1)+" / "+pages:"Next",false,mod.config.menuAccent,()->{page=which==0?Math.floorMod(page-1,pages):(page+1)%pages;updateSearch();});addRenderableWidget(b);results.add(b);}notice=found.isEmpty()?"No matching design settings.":found.size()+" matching design settings.";
    }
    private void apply(boolean backup,String id){io(()->{if(id.isEmpty())throw new IllegalArgumentException("Select a design first");var entry=DesignManager.archive().load(backup,id);DesignManager.apply(mod,entry.look());access=DesignAccess.current();if(parent instanceof DashboardScreen dashboard)dashboard.checkpoint();notice=(backup?"Restored ":"Applied ")+entry.name()+". Previous look backed up.";});}
    private void open(SettingSearch.Item item){Screen screen=switch(item.target()){case "scene"->new CharacterSceneScreen(mod,this,MenuBackgrounds.current()).openTab(item.tab());case "background"->new MenuBackgroundScreen(mod,this).openTab(item.tab());case "appearance"->new AppearanceScreen(mod,this);case "text"->new TextBoxesScreen(mod,this);case "layout"->new TrackerScreen(mod);case "inventory"->new InventoryThemesScreen(mod,this);default->null;};if(screen==null){tab=item.tab();page=0;rebuildWidgets();}else Minecraft.getInstance().gui.setScreen(screen);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){FestiveUi.background(g,width,height,mod.config,false);var m=MenuLayout.of(width,height);m.header(g,"Design tools / "+TABS[tab]);FestiveUi.surface(g,m.margin()-5,m.top()-5,m.contentWidth()+10,Math.min(height-26,m.row(7))-m.top()+5,DesignAccess.contrast()?0xFF07101B:0xDC0D1421);m.footer(g,notice);super.extractRenderState(g,mx,my,delta);}
    @Override public void onClose(){try{DesignManager.saveAccess(mod,access);Minecraft.getInstance().gui.setScreen(parent);}catch(Exception ex){notice=ex.getMessage();}}
    @Override public boolean isPauseScreen(){return false;}
}
