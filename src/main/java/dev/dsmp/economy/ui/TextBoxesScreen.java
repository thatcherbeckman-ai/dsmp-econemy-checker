package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.text.*;
import net.minecraft.*;
import java.util.Locale;

/** Add/edit text, then drag it anywhere in the preview canvas. */
public final class TextBoxesScreen extends class_437 {
    private final EconomyTrackerClient mod;
    private final class_437 parent;
    private final TextBoxEditor editor;
    private class_342 content,start,end;
    private String notice="Drag below. Use \\n for a new line.";
    private TextBox dragging;
    private double offsetX,offsetY;
    public TextBoxesScreen(EconomyTrackerClient mod,class_437 parent) {
        super(class_2561.method_43470("Custom Text Boxes"));this.mod=mod;this.parent=parent;editor=new TextBoxEditor(mod.config);
    }
    @Override protected void method_25426() {
        TextBox box=editor.selected();
        button(10,28,44,"New",()->change(()->editor.add()));
        button(58,28,106,editor.label(),()->change(editor::next));
        button(168,28,44,"Delete",()->change(editor::delete));
        button(216,28,44,"Done",this::method_25419);button(264,28,48,"Cancel",this::cancel);
        content=new class_342(field_22793,10,52,Math.max(280,field_22789-20),20,class_2561.method_43470("Text (use \\n for lines)"));
        content.method_1880(1024);content.method_1852(box==null?"":box.text.replace("\n","\\n"));method_37063(content);
        start=new class_342(field_22793,44,78,70,20,class_2561.method_43470("Start font color"));start.method_1880(7);start.method_1852(String.format(Locale.ROOT,"%06X",box==null?0x49E8D4:box.startColor));method_37063(start);
        end=new class_342(field_22793,148,78,70,20,class_2561.method_43470("End gradient color"));end.method_1880(7);end.method_1852(String.format(Locale.ROOT,"%06X",box==null?0x55C900:box.endColor));method_37063(end);
        content.method_1863(value->previewFields());start.method_1863(value->previewFields());end.method_1863(value->previewFields());
        button(222,78,88,"Grad: "+new String[]{"Solid","Across","Down"}[box==null?0:box.gradient],()->edit(b->b.gradient=(b.gradient+1)%3));
        button(10,104,44,"Size -",()->change(()->editor.size(-.1f)));button(58,104,44,"Size +",()->change(()->editor.size(.1f)));
        button(106,104,88,new String[]{"Inter","Inter bold","Minecraft"}[box==null?0:box.font],()->edit(b->b.font=(b.font+1)%3));
        button(198,104,58,new String[]{"Left","Center","Right"}[box==null?0:box.align],()->edit(b->b.align=(b.align+1)%3));
        button(260,104,50,"BG:"+new String[]{"off","glass","panel"}[box==null?1:box.backgroundStyle],()->edit(b->b.backgroundStyle=(b.backgroundStyle+1)%3));
        button(10,128,70,box==null||box.visible?"Visible":"Hidden",()->edit(b->b.visible=!b.visible));
        button(84,128,74,"Shadow:"+(box!=null&&box.shadow?"on":"off"),()->edit(b->b.shadow=!b.shadow));
        button(162,128,90,"Mint/Lime",()->change(editor::preset));
        button(256,128,56,"+ Line",()->{if(editor.selected()!=null)content.method_1867("\\n");});
    }
    private void button(int x,int y,int width,String label,Runnable action) {
        method_37063((class_364)class_4185.method_46430(class_2561.method_43470(label),b->action.run()).method_46434(x,y,width,20).method_46431());
    }
    private boolean fields() {
        try { editor.fields(content.method_1882(),start.method_1882(),end.method_1882());return true; }
        catch(IllegalArgumentException ex){notice=ex.getMessage();return false;}
    }
    private void previewFields(){if(fields())notice="Drag below. Use \\n for a new line.";}
    private void change(Runnable action){if(!fields())return;try{action.run();dragging=null;method_41843();}catch(IllegalArgumentException ex){notice=ex.getMessage();}}
    private void edit(java.util.function.Consumer<TextBox> action){change(()->{if(editor.selected()!=null)action.accept(editor.selected());});}
    @Override public void method_25394(class_332 graphics,int mx,int my,float delta){
        graphics.method_25294(0,0,field_22789,field_22790,0xEE0B1018);
        TextBox box=editor.selected();
        graphics.method_51433(field_22793,"TEXT BOXES / "+editor.label()+(box==null?"":"  "+Math.round(box.size*100)+"%"),10,10,0xFFEAF2F8,true);
        graphics.method_51433(field_22793,"Start",10,83,0xFFEAF2F8,false);graphics.method_51433(field_22793,"End",120,83,0xFFEAF2F8,false);
        graphics.method_51433(field_22793,notice,10,151,0xFFB4C1CC,false);
        TrackerRenderer.drawAll(graphics,mod.config,mod.ledger,mod.viewCycle,field_22789,field_22790,true,false,mod.wallet.estimated());
        TextHud.outline(graphics,box,field_22789,field_22790);
        super.method_25394(graphics,mx,my,delta);
    }
    @Override public boolean method_25402(class_11909 event,boolean doubleClick){
        if(super.method_25402(event,doubleClick))return true;
        if(event.method_74245()!=0||event.comp_4799()<TextLayout.EDITOR_TOP||!fields())return false;
        for(int i=mod.config.textBoxes.size()-1;i>=0;i--){var box=mod.config.textBoxes.get(i);var bounds=TextHud.bounds(box,field_22789,field_22790,true);
            if(bounds.contains(event.comp_4798(),event.comp_4799())){editor.select(box.id);dragging=box;offsetX=event.comp_4798()-bounds.x();offsetY=event.comp_4799()-bounds.y();method_41843();return true;}}
        return false;
    }
    @Override public boolean method_25403(class_11909 event,double dx,double dy){
        if(dragging==null)return super.method_25403(event,dx,dy);
        var bounds=TextHud.bounds(dragging,field_22789,field_22790,true);
        TextLayout.drag(dragging,bounds,event.comp_4798(),event.comp_4799(),offsetX,offsetY,field_22789,field_22790,true);return true;
    }
    @Override public boolean method_25406(class_11909 event){if(dragging!=null){dragging=null;return true;}return super.method_25406(event);}
    @Override public void method_25419(){if(!fields())return;if(mod.saveConfig())class_310.method_1551().method_1507(parent);else notice="Save failed. Retry Done or use Cancel.";}
    private void cancel(){editor.cancel();class_310.method_1551().method_1507(parent);}
    @Override public boolean method_25421(){return false;}
}
