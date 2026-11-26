package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.text.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

/** Whole-box typography with reversible drafts, named swatches, custom RGB and content-safe presets. */
public final class TextStyleScreen extends WindowedScreen {
    private static final String[] TABS={"Fonts","Colors","Gradient","Effects","Spacing","Presets"},ROLES={"Start","End","Outline","Shadow","Background"};
    private final EconomyTrackerClient mod;private final Screen parent;private final TextBox box;private final TextStyleHistory history;
    private int tab,role,palettePage;private boolean fullPreview;private net.minecraft.client.gui.components.EditBox hex;
    private String notice="Styles affect the whole box. Use Lines for individual rows. Parent Done saves.";
    public TextStyleScreen(EconomyTrackerClient mod,Screen parent,TextBox box){super(Component.literal("Text style"));this.mod=mod;this.parent=parent;this.box=box;history=new TextStyleHistory(box);}
    private void edit(Consumer<TextBox> change){try{history.edit(change);notice="Preview updated. Undo/Redo is available. Parent Done saves.";rebuildWidgets();}catch(IllegalArgumentException ex){notice=ex.getMessage();}}
    private void button(int col,int cols,int row,String label,boolean selected,Runnable action){var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.x(col,cols),m.row(row),m.cellWidth(col,cols),m.buttonHeight(),label,selected,mod.config.menuAccent,action));}
    private void value(int row,String label,Runnable less,Runnable more){button(0,3,row,"-",false,less);button(1,3,row,label,true,()->{});button(2,3,row,"+",false,more);}
    private void tab(int next){if(next!=tab)tabTransition(next-tab);tab=next;rebuildWidgets();}
    @Override protected void initMenu(){var m=MenuLayout.of(width,height);hex=null;
        addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,()->{history.cancel();MenuNavigation.open(parent);}));
        var undo=new SettingsButton(m.doneX()-2*(m.actionWidth()+m.gap()),m.actionY(),m.actionWidth(),m.buttonHeight(),"Undo",false,mod.config.menuAccent,()->{history.undo();rebuildWidgets();});undo.active=history.canUndo();addRenderableWidget(undo);var redo=new SettingsButton(m.doneX()-m.actionWidth()-m.gap(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Redo",false,mod.config.menuAccent,()->{history.redo();rebuildWidgets();});redo.active=history.canRedo();addRenderableWidget(redo);
        for(int i=0;i<TABS.length;i++){int index=i;button(i,TABS.length,0,TABS[i],tab==i,()->tab(index));}
        addRenderableWidget(new SettingsButton(width-m.margin()-76,m.row(8)+6,70,14,fullPreview?"Sample":"Full box",false,mod.config.menuAccent,()->{fullPreview=!fullPreview;rebuildWidgets();}));
        if(tab==0){
            for(int i=0;i<TextFonts.NAMES.length;i++){int font=i;button(i%2,2,1+i/2,TextFonts.NAMES[i],box.font==i,()->edit(b->TextTypography.font(b,font)));}
            button(0,2,3,"Bold: "+(box.bold?"on":"off"),box.bold,()->edit(b->b.bold=!b.bold));button(1,2,3,"Italic: "+(box.italic?"on":"off"),box.italic,()->edit(b->b.italic=!b.italic));
            value(4,"Size: "+Math.round(box.size*100)+"%",()->edit(b->b.size=Math.max(.4f,Math.round((b.size-.1f)*10)/10f)),()->edit(b->b.size=Math.min(4,Math.round((b.size+.1f)*10)/10f)));
            for(int i=0;i<3;i++){int align=i;button(i,3,5,new String[]{"Align left","Align center","Align right"}[i],box.align==i,()->edit(b->b.align=align));}
        }else if(tab==1){
            for(int i=0;i<12;i++){int index=palettePage*12+i;button(i%3,3,1+i/3,TextTypography.COLOR_NAMES[index],TextTypography.color(box,role)==TextTypography.COLORS[index],()->edit(b->TextTypography.color(b,role,TextTypography.COLORS[index])));}
            button(0,3,5,"Prev colors",false,()->{palettePage=1-palettePage;rebuildWidgets();});button(1,3,5,"Palette "+(palettePage+1)+" / 2",true,()->{});button(2,3,5,"Next colors",false,()->{palettePage=1-palettePage;rebuildWidgets();});
            for(int i=0;i<ROLES.length;i++){int index=i;button(i,ROLES.length,6,ROLES[i],role==i,()->{role=index;rebuildWidgets();});}
            hex=new SettingsEditBox(font,m.margin(),m.row(7),m.cellWidth(0,3)*2+m.gap(),m.buttonHeight(),Component.literal(ROLES[role]+" color: RRGGBB"));hex.setMaxLength(7);hex.setValue(String.format(java.util.Locale.ROOT,"%06X",TextTypography.color(box,role)));addRenderableWidget(hex);button(2,3,7,"Apply hex",true,()->{try{int rgb=TextBox.rgb(hex.getValue());edit(b->TextTypography.color(b,role,rgb));}catch(IllegalArgumentException ex){notice=ex.getMessage();}});
        }else if(tab==2){
            for(int i=0;i<3;i++){int mode=i;button(i,3,1,new String[]{"Solid","Across","Down"}[i],box.gradient==i,()->edit(b->TextTypography.gradient(b,mode)));}
            button(0,3,2,"Start color",false,()->{role=0;tab(1);});button(1,3,2,"Swap colors",false,()->edit(b->{int first=b.startColor;TextTypography.color(b,0,b.endColor);TextTypography.color(b,1,first);}));button(2,3,2,"End color",false,()->{role=1;tab(1);});
            for(int i=0;i<6;i++){int preset=i;button(i%3,3,3+i/3,TextTypography.PRESETS[i],false,()->edit(b->TextTypography.pair(b,preset)));}
        }else if(tab==3){
            button(0,2,1,"Outline: "+(TextTypography.anyOutline(box)?"on":"off"),TextTypography.anyOutline(box),()->edit(b->TextTypography.outline(b,!TextTypography.anyOutline(b))));button(1,2,1,"Shadow: "+(box.shadow?"on":"off"),box.shadow,()->edit(b->b.shadow=!b.shadow));
            value(2,"Outline: "+String.format(java.util.Locale.ROOT,"%.1f",box.outlineWidth),()->edit(b->{TextTypography.outline(b,TextTypography.anyOutline(b));b.outlineWidth=Math.max(.3f,Math.round((b.outlineWidth-.2f)*10)/10f);}),()->edit(b->{TextTypography.outline(b,TextTypography.anyOutline(b));b.outlineWidth=Math.min(2,Math.round((b.outlineWidth+.2f)*10)/10f);}));
            value(3,"Shadow offset: "+box.shadowOffset,()->edit(b->b.shadowOffset=Math.max(.5f,b.shadowOffset-.5f)),()->edit(b->b.shadowOffset=Math.min(4,b.shadowOffset+.5f)));
            for(int i=0;i<3;i++){int bg=i;button(i,3,4,new String[]{"No background","Glass background","Panel background"}[i],box.backgroundStyle==i,()->edit(b->b.backgroundStyle=bg));}
            value(5,"Background: "+Math.round(box.backgroundOpacity/255f*100)+"%",()->edit(b->b.backgroundOpacity=Math.max(0,b.backgroundOpacity-16)),()->edit(b->b.backgroundOpacity=Math.min(255,b.backgroundOpacity+16)));
            button(0,2,6,"Effect colors",false,()->{role=2;tab(1);});button(1,2,6,"Reset effects",false,()->edit(TextTypography::reset));
        }else if(tab==4){
            value(1,"Letter spacing: "+box.letterSpacing,()->edit(b->b.letterSpacing=Math.max(0,b.letterSpacing-.5f)),()->edit(b->b.letterSpacing=Math.min(6,b.letterSpacing+.5f)));
            value(2,"Line gap: "+box.lineGap,()->edit(b->b.lineGap=Math.max(0,b.lineGap-1)),()->edit(b->b.lineGap=Math.min(20,b.lineGap+1)));
            value(3,"Text opacity: "+Math.round(box.textOpacity/255f*100)+"%",()->edit(b->b.textOpacity=Math.max(0,b.textOpacity-16)),()->edit(b->b.textOpacity=Math.min(255,b.textOpacity+16)));
            button(0,1,5,"Reset spacing",false,()->edit(b->{b.letterSpacing=0;b.lineGap=3;b.textOpacity=255;}));
        }else for(int i=0;i<TextTypography.PRESETS.length;i++){int preset=i;button(i%3,3,1+i/3,TextTypography.PRESETS[i],false,()->edit(b->TextTypography.preset(b,preset)));}
    }
    @Override protected void drawMenu(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta){var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);int room=m.doneX()-2*(m.actionWidth()+m.gap())-m.margin()-8;FestiveUi.textFit(g,"Text style / "+TABS[tab],m.margin(),m.top()>=64?15:9,room,0xFFF0E6EB,.85f,true);

        int top=m.row(8)+4,h=height-top-30;if(h>=30){FestiveUi.panel(g,m.margin(),top,m.contentWidth(),h,mod.config.menuAccent);FestiveUi.text(g,(!fullPreview&&box.lines().length>3?"LIVE PREVIEW / FIRST 3 LINES":"LIVE PREVIEW"),m.margin()+10,top+7,0xFFB7A5B4,.62f,true);g.enableScissor(m.margin()+5,top+20,width-m.margin()-5,top+h-4);try{var sample=box;if(!fullPreview&&box.lines().length>3){sample=box.copy();sample.text=String.join("\n",java.util.Arrays.copyOf(box.lines(),3));sample.trimStyles();}StyledTextHud.preview(g,sample,m.margin()+10,top+22,m.contentWidth()-20,h-26);}finally{g.disableScissor();}}
        m.footer(g,notice);super.drawMenu(g,mx,my,delta);if(tab==1)for(int i=0;i<12;i++){int index=palettePage*12+i;UiSurfaces.fill(g,m.x(i%3,3)+14,m.row(1+i/3)+m.buttonHeight()/2-3,6,6,0xFF000000|TextTypography.COLORS[index]);}
    }
    @Override public void onClose(){MenuNavigation.open(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
