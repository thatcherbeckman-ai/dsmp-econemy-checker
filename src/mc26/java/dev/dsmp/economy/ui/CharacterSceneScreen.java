package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.menubackground.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.nio.file.Path;

/** Scene composer: a copied preference draft plus a preview of the account's actual character. */
public final class CharacterSceneScreen extends Screen {
    private static final String[] TABS={"Scenery","Player","Text","Layout"};
    private final EconomyTrackerClient mod;private final Screen parent;private MenuBackgroundOptions draft;
    private int tab,page;private EditBox title,subtitle,titleColor,subtitleColor;
    private String notice="Your skin, scenery and text. Done saves; Cancel keeps your current menu.";
    public CharacterSceneScreen(EconomyTrackerClient mod,Screen parent,MenuBackgroundOptions source){
        super(Component.literal(MenuLayout.BRAND+" / Character scene"));this.mod=mod;this.parent=parent;draft=source.playerView();
        page=Arrays.asList(MenuScene.values()).indexOf(draft.scene)/6;
    }
    public MenuBackgroundOptions previewOptions(){return draft;}
    private int formWidth(MenuLayout m){return width>=780?(m.contentWidth()-m.gap()*2)/2:m.contentWidth();}
    private int x(MenuLayout m,int col,int cols){return m.margin()+col*(formWidth(m)+m.gap())/cols;}
    private int cw(MenuLayout m,int cols){return (formWidth(m)+m.gap())/cols-m.gap();}
    private void button(int col,int cols,int row,String label,boolean selected,Runnable action){var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(x(m,col,cols),m.row(row),cw(m,cols),m.buttonHeight(),label,selected,mod.config.menuAccent,action));}
    private void change(Runnable action){if(!fields())return;action.run();draft.validate();rebuildWidgets();}
    private void triple(int row,String name,int value,Runnable less,Runnable reset,Runnable more){button(0,3,row,name+" -",false,()->change(less));button(1,3,row,name+": "+value,false,()->change(reset));button(2,3,row,name+" +",false,()->change(more));}
    private EditBox field(int col,int cols,int row,String value,int max,String hint){var m=MenuLayout.of(width,height);var input=new SettingsEditBox(font,x(m,col,cols),m.row(row),cw(m,cols),m.buttonHeight(),Component.literal(hint));input.setMaxLength(max);input.setHint(Component.literal(hint));input.setValue(value);addRenderableWidget(input);return input;}
    @Override protected void init(){
        title=subtitle=titleColor=subtitleColor=null;var m=MenuLayout.of(width,height);var c=draft.character;
        addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,()->Minecraft.getInstance().gui.setScreen(parent)));
        for(int i=0;i<TABS.length;i++){int target=i;button(i,4,0,TABS[i],tab==i,()->change(()->tab=target));}
        if(tab==0){
            var scenes=MenuScene.values();for(int i=0;i<6&&page*6+i<scenes.length;i++){var scene=scenes[page*6+i];button(i%2,2,1+i/2,scene.label,draft.scene==scene,()->change(()->draft.scene=scene));}
            int pages=(scenes.length+5)/6;button(0,3,4,"Prev",false,()->change(()->page=Math.floorMod(page-1,pages)));button(1,3,4,"Page "+(page+1)+" / "+pages,false,()->change(()->page=(page+1)%pages));button(2,3,4,"Next",false,()->change(()->page=(page+1)%pages));
            button(0,3,5,"Current background",false,()->change(()->{var current=MenuBackgrounds.current();current.character=c;draft=current;page=Arrays.asList(MenuScene.values()).indexOf(draft.scene)/6;}));button(1,3,5,"Import PNG",false,this::importPng);button(2,3,5,"Snow overlook",false,()->change(()->{CinematicTerrain.preset(draft);page=Arrays.asList(MenuScene.values()).indexOf(draft.scene)/6;}));
            button(0,3,6,"In menus: "+(c.enabled?"on":"off"),c.enabled,()->change(()->c.enabled=!c.enabled));button(1,3,6,"Full preview",true,this::fullPreview);button(2,3,6,"Style: "+c.sceneryStyle.label,c.sceneryStyle!=CharacterComposition.SceneryStyle.PHOTO,()->change(()->{var styles=CharacterComposition.SceneryStyle.values();c.sceneryStyle=styles[(c.sceneryStyle.ordinal()+1)%styles.length];if(c.sceneryStyle!=CharacterComposition.SceneryStyle.PHOTO){draft.blur=0;draft.overlay=5;draft.vignette=1;c.enabled=true;c.view=CharacterComposition.View.LEFT;c.turn=-15;c.tilt=0;c.nameplate=true;c.size=36;c.ground=83;if(c.title.equals("{player}"))c.title="";}if(draft.scene==MenuScene.MINECRAFT_SNOW&&c.sceneryStyle!=CharacterComposition.SceneryStyle.BLOCKS)draft.scene=MenuScene.WINTER_CABIN;}));
        }else if(tab==1){
            button(0,2,1,"View: "+c.view.label,false,()->change(()->{var values=CharacterComposition.View.values();c.view=values[(c.view.ordinal()+1)%values.length];}));button(1,2,1,"Cape: "+(c.cape?"on":"off"),c.cape,()->change(()->c.cape=!c.cape));
            if(CinematicTerrain.usesBlocks(draft)||CinematicTerrain.usesIllustrated(draft)){button(0,2,2,"Ground: Scenery",true,()->notice="Ground matches your scenery. Photo style offers separate platform materials.");button(1,2,2,"Base: Landscape",true,()->notice="The landscape extends into the scene. Switch to Photo for block/deck platforms.");}else{button(0,2,2,"Platform: "+c.platform.label,false,()->change(()->{var values=CharacterComposition.Platform.values();c.platform=values[(c.platform.ordinal()+1)%values.length];}));button(1,2,2,"Base: "+c.base.label,false,()->change(()->{var values=CharacterComposition.Base.values();c.base=values[(c.base.ordinal()+1)%values.length];}));}
            triple(3,"Size",c.size,()->c.size=Math.max(20,c.size-5),()->c.size=38,()->c.size=Math.min(55,c.size+5));
            triple(4,"Turn",c.turn,()->c.turn=Math.max(-180,c.turn-10),()->c.turn=0,()->c.turn=Math.min(180,c.turn+10));
            triple(5,"Tilt",c.tilt,()->c.tilt=Math.max(-15,c.tilt-5),()->c.tilt=0,()->c.tilt=Math.min(15,c.tilt+5));
            button(0,2,6,"Shadow: "+(c.shadow?"on":"off"),c.shadow,()->change(()->c.shadow=!c.shadow));button(1,2,6,"Full preview",true,this::fullPreview);
        }else if(tab==2){
            title=field(0,1,1,c.title.replace("\n","\\n"),128,"Title - {player} inserts your name");subtitle=field(0,1,2,c.subtitle.replace("\n","\\n"),248,"Subtitle - use \\n for another line");
            titleColor=field(0,3,3,String.format(Locale.ROOT,"%06X",c.titleColor),7,"Title color");subtitleColor=field(1,3,3,String.format(Locale.ROOT,"%06X",c.subtitleColor),7,"Subtitle color");
            button(2,3,3,"Color preset",false,()->change(()->{int[] palette={0xFFFFFF,0xFFD1E3,0xF5D78C,0xA5E5D3,0xCBBDF5};int i=0;for(;i<palette.length;i++)if(palette[i]==c.titleColor)break;c.titleColor=palette[(i+1)%palette.length];c.subtitleColor=palette[(i+2)%palette.length];}));
            button(0,3,4,CharacterComposition.FONTS[c.font],false,()->change(()->c.font=(c.font+1)%CharacterComposition.FONTS.length));button(1,3,4,"Text shadow: "+(c.textShadow?"on":"off"),c.textShadow,()->change(()->c.textShadow=!c.textShadow));button(2,3,4,"Name: "+(c.nameplate?"on":"off"),c.nameplate,()->change(()->c.nameplate=!c.nameplate));
            triple(5,"Text size",c.textSize,()->c.textSize=Math.max(50,c.textSize-10),()->c.textSize=100,()->c.textSize=Math.min(200,c.textSize+10));
            button(0,2,6,"Position: "+c.caption.name().toLowerCase(Locale.ROOT).replace('_',' '),false,()->change(()->{var values=CharacterComposition.Caption.values();c.caption=values[(c.caption.ordinal()+1)%values.length];}));button(1,2,6,"Full preview",true,this::fullPreview);
            for(var input:new EditBox[]{title,subtitle,titleColor,subtitleColor})input.setResponder(value->fields());
        }else{
            triple(1,"Horizontal",c.x,()->c.x=Math.max(0,c.x-5),()->c.x=50,()->c.x=Math.min(100,c.x+5));
            triple(2,"Ground",c.ground,()->c.ground=Math.max(55,c.ground-5),()->c.ground=78,()->c.ground=Math.min(90,c.ground+5));
            triple(3,"Platform size",c.platformSize,()->c.platformSize=Math.max(50,c.platformSize-10),()->c.platformSize=100,()->c.platformSize=Math.min(150,c.platformSize+10));
            button(0,3,4,"Left",false,()->change(()->c.x=28));button(1,3,4,"Center",false,()->change(()->c.x=50));button(2,3,4,"Right",false,()->change(()->c.x=72));
            button(0,2,5,"Reset composition",false,()->change(()->{var defaults=new CharacterComposition();c.x=defaults.x;c.ground=defaults.ground;c.size=defaults.size;c.platformSize=defaults.platformSize;c.turn=0;c.tilt=0;}));button(1,2,5,"Light: "+(c.matchLighting?"Scenery":"Bright"),c.matchLighting,()->change(()->c.matchLighting=!c.matchLighting));
            button(0,2,6,"In menus: "+(c.enabled?"on":"off"),c.enabled,()->change(()->c.enabled=!c.enabled));button(1,2,6,"Full preview",true,this::fullPreview);
        }
    }
    private boolean fields(){if(title==null)return true;try{var next=draft.character.copy();next.title=title.getValue().replace("\\n","\n");next.subtitle=subtitle.getValue().replace("\\n","\n");next.titleColor=dev.dsmp.economy.text.TextBox.rgb(titleColor.getValue());next.subtitleColor=dev.dsmp.economy.text.TextBox.rgb(subtitleColor.getValue());next.validate();draft.character.title=next.title;draft.character.subtitle=next.subtitle;draft.character.titleColor=next.titleColor;draft.character.subtitleColor=next.subtitleColor;return true;}catch(IllegalArgumentException ex){notice=ex.getMessage();return false;}}
    private void fullPreview(){if(fields())Minecraft.getInstance().gui.setScreen(new CharacterPreviewScreen(mod,this,draft));}
    private void importPng(){if(!fields())return;LocalImagePicker.choose(path->{if(path==null||Minecraft.getInstance().gui.screen()!=this)return;try{draft.customImage=MenuBackgrounds.store().importPng(Path.of(path));draft.scene=MenuScene.CUSTOM;notice="PNG copied. Done saves your scene.";rebuildWidgets();}catch(Exception ex){notice="Import failed: "+ex.getMessage();}},error->notice=error);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config,false);m.header(g,"Character scene / "+draft.scene.label);
        int form=formWidth(m);FestiveUi.surface(g,m.margin()-6,m.top()-6,form+12,Math.min(height-24,m.row(7)+4)-m.top()+6,0xDA0D1421);
        int left=width>=780?m.margin()+form+m.gap()*2:m.margin(),top=width>=780?m.top():m.row(7)+m.gap(),w=width-m.margin()-left,h=height-top-32;
        if(h>=80){
            FestiveUi.panel(g,left,top,w,h,mod.config.menuAccent);int ix=left+8,iy=top+28,iw=w-16,ih=h-36;
            g.enableScissor(ix,iy,ix+iw,iy+ih);g.pose().pushMatrix();g.pose().translate(ix,iy);try{MenuBackgroundRenderer.background(g,iw,ih,mod.config,draft);}finally{g.pose().popMatrix();}
            var preview=draft.copy();preview.character.enabled=true;CharacterSceneRenderer.draw(g,ix,iy,iw,ih,preview);g.disableScissor();UiSurfaces.corners(g,ix,iy,iw,ih,0xFF111827);
            FestiveUi.text(g,"YOUR CHARACTER / PREVIEW",left+14,top+9,0xFFE8DFF4,.7f,true);
        }
        m.footer(g,notice);super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){if(!fields())return;try{MenuBackgrounds.save(draft);if(parent instanceof MenuBackgroundScreen backgrounds)backgrounds.sceneSaved(draft);Minecraft.getInstance().gui.setScreen(parent);}catch(java.io.IOException ex){notice="Save failed: "+ex.getMessage();}}
    @Override public boolean isPauseScreen(){return false;}
}
