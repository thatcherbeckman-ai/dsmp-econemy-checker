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
    private record Control(int x,int y,int w,String label){}
    private final List<Control> controls=new ArrayList<>();
    public LineStylesScreen(EconomyTrackerClient mod,net.minecraft.client.gui.screens.Screen parent,TextBox box){super(net.minecraft.network.chat.Component.literal("Line design"));this.mod=mod;this.parent=parent;this.box=box;original=box.copy();}
    private LineStyle style(){return box.editLine(line);}
    @Override protected void init(){
        controls.clear();start=end=bg=dot=noteColor=null;line=Math.min(line,box.lines().length-1);var s=style();if(!s.custom){s.font=box.font;s.gradient=box.gradient;s.startColor=box.startColor;s.endColor=box.endColor;}
        button(width-116,12,50,"Done",this::onClose);button(width-60,12,48,"Cancel",this::cancel);
        int tab=(width-32)/3;String[] tabs={"Text","Row","Image"};for(int i=0;i<3;i++){int target=i;button(12+i*(tab+4),40,tab,tabs[i],()->change(()->page=target));}
        button(12,68,44,"Prev",()->change(()->line=Math.floorMod(line-1,box.lines().length)));button(60,68,44,"Next",()->change(()->line=(line+1)%box.lines().length));button(108,68,width-120,"Line "+(line+1)+" / "+box.lines().length,()->change(()->line=(line+1)%box.lines().length));
        content=field(12,92,width-24,box.lines()[line],160,"Line text");note=field(12,116,width-24,s.note,80,"Optional side note, e.g. Tie = DD");
        if(page==0){start=field(12,150,90,hex(s.startColor),7,"Font color");end=field(112,150,90,hex(s.endColor),7,"Gradient end");button(212,148,width-224,"Outline:"+(s.custom&&s.outline?"on":"off"),()->edit(v->{boolean old=v.custom&&v.outline;v.custom=true;v.outline=!old;}));
            button(12,174,100,TextFonts.NAMES[s.font],()->edit(v->{v.custom=true;v.font=(v.font+1)%4;}));button(118,174,90,new String[]{"Solid","Across","Down"}[s.gradient],()->edit(v->{v.custom=true;v.gradient=(v.gradient+1)%3;}));button(214,174,width-226,s.custom?"Custom":"Box style",()->edit(v->v.custom=!v.custom));
            button(12,198,50,"Size -",()->edit(v->v.textSize=Math.max(.5f,Math.round((v.textSize-.1f)*10)/10f)));button(68,198,50,"Size +",()->edit(v->v.textSize=Math.min(2.5f,Math.round((v.textSize+.1f)*10)/10f)));
        }else if(page==1){int w=(width-40)/3;bg=field(12,150,w,hex(s.backgroundColor),7,"Row background");dot=field(20+w,150,w,hex(s.bulletColor),7,"Bullet color");noteColor=field(28+w*2,150,w,hex(s.noteColor),7,"Side-note color");
            button(12,174,100,new String[]{"Plain","Pill","Card"}[s.shape],()->edit(v->v.shape=(v.shape+1)%3));button(118,174,90,"Dot:"+(s.bullet?"on":"off"),()->edit(v->v.bullet=!v.bullet));button(12,198,90,"Opacity -",()->edit(v->v.backgroundOpacity=Math.max(0,v.backgroundOpacity-20)));button(108,198,90,"Opacity +",()->edit(v->v.backgroundOpacity=Math.min(255,v.backgroundOpacity+20)));
        }else{button(12,148,100,"Import PNG",this::importImage);button(118,148,90,"Refresh",()->change(TextImages::refresh));button(214,148,width-226,"Folder",this::folder);button(12,174,140,"Next image",()->edit(v->{var choices=TextImages.choices();v.image=choices.get(Math.floorMod(choices.indexOf(v.image)+1,choices.size()));}));button(158,174,62,"Clear",()->edit(v->v.image=""));button(226,174,38,"-",()->edit(v->v.iconSize=Math.max(8,v.iconSize-2)));button(270,174,width-282,"+",()->edit(v->v.iconSize=Math.min(32,v.iconSize+2)));}
        for(var f:new net.minecraft.client.gui.components.EditBox[]{content,note,start,end,bg,dot,noteColor})if(f!=null)f.setResponder(value->{if(fields())notice="Preview updated. Parent editor Done saves these changes.";});
    }
    private static String hex(int color){return String.format(Locale.ROOT,"%06X",color);}
    private net.minecraft.client.gui.components.EditBox field(int x,int y,int w,String value,int max,String placeholder){var f=new net.minecraft.client.gui.components.EditBox(font,x,y,w,18,net.minecraft.network.chat.Component.literal(placeholder));f.setMaxLength(max);f.setValue(value);f.setHint(net.minecraft.network.chat.Component.literal(placeholder));addRenderableWidget(f);return f;}
    private void button(int x,int y,int w,String label,Runnable action){controls.add(new Control(x,y,w,label));addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.literal(label),b->action.run()).bounds(x,y,w,20).build());}
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
        FestiveUi.background(g,width,height,mod.config);FestiveUi.text(g,"LINE DESIGN",12,17,0xFFEAE2F6,.95f,true);var s=style();
        if(page==0){FestiveUi.text(g,"FONT COLOR",12,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"GRADIENT END",112,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"Scale "+Math.round(s.textSize*100)+"%",126,205,0xFFDBD1EE,.7f,false);}
        if(page==1){int w=(width-40)/3;FestiveUi.text(g,"BACKGROUND",12,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"BULLET",20+w,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"SIDE NOTE",28+w*2,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"Opacity "+s.backgroundOpacity,206,205,0xFFDBD1EE,.7f,false);}
        if(page==2){TextImages.draw(g,s.image,12,199,20,0xFFFFFFFF);FestiveUi.text(g,FestiveUi.fit(TextImages.label(s.image),width-54,.65f),38,204,0xFFDBD1EE,.65f,false);}
        if(height>=340){FestiveUi.text(g,"PREVIEW",12,232,0xFFAAA3BF,.65f,true);StyledTextHud.preview(g,box,12,246,width-24,height-274);}
        FestiveUi.text(g,FestiveUi.fit(notice,width-24,.65f),12,height-15,0xFFD9B5CE,.65f,false);super.extractRenderState(g,mx,my,delta);for(var c:controls)FestiveUi.control(g,c.x,c.y,c.w,20,c.label,c.label.equals("Done")||c.label.equals(new String[]{"Text","Row","Image"}[page]),mx>=c.x&&mx<c.x+c.w&&my>=c.y&&my<c.y+20,mod.config.menuAccent);
    }
    @Override public void onClose(){if(fields())net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}
    private void cancel(){box.restore(original);net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
