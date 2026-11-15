package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.text.*;
import net.minecraft.*;
import java.util.*;
import java.nio.file.*;

/** Individual text/row/image controls. The parent text editor owns Save and Cancel. */
public final class LineStylesScreen extends class_437 {
    private final EconomyTrackerClient mod;private final class_437 parent;private final TextBox box,original;
    private int line,page;private class_342 content,note,start,end,bg,dot,noteColor;
    private String notice="Edit this line; Done returns to the drag preview.";
    private record Control(int x,int y,int w,String label){}
    private final List<Control> controls=new ArrayList<>();
    public LineStylesScreen(EconomyTrackerClient mod,class_437 parent,TextBox box){super(class_2561.method_43470("Line design"));this.mod=mod;this.parent=parent;this.box=box;original=box.copy();}
    private LineStyle style(){return box.editLine(line);}
    @Override protected void method_25426(){
        controls.clear();start=end=bg=dot=noteColor=null;line=Math.min(line,box.lines().length-1);var s=style();if(!s.custom){s.font=box.font;s.gradient=box.gradient;s.startColor=box.startColor;s.endColor=box.endColor;}
        button(field_22789-116,12,50,"Done",this::method_25419);button(field_22789-60,12,48,"Cancel",this::cancel);
        int tab=(field_22789-32)/3;String[] tabs={"Text","Row","Image"};for(int i=0;i<3;i++){int target=i;button(12+i*(tab+4),40,tab,tabs[i],()->change(()->page=target));}
        button(12,68,44,"Prev",()->change(()->line=Math.floorMod(line-1,box.lines().length)));button(60,68,44,"Next",()->change(()->line=(line+1)%box.lines().length));button(108,68,field_22789-120,"Line "+(line+1)+" / "+box.lines().length,()->change(()->line=(line+1)%box.lines().length));
        content=field(12,92,field_22789-24,box.lines()[line],160,"Line text");note=field(12,116,field_22789-24,s.note,80,"Optional side note, e.g. Tie = DD");
        if(page==0){start=field(12,150,90,hex(s.startColor),7,"Font color");end=field(112,150,90,hex(s.endColor),7,"Gradient end");button(212,148,field_22789-224,"Outline:"+(s.custom&&s.outline?"on":"off"),()->edit(v->{boolean old=v.custom&&v.outline;v.custom=true;v.outline=!old;}));
            button(12,174,100,TextFonts.NAMES[s.font],()->edit(v->{v.custom=true;v.font=(v.font+1)%4;}));button(118,174,90,new String[]{"Solid","Across","Down"}[s.gradient],()->edit(v->{v.custom=true;v.gradient=(v.gradient+1)%3;}));button(214,174,field_22789-226,s.custom?"Custom":"Box style",()->edit(v->v.custom=!v.custom));
            button(12,198,50,"Size -",()->edit(v->v.textSize=Math.max(.5f,Math.round((v.textSize-.1f)*10)/10f)));button(68,198,50,"Size +",()->edit(v->v.textSize=Math.min(2.5f,Math.round((v.textSize+.1f)*10)/10f)));
        }else if(page==1){int w=(field_22789-40)/3;bg=field(12,150,w,hex(s.backgroundColor),7,"Row background");dot=field(20+w,150,w,hex(s.bulletColor),7,"Bullet color");noteColor=field(28+w*2,150,w,hex(s.noteColor),7,"Side-note color");
            button(12,174,100,new String[]{"Plain","Pill","Card"}[s.shape],()->edit(v->v.shape=(v.shape+1)%3));button(118,174,90,"Dot:"+(s.bullet?"on":"off"),()->edit(v->v.bullet=!v.bullet));button(12,198,90,"Opacity -",()->edit(v->v.backgroundOpacity=Math.max(0,v.backgroundOpacity-20)));button(108,198,90,"Opacity +",()->edit(v->v.backgroundOpacity=Math.min(255,v.backgroundOpacity+20)));
        }else{button(12,148,100,"Import PNG",this::importImage);button(118,148,90,"Refresh",()->change(TextImages::refresh));button(214,148,field_22789-226,"Folder",this::folder);button(12,174,140,"Next image",()->edit(v->{var choices=TextImages.choices();v.image=choices.get(Math.floorMod(choices.indexOf(v.image)+1,choices.size()));}));button(158,174,62,"Clear",()->edit(v->v.image=""));button(226,174,38,"-",()->edit(v->v.iconSize=Math.max(8,v.iconSize-2)));button(270,174,field_22789-282,"+",()->edit(v->v.iconSize=Math.min(32,v.iconSize+2)));}
        for(var f:new class_342[]{content,note,start,end,bg,dot,noteColor})if(f!=null)f.method_1863(value->{if(fields())notice="Preview updated. Parent editor Done saves these changes.";});
    }
    private static String hex(int color){return String.format(Locale.ROOT,"%06X",color);}
    private class_342 field(int x,int y,int w,String value,int max,String placeholder){var f=new class_342(field_22793,x,y,w,18,class_2561.method_43470(placeholder));f.method_1880(max);f.method_1852(value);f.method_47404(class_2561.method_43470(placeholder));method_37063(f);return f;}
    private void button(int x,int y,int w,String label,Runnable action){controls.add(new Control(x,y,w,label));method_37063((class_364)class_4185.method_46430(class_2561.method_43470(label),b->action.run()).method_46434(x,y,w,20).method_46431());}
    private boolean fields(){
        if(content==null)return true;
        try{var next=box.copy();String[] lines=next.lines();lines[line]=content.method_1882();next.text=String.join("\n",lines);var s=next.editLine(line);s.note=note.method_1882();
            if(start!=null){int a=TextBox.rgb(start.method_1882()),b=TextBox.rgb(end.method_1882());if(a!=s.startColor||b!=s.endColor)s.custom=true;s.startColor=a;s.endColor=b;}
            if(bg!=null){s.backgroundColor=TextBox.rgb(bg.method_1882());s.bulletColor=TextBox.rgb(dot.method_1882());s.noteColor=TextBox.rgb(noteColor.method_1882());}next.validate();box.restore(next);return true;
        }catch(IllegalArgumentException ex){notice=ex.getMessage();return false;}
    }
    private void change(Runnable action){if(!fields())return;try{action.run();box.validate();method_41843();}catch(IllegalArgumentException ex){notice=ex.getMessage();}}
    private void edit(java.util.function.Consumer<LineStyle> action){change(()->action.accept(style()));}
    private void importImage(){if(!fields())return;try{String selected=org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog("Choose a PNG image",null,null,"PNG images (*.png)",false);if(selected==null)return;String key=TextImages.importPng(Path.of(selected));style().image=key;notice="Image imported for this line.";method_41843();}catch(Exception|LinkageError ex){notice="Could not import PNG: "+ex.getMessage()+". You can also use Folder and Refresh.";}}
    private void folder(){try{Files.createDirectories(TextImages.store().directory());class_156.method_668().method_60932(TextImages.store().directory());}catch(Exception ex){notice="Images folder: "+TextImages.store().directory();}}
    @Override public void method_25394(class_332 g,int mx,int my,float delta){
        FestiveUi.background(g,field_22789,field_22790,mod.config);FestiveUi.text(g,"LINE DESIGN",12,17,0xFFEAE2F6,.95f,true);var s=style();
        if(page==0){FestiveUi.text(g,"FONT COLOR",12,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"GRADIENT END",112,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"Scale "+Math.round(s.textSize*100)+"%",126,205,0xFFDBD1EE,.7f,false);}
        if(page==1){int w=(field_22789-40)/3;FestiveUi.text(g,"BACKGROUND",12,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"BULLET",20+w,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"SIDE NOTE",28+w*2,139,0xFFAAA3BF,.6f,true);FestiveUi.text(g,"Opacity "+s.backgroundOpacity,206,205,0xFFDBD1EE,.7f,false);}
        if(page==2){TextImages.draw(g,s.image,12,199,20,0xFFFFFFFF);FestiveUi.text(g,FestiveUi.fit(TextImages.label(s.image),field_22789-54,.65f),38,204,0xFFDBD1EE,.65f,false);}
        if(field_22790>=340){FestiveUi.text(g,"PREVIEW",12,232,0xFFAAA3BF,.65f,true);StyledTextHud.preview(g,box,12,246,field_22789-24,field_22790-274);}
        FestiveUi.text(g,FestiveUi.fit(notice,field_22789-24,.65f),12,field_22790-15,0xFFD9B5CE,.65f,false);super.method_25394(g,mx,my,delta);for(var c:controls)FestiveUi.control(g,c.x,c.y,c.w,20,c.label,c.label.equals("Done")||c.label.equals(new String[]{"Text","Row","Image"}[page]),mx>=c.x&&mx<c.x+c.w&&my>=c.y&&my<c.y+20,mod.config.menuAccent);
    }
    @Override public void method_25419(){if(fields())class_310.method_1551().method_1507(parent);}
    private void cancel(){box.restore(original);class_310.method_1551().method_1507(parent);}
    @Override public boolean method_25421(){return false;}
}
