package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.menubackground.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.nio.file.Path;
import java.util.Arrays;

/** A live cosmetic draft, with separate scene, appearance, motion and PNG controls. */
public final class MenuBackgroundScreen extends Screen {
    private static final String[] TABS={"Scenes","Look","Motion","Custom"};
    private final EconomyTrackerClient mod;private final Screen parent;
    private MenuBackgroundOptions draft=MenuBackgrounds.current();
    private int tab,page=Arrays.asList(MenuScene.values()).indexOf(draft.scene)/6;
    private String notice=MenuBackgrounds.loadError()==null?"Preview updates live. Done saves; Cancel restores your current background.":MenuBackgrounds.loadError();
    public MenuBackgroundScreen(EconomyTrackerClient mod,Screen parent){super(Component.literal(MenuLayout.BRAND+" / Menu backgrounds"));this.mod=mod;this.parent=parent;}
    public MenuBackgroundOptions previewOptions(){return draft;}
    public void sceneSaved(MenuBackgroundOptions saved){draft=saved.copy();page=Arrays.asList(MenuScene.values()).indexOf(draft.scene)/6;notice="Character scene saved. Use the editor for a full-screen preview.";rebuildWidgets();}
    private int formWidth(MenuLayout m){return width>=780?(m.contentWidth()-m.gap()*2)/2:m.contentWidth();}
    private void button(int col,int columns,int row,String label,boolean selected,Runnable action){var m=MenuLayout.of(width,height);int fw=formWidth(m),x=m.margin()+col*(fw+m.gap())/columns,w=(fw+m.gap())/columns-m.gap();addRenderableWidget(new SettingsButton(x,m.row(row),w,m.buttonHeight(),label,selected,mod.config.menuAccent,action));}
    private void change(Runnable action){action.run();draft.validate();rebuildWidgets();}
    private void select(MenuScene scene){change(()->{if(draft.scene==MenuScene.ORIGINAL&&scene!=MenuScene.ORIGINAL){draft.overlay=30;draft.cards=80;draft.vignette=1;draft.parallax=true;}draft.scene=scene;});}
    @Override protected void init(){
        var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,()->Minecraft.getInstance().gui.setScreen(parent)));
        for(int i=0;i<TABS.length;i++){int target=i;button(i,4,0,TABS[i],tab==i,()->change(()->tab=target));}
        if(tab==0){
            var scenes=MenuScene.values();for(int i=0;i<6&&page*6+i<scenes.length;i++){var scene=scenes[page*6+i];button(i%2,2,1+i/2,scene.label,draft.scene==scene,()->select(scene));}
            int pages=(scenes.length+5)/6;button(0,3,4,"Prev",false,()->change(()->page=Math.floorMod(page-1,pages)));button(1,3,4,"Page "+(page+1)+" / "+pages,false,()->change(()->page=(page+1)%pages));button(2,3,4,"Next",false,()->change(()->page=(page+1)%pages));button(0,2,5,"Reset background",false,()->change(()->{draft=new MenuBackgroundOptions();page=0;}));button(1,2,5,"Character scene",false,()->Minecraft.getInstance().gui.setScreen(new CharacterSceneScreen(mod,this,draft)));
        }else if(tab==1){
            triple(1,"Brightness",draft.brightness,()->draft.brightness=Math.max(50,draft.brightness-10),()->draft.brightness=100,()->draft.brightness=Math.min(150,draft.brightness+10));
            triple(2,"Darkness",draft.overlay,()->draft.overlay=Math.max(0,draft.overlay-5),()->draft.overlay=30,()->draft.overlay=Math.min(80,draft.overlay+5));
            button(0,2,3,"Blur: "+MenuBackgroundOptions.LEVELS[draft.blur],false,()->change(()->draft.blur=(draft.blur+1)%4));button(1,2,3,"Tint: "+MenuBackgroundOptions.TINTS[draft.tint],false,()->change(()->draft.tint=(draft.tint+1)%MenuBackgroundOptions.TINTS.length));
            triple(4,"Tint",draft.tintAmount,()->draft.tintAmount=Math.max(0,draft.tintAmount-5),()->draft.tintAmount=15,()->draft.tintAmount=Math.min(50,draft.tintAmount+5));
            button(0,2,5,"Vignette: "+MenuBackgroundOptions.LEVELS[draft.vignette],false,()->change(()->draft.vignette=(draft.vignette+1)%4));button(1,2,5,draft.contain?"Fit: Contain":"Fit: Cover",false,()->change(()->draft.contain=!draft.contain));
            triple(6,"Cards",draft.cards,()->draft.cards=Math.max(45,draft.cards-5),()->draft.cards=85,()->draft.cards=Math.min(100,draft.cards+5));
        }else if(tab==2){
            button(0,2,1,"Animation: "+(draft.animations?"on":"off"),draft.animations,()->change(()->draft.animations=!draft.animations));button(1,2,1,"Pan: "+(draft.parallax?"on":"off"),draft.parallax,()->change(()->draft.parallax=!draft.parallax));
            button(0,1,2,"Particles: "+draft.particles.name().toLowerCase(java.util.Locale.ROOT),false,()->change(()->{var values=MenuScene.Particles.values();draft.particles=values[(draft.particles.ordinal()+1)%values.length];}));
            button(0,2,3,"Density: "+MenuBackgroundOptions.DENSITIES[draft.density],false,()->change(()->draft.density=(draft.density+1)%3));button(1,2,3,"Decor: "+MenuBackgroundOptions.DECORATIONS[draft.decorations],false,()->change(()->draft.decorations=(draft.decorations+1)%3));
            triple(4,"Speed",draft.speed,()->draft.speed=Math.max(25,draft.speed-25),()->draft.speed=100,()->draft.speed=Math.min(200,draft.speed+25));
            triple(5,"Particles",draft.particleOpacity,()->draft.particleOpacity=Math.max(20,draft.particleOpacity-10),()->draft.particleOpacity=65,()->draft.particleOpacity=Math.min(100,draft.particleOpacity+10));
        }else{
            button(0,1,1,"Import PNG",true,this::importPng);button(0,2,2,"Use imported image",false,()->{if(draft.customImage.isEmpty())notice="Import a PNG first (up to 8 MB).";else select(MenuScene.CUSTOM);});button(1,2,2,"Clear selection",false,()->change(()->{draft.customImage="";if(draft.scene==MenuScene.CUSTOM)draft.scene=MenuScene.ORIGINAL;}));
            button(0,2,3,"Refresh images",false,()->{MenuBackgroundRenderer.refresh();notice="Background textures refreshed.";});button(1,2,3,draft.contain?"Fit: Contain":"Fit: Cover",false,()->change(()->draft.contain=!draft.contain));
            triple(4,"Zoom",draft.zoom,()->draft.zoom=Math.max(100,draft.zoom-10),()->draft.zoom=100,()->draft.zoom=Math.min(150,draft.zoom+10));
            button(0,1,5,"Reset all background options",false,()->change(()->{draft=new MenuBackgroundOptions();page=0;}));
        }
    }
    private void triple(int row,String name,int value,Runnable less,Runnable reset,Runnable more){button(0,3,row,name+" -",false,()->change(less));button(1,3,row,name+": "+value+"%",false,()->change(reset));button(2,3,row,name+" +",false,()->change(more));}
    private void importPng(){LocalImagePicker.choose(path->{if(path==null||Minecraft.getInstance().gui.screen()!=this)return;try{draft.customImage=MenuBackgrounds.store().importPng(Path.of(path));select(MenuScene.CUSTOM);notice="PNG copied locally. Done saves your selection.";}catch(Exception ex){notice="Import failed: "+ex.getMessage();}},error->{if(Minecraft.getInstance().gui.screen()==this)notice=error;});}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,"Menu backgrounds / "+draft.scene.label);
        int form=formWidth(m);FestiveUi.surface(g,m.margin()-6,m.top()-6,form+12,Math.min(height-24,m.row(tab==1?7:6)+4)-m.top()+6,0xD40D1421);
        int left=width>=780?m.margin()+form+m.gap()*2:m.margin(),top=width>=780?m.top():m.row(7)+m.gap(),w=width-m.margin()-left,h=height-top-32;
        if(h>=60){
            FestiveUi.panel(g,left,top,w,h,mod.config.menuAccent);
            int ix=left+8,iy=top+28,iw=w-16,ih=h-36;
            g.enableScissor(ix,iy,ix+iw,iy+ih);g.pose().pushMatrix();g.pose().translate(ix,iy);
            try{MenuBackgroundRenderer.background(g,iw,ih,mod.config,draft);}finally{g.pose().popMatrix();}CharacterSceneRenderer.draw(g,ix,iy,iw,ih,draft);g.disableScissor();
            UiSurfaces.corners(g,ix,iy,iw,ih,0xFF111827);
            FestiveUi.text(g,"LIVE PREVIEW",left+14,top+9,0xFFF3E5F3,.72f,true);
            int sampleWidth=Math.min(250,w-32),sampleY=top+h-68;FestiveUi.surface(g,left+16,sampleY,sampleWidth,48,draft.cards*255/100<<24|0x101522);
            FestiveUi.text(g,"Your menu",left+28,sampleY+9,0xFFF0E6F2,.9f,true);FestiveUi.text(g,"Cards and text stay readable",left+28,sampleY+27,0xFFBAC1D3,.68f,false);
        }

        m.footer(g,notice);super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){try{MenuBackgrounds.save(draft);Minecraft.getInstance().gui.setScreen(parent);}catch(java.io.IOException|IllegalArgumentException ex){notice="Save failed: "+ex.getMessage();}}
    @Override public boolean isPauseScreen(){return false;}
}
