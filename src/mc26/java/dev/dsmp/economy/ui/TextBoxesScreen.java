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
    private TextBox dragging;
    private double offsetX,offsetY;
    public TextBoxesScreen(EconomyTrackerClient mod,net.minecraft.client.gui.screens.Screen parent) {
        super(net.minecraft.network.chat.Component.literal(MenuLayout.BRAND+" / Text boxes"));this.mod=mod;this.parent=parent;editor=new TextBoxEditor(mod.config);
    }
    @Override protected void init() {
        TextBox box=editor.selected();var m=MenuLayout.of(width,height);
        button(m.doneX(),8,m.actionWidth(),"Done",this::onClose);button(m.cancelX(),8,m.actionWidth(),"Cancel",this::cancel);
        int row=42,step=30;
        String[] labels={"New",editor.label(),"Delete","Lines","Preset"};
        Runnable[] actions={()->change(()->editor.add()),()->change(editor::next),()->change(editor::delete),this::lines,()->change(()->editor.reference(net.minecraft.client.Minecraft.getInstance().getUser().getName()))};
        for(int i=0;i<labels.length;i++)button(m.x(i,5),row,m.cellWidth(i,5),labels[i],actions[i]);
        content=new SettingsEditBox(font,m.margin(),row+step,m.contentWidth(),22,net.minecraft.network.chat.Component.literal("Text (use \\n for lines)"));
        content.setMaxLength(1024);content.setValue(box==null?"":box.text.replace("\n","\\n"));addRenderableWidget(content);
        int colorY=row+step*2,cell=m.cellWidth(0,6),label=width>=620?34:0;
        start=new SettingsEditBox(font,m.x(0,6)+label,colorY,cell-label,22,net.minecraft.network.chat.Component.literal("Start font color"));start.setMaxLength(7);start.setValue(String.format(Locale.ROOT,"%06X",box==null?0x49E8D4:box.startColor));addRenderableWidget(start);
        end=new SettingsEditBox(font,m.x(1,6)+label,colorY,cell-label,22,net.minecraft.network.chat.Component.literal("End gradient color"));end.setMaxLength(7);end.setValue(String.format(Locale.ROOT,"%06X",box==null?0x55C900:box.endColor));addRenderableWidget(end);
        content.setResponder(value->previewFields());start.setResponder(value->previewFields());end.setResponder(value->previewFields());
        button(m.x(2,6),colorY,m.cellWidth(2,6),"Grad: "+new String[]{"Solid","Across","Down"}[box==null?0:box.gradient],()->edit(b->b.gradient=(b.gradient+1)%3));
        button(m.x(3,6),colorY,m.cellWidth(3,6),TextFonts.NAMES[box==null?0:box.font],()->edit(b->b.font=(b.font+1)%4));
        button(m.x(4,6),colorY,m.cellWidth(4,6),new String[]{"Left","Center","Right"}[box==null?0:box.align],()->edit(b->b.align=(b.align+1)%3));
        button(m.x(5,6),colorY,m.cellWidth(5,6),"BG:"+new String[]{"off","glass","panel"}[box==null?1:box.backgroundStyle],()->edit(b->b.backgroundStyle=(b.backgroundStyle+1)%3));
        int last=row+step*3;
        button(m.x(0,6),last,m.cellWidth(0,6),"Size -",()->change(()->editor.size(-.1f)));button(m.x(1,6),last,m.cellWidth(1,6),"Size +",()->change(()->editor.size(.1f)));
        button(m.x(2,6),last,m.cellWidth(2,6),box==null||box.visible?"Visible":"Hidden",()->edit(b->b.visible=!b.visible));
        button(m.x(3,6),last,m.cellWidth(3,6),"Shadow:"+(box!=null&&box.shadow?"on":"off"),()->edit(b->b.shadow=!b.shadow));
        button(m.x(4,6),last,m.cellWidth(4,6),"Mint/Lime",()->change(editor::preset));
        button(m.x(5,6),last,m.cellWidth(5,6),"+ Line",()->{if(editor.selected()!=null)content.insertText("\\n");});
    }
    private void button(int x,int y,int width,String label,Runnable action){
        addRenderableWidget(new SettingsButton(x,y,width,22,label,label.equals("Done"),mod.config.menuAccent,action));
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
        var m=MenuLayout.of(width,height);String title="TEXT BOXES / "+editor.label()+(box==null?"":"  "+Math.round(box.size*100)+"%");
        FestiveUi.textFit(graphics,title,m.margin(),14,m.doneX()-m.margin()-8,0xFFEAF2F8,.84f,true);
        if(width>=620){FestiveUi.text(graphics,"Start",m.x(0,6),109,0xFFB6ABC8,.7f,false);FestiveUi.text(graphics,"End",m.x(1,6),109,0xFFB6ABC8,.7f,false);}
        TrackerRenderer.drawAll(graphics,mod.config,mod.ledger,mod.viewCycle,width,height-24,true,false,mod.wallet.estimated());
        TextHud.outline(graphics,box,width,height-24);
        super.extractRenderState(graphics,mx,my,delta);
        m.footer(graphics,notice);
    }
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick){
        if(super.mouseClicked(event,doubleClick))return true;
        if(event.button()!=com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT||event.y()<TextLayout.EDITOR_TOP||event.y()>=height-24||!fields())return false;
        for(int i=mod.config.textBoxes.size()-1;i>=0;i--){var box=mod.config.textBoxes.get(i);var bounds=TextHud.bounds(box,width,height-24,true);
            if(bounds.contains(event.x(),event.y())){editor.select(box.id);dragging=box;offsetX=event.x()-bounds.x();offsetY=event.y()-bounds.y();rebuildWidgets();return true;}}
        return false;
    }
    @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event,double dx,double dy){
        if(dragging==null)return super.mouseDragged(event,dx,dy);
        var bounds=TextHud.bounds(dragging,width,height-24,true);
        TextLayout.drag(dragging,bounds,event.x(),event.y(),offsetX,offsetY,width,height-24,true);return true;
    }
    @Override public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event){if(dragging!=null){dragging=null;return true;}return super.mouseReleased(event);}
    @Override public void onClose(){if(!fields())return;if(mod.saveConfig()){if(parent instanceof DashboardScreen dashboard)dashboard.checkpoint();net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}else notice="Save failed. Retry Done or use Cancel.";}
    private void cancel(){editor.cancel();net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
