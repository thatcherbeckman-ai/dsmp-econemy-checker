package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.text.*;

import java.util.Locale;

/** Add/edit text, then drag it anywhere in the preview canvas. */
public final class TextBoxesScreen extends net.minecraft.client.gui.screens.Screen {
    private final EconomyTrackerClient mod;
    private final net.minecraft.client.gui.screens.Screen parent;
    private final TextBoxEditor editor;
    private net.minecraft.client.gui.components.EditBox content,start,end;
    private String notice="Drag below. Use \\n for a new line.";
    private record Control(int x,int y,int width,String label){}
    private final java.util.List<Control> controls=new java.util.ArrayList<>();
    private TextBox dragging;
    private double offsetX,offsetY;
    public TextBoxesScreen(EconomyTrackerClient mod,net.minecraft.client.gui.screens.Screen parent) {
        super(net.minecraft.network.chat.Component.literal("Custom Text Boxes"));this.mod=mod;this.parent=parent;editor=new TextBoxEditor(mod.config);
    }
    @Override protected void init() {
        controls.clear();
        TextBox box=editor.selected();
        button(width-114,4,54,"Lines",this::lines);button(width-56,4,46,"Preset",()->change(()->editor.reference(net.minecraft.client.Minecraft.getInstance().getUser().getName())));
        button(10,28,44,"New",()->change(()->editor.add()));
        button(58,28,106,editor.label(),()->change(editor::next));
        button(168,28,44,"Delete",()->change(editor::delete));
        button(216,28,44,"Done",this::onClose);button(264,28,48,"Cancel",this::cancel);
        content=new net.minecraft.client.gui.components.EditBox(font,10,52,Math.max(280,width-20),20,net.minecraft.network.chat.Component.literal("Text (use \\n for lines)"));
        content.setMaxLength(1024);content.setValue(box==null?"":box.text.replace("\n","\\n"));addRenderableWidget(content);
        start=new net.minecraft.client.gui.components.EditBox(font,44,78,70,20,net.minecraft.network.chat.Component.literal("Start font color"));start.setMaxLength(7);start.setValue(String.format(Locale.ROOT,"%06X",box==null?0x49E8D4:box.startColor));addRenderableWidget(start);
        end=new net.minecraft.client.gui.components.EditBox(font,148,78,70,20,net.minecraft.network.chat.Component.literal("End gradient color"));end.setMaxLength(7);end.setValue(String.format(Locale.ROOT,"%06X",box==null?0x55C900:box.endColor));addRenderableWidget(end);
        content.setResponder(value->previewFields());start.setResponder(value->previewFields());end.setResponder(value->previewFields());
        button(222,78,88,"Grad: "+new String[]{"Solid","Across","Down"}[box==null?0:box.gradient],()->edit(b->b.gradient=(b.gradient+1)%3));
        button(10,104,44,"Size -",()->change(()->editor.size(-.1f)));button(58,104,44,"Size +",()->change(()->editor.size(.1f)));
        button(106,104,88,TextFonts.NAMES[box==null?0:box.font],()->edit(b->b.font=(b.font+1)%4));
        button(198,104,58,new String[]{"Left","Center","Right"}[box==null?0:box.align],()->edit(b->b.align=(b.align+1)%3));
        button(260,104,50,"BG:"+new String[]{"off","glass","panel"}[box==null?1:box.backgroundStyle],()->edit(b->b.backgroundStyle=(b.backgroundStyle+1)%3));
        button(10,128,70,box==null||box.visible?"Visible":"Hidden",()->edit(b->b.visible=!b.visible));
        button(84,128,74,"Shadow:"+(box!=null&&box.shadow?"on":"off"),()->edit(b->b.shadow=!b.shadow));
        button(162,128,90,"Mint/Lime",()->change(editor::preset));
        button(256,128,56,"+ Line",()->{if(editor.selected()!=null)content.insertText("\\n");});
    }
    private void button(int x,int y,int width,String label,Runnable action) {
        controls.add(new Control(x,y,width,label));
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.literal(label),b->action.run()).bounds(x,y,width,20).build());
    }
    private void lines(){if(!fields())return;var box=editor.selected();if(box==null){notice="Create a text box first.";return;}net.minecraft.client.Minecraft.getInstance().gui.setScreen(new LineStylesScreen(mod,this,box));}
    private boolean fields() {
        try { editor.fields(content.getValue(),start.getValue(),end.getValue());return true; }
        catch(IllegalArgumentException ex){notice=ex.getMessage();return false;}
    }
    private void previewFields(){if(fields())notice="Drag below. Use \\n for a new line.";}
    private void change(Runnable action){if(!fields())return;try{action.run();dragging=null;rebuildWidgets();}catch(IllegalArgumentException ex){notice=ex.getMessage();}}
    private void edit(java.util.function.Consumer<TextBox> action){change(()->{if(editor.selected()!=null)action.accept(editor.selected());});}
    @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics,int mx,int my,float delta){
        FestiveUi.background(graphics,width,height,mod.config);
        TextBox box=editor.selected();
        FestiveUi.text(graphics,FestiveUi.fit("TEXT BOXES / "+editor.label()+(box==null?"":"  "+Math.round(box.size*100)+"%"),width-136,.75f),10,10,0xFFEAF2F8,.75f,true);
        graphics.text(font,"Start",10,83,0xFFEAF2F8,false);graphics.text(font,"End",120,83,0xFFEAF2F8,false);
        graphics.text(font,notice,10,151,0xFFB4C1CC,false);
        TrackerRenderer.drawAll(graphics,mod.config,mod.ledger,mod.viewCycle,width,height,true,false,mod.wallet.estimated());
        TextHud.outline(graphics,box,width,height);
        super.extractRenderState(graphics,mx,my,delta);
        for(var c:controls)FestiveUi.control(graphics,c.x(),c.y(),c.width(),20,c.label(),c.label().equals("Done"),mx>=c.x()&&mx<c.x()+c.width()&&my>=c.y()&&my<c.y()+20,mod.config.menuAccent);
    }
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick){
        if(super.mouseClicked(event,doubleClick))return true;
        if(event.button()!=com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT||event.y()<TextLayout.EDITOR_TOP||!fields())return false;
        for(int i=mod.config.textBoxes.size()-1;i>=0;i--){var box=mod.config.textBoxes.get(i);var bounds=TextHud.bounds(box,width,height,true);
            if(bounds.contains(event.x(),event.y())){editor.select(box.id);dragging=box;offsetX=event.x()-bounds.x();offsetY=event.y()-bounds.y();rebuildWidgets();return true;}}
        return false;
    }
    @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event,double dx,double dy){
        if(dragging==null)return super.mouseDragged(event,dx,dy);
        var bounds=TextHud.bounds(dragging,width,height,true);
        TextLayout.drag(dragging,bounds,event.x(),event.y(),offsetX,offsetY,width,height,true);return true;
    }
    @Override public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event){if(dragging!=null){dragging=null;return true;}return super.mouseReleased(event);}
    @Override public void onClose(){if(!fields())return;if(mod.saveConfig()){if(parent instanceof DashboardScreen dashboard)dashboard.checkpoint();net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}else notice="Save failed. Retry Done or use Cancel.";}
    private void cancel(){editor.cancel();net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
