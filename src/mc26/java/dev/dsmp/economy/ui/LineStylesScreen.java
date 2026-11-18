package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.text.*;

import java.util.*;
import java.nio.file.*;

/** Individual text/row/image controls. The parent text editor owns Save and Cancel. */
public final class LineStylesScreen extends net.minecraft.client.gui.screens.Screen {
    private final EconomyTrackerClient mod;private final net.minecraft.client.gui.screens.Screen parent;private final TextBox box,original;
    private int line,page;private net.minecraft.client.gui.components.EditBox content,note,start,end,bg,dot,noteColor;
    private String notice="Edit this line; Done returns to the drag preview.";
    public LineStylesScreen(EconomyTrackerClient mod,net.minecraft.client.gui.screens.Screen parent,TextBox box){super(net.minecraft.network.chat.Component.literal(MenuLayout.BRAND+" / Line design"));this.mod=mod;this.parent=parent;this.box=box;original=box.copy();}
    private LineStyle style(){return box.editLine(line);}
    @Override protected void init(){
        start=end=bg=dot=noteColor=null;line=Math.min(line,box.lines().length-1);var s=style();if(!s.custom){s.font=box.font;s.gradient=box.gradient;s.startColor=box.startColor;s.endColor=box.endColor;}
        var m=MenuLayout.of(width,height);int form=styleY();
        button(m.doneX(),m.actionY(),m.actionWidth(),"Done",this::onClose);button(m.cancelX(),m.actionY(),m.actionWidth(),"Cancel",this::cancel);
        String[] tabs={"Text","Row","Image"};for(int i=0;i<3;i++){int target=i;button(m.x(i,3),m.row(0),m.cellWidth(i,3),tabs[i],()->change(()->page=target));}
        button(m.x(0,4),m.row(1),m.cellWidth(0,4),"Prev",()->change(()->line=Math.floorMod(line-1,box.lines().length)));button(m.x(1,4),m.row(1),m.cellWidth(1,4),"Next",()->change(()->line=(line+1)%box.lines().length));button(m.x(2,4),m.row(1),m.contentWidth()-(m.x(2,4)-m.margin()),"Line "+(line+1)+" / "+box.lines().length,()->change(()->line=(line+1)%box.lines().length));
        content=field(m.margin(),m.row(2),m.contentWidth(),box.lines()[line],160,"Line text");note=field(m.margin(),m.row(3),m.contentWidth(),s.note,80,"Optional side note, e.g. Tie = DD");
        if(page==0){
            start=field(m.x(0,3),form,m.cellWidth(0,3),hex(s.startColor),7,"Font color");end=field(m.x(1,3),form,m.cellWidth(1,3),hex(s.endColor),7,"Gradient end");button(m.x(2,3),form,m.cellWidth(2,3),"Outline:"+(s.custom&&s.outline?"on":"off"),()->edit(v->{boolean old=v.custom&&v.outline;v.custom=true;v.outline=!old;}));
            button(m.x(0,3),form+m.step(),m.cellWidth(0,3),TextFonts.NAMES[s.font],()->edit(v->{v.custom=true;v.font=(v.font+1)%4;}));button(m.x(1,3),form+m.step(),m.cellWidth(1,3),new String[]{"Solid","Across","Down"}[s.gradient],()->edit(v->{v.custom=true;v.gradient=(v.gradient+1)%3;}));button(m.x(2,3),form+m.step(),m.cellWidth(2,3),s.custom?"Custom":"Box style",()->edit(v->v.custom=!v.custom));
            button(m.x(0,3),form+m.step()*2,m.cellWidth(0,3),"Size -",()->edit(v->v.textSize=Math.max(.5f,Math.round((v.textSize-.1f)*10)/10f)));button(m.x(1,3),form+m.step()*2,m.cellWidth(1,3),"Size +",()->edit(v->v.textSize=Math.min(2.5f,Math.round((v.textSize+.1f)*10)/10f)));
        }else if(page==1){
            bg=field(m.x(0,3),form,m.cellWidth(0,3),hex(s.backgroundColor),7,"Row background");dot=field(m.x(1,3),form,m.cellWidth(1,3),hex(s.bulletColor),7,"Bullet color");noteColor=field(m.x(2,3),form,m.cellWidth(2,3),hex(s.noteColor),7,"Side-note color");
            button(m.x(0,3),form+m.step(),m.cellWidth(0,3),new String[]{"Plain","Pill","Card"}[s.shape],()->edit(v->v.shape=(v.shape+1)%3));button(m.x(1,3),form+m.step(),m.cellWidth(1,3),"Dot:"+(s.bullet?"on":"off"),()->edit(v->v.bullet=!v.bullet));button(m.x(0,3),form+m.step()*2,m.cellWidth(0,3),"Opacity -",()->edit(v->v.backgroundOpacity=Math.max(0,v.backgroundOpacity-20)));button(m.x(1,3),form+m.step()*2,m.cellWidth(1,3),"Opacity +",()->edit(v->v.backgroundOpacity=Math.min(255,v.backgroundOpacity+20)));
        }else{
            button(m.x(0,3),form,m.cellWidth(0,3),"Import PNG",this::importImage);button(m.x(1,3),form,m.cellWidth(1,3),"Refresh",()->change(TextImages::refresh));button(m.x(2,3),form,m.cellWidth(2,3),"Folder",this::folder);
            button(m.x(0,4),form+m.step(),m.cellWidth(0,4),"Next image",()->edit(v->{var choices=TextImages.choices();v.image=choices.get(Math.floorMod(choices.indexOf(v.image)+1,choices.size()));}));button(m.x(1,4),form+m.step(),m.cellWidth(1,4),"Clear",()->edit(v->v.image=""));button(m.x(2,4),form+m.step(),m.cellWidth(2,4),"-",()->edit(v->v.iconSize=Math.max(8,v.iconSize-2)));button(m.x(3,4),form+m.step(),m.cellWidth(3,4),"+",()->edit(v->v.iconSize=Math.min(32,v.iconSize+2)));
        }
        for(var f:new net.minecraft.client.gui.components.EditBox[]{content,note,start,end,bg,dot,noteColor})if(f!=null)f.setResponder(value->{if(fields())notice="Preview updated. Parent editor Done saves these changes.";});
    }
    private int styleY(){var m=MenuLayout.of(width,height);return m.row(4)+(m.top()>=64?12:6);}
    private static String hex(int color){return String.format(Locale.ROOT,"%06X",color);}
    private net.minecraft.client.gui.components.EditBox field(int x,int y,int w,String value,int max,String placeholder){var f=new net.minecraft.client.gui.components.EditBox(font,x,y,w,MenuLayout.of(width,height).buttonHeight(),net.minecraft.network.chat.Component.literal(placeholder));f.setMaxLength(max);f.setValue(value);f.setHint(net.minecraft.network.chat.Component.literal(placeholder));addRenderableWidget(f);return f;}
    private void button(int x,int y,int w,String label,Runnable action){addRenderableWidget(new SettingsButton(x,y,w,MenuLayout.of(width,height).buttonHeight(),label,label.equals("Done")||label.equals(new String[]{"Text","Row","Image"}[page]),mod.config.menuAccent,action));}
    private boolean fields(){
        if(content==null)return true;
        try{var next=box.copy();String[] lines=next.lines();lines[line]=content.getValue();next.text=String.join("\n",lines);var s=next.editLine(line);s.note=note.getValue();
            if(start!=null){int a=TextBox.rgb(start.getValue()),b=TextBox.rgb(end.getValue());if(a!=s.startColor||b!=s.endColor)s.custom=true;s.startColor=a;s.endColor=b;}
            if(bg!=null){s.backgroundColor=TextBox.rgb(bg.getValue());s.bulletColor=TextBox.rgb(dot.getValue());s.noteColor=TextBox.rgb(noteColor.getValue());}next.validate();box.restore(next);return true;
        }catch(IllegalArgumentException ex){notice=ex.getMessage();return false;}
    }
    private void change(Runnable action){if(!fields())return;try{action.run();box.validate();rebuildWidgets();}catch(IllegalArgumentException ex){notice=ex.getMessage();}}
    private void edit(java.util.function.Consumer<LineStyle> action){change(()->action.accept(style()));}
    private void importImage(){if(!fields())return;int selectedLine=line;LocalImagePicker.choose(selected->{
        if(net.minecraft.client.Minecraft.getInstance().gui.screen()!=this||selected==null)return;
        try{String key=TextImages.importPng(Path.of(selected));box.editLine(selectedLine).image=key;notice="Image imported for this line.";rebuildWidgets();}catch(Exception ex){notice="Could not import PNG: "+ex.getMessage();}
    },error->{if(net.minecraft.client.Minecraft.getInstance().gui.screen()==this)notice=error;});}
    private void folder(){try{Files.createDirectories(TextImages.store().directory());if(!org.lwjgl.sdl.SDLMisc.SDL_OpenURL(TextImages.store().directory().toUri().toString()))throw new java.io.IOException("Folder could not be opened");}catch(Exception ex){notice="Images folder: "+TextImages.store().directory();}}
    @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta){
        var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,"Line design / "+(line+1)+" of "+box.lines().length);var s=style();int form=styleY();
        if(page==0){FestiveUi.text(g,"FONT COLOR",m.x(0,3),form-10,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"GRADIENT END",m.x(1,3),form-10,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"Scale "+Math.round(s.textSize*100)+"%",m.x(2,3),form+m.step()*2+7,0xFFDBD1EE,.7f,false);}
        if(page==1){String[] titles={"BACKGROUND","BULLET","SIDE NOTE"};for(int i=0;i<3;i++)FestiveUi.text(g,titles[i],m.x(i,3),form-10,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"Opacity "+s.backgroundOpacity,m.x(2,3),form+m.step()*2+7,0xFFDBD1EE,.7f,false);}
        if(page==2){TextImages.draw(g,s.image,m.margin(),form+m.step()*2,20,0xFFFFFFFF);FestiveUi.text(g,FestiveUi.fit(TextImages.label(s.image),m.contentWidth()-30,.7f),m.margin()+28,form+m.step()*2+6,0xFFDBD1EE,.7f,false);}
        int previewTop=form+m.step()*3+4;
        if(height-previewTop>=70){FestiveUi.panel(g,m.margin(),previewTop,m.contentWidth(),height-previewTop-30,0x625084);FestiveUi.text(g,"PREVIEW",m.margin()+10,previewTop+8,0xFFAAA3BF,.65f,true);StyledTextHud.preview(g,box,m.margin()+10,previewTop+25,m.contentWidth()-20,height-previewTop-60);}
        m.footer(g,notice);super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){if(fields())net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}
    private void cancel(){box.restore(original);net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
