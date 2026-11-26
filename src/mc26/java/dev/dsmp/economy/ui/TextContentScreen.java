package dev.dsmp.economy.ui;
import dev.dsmp.economy.*;
import dev.dsmp.economy.text.*;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Native multiline editing: Enter inserts a real line break; only valid drafts reach the preview. */
public final class TextContentScreen extends WindowedScreen {
    private final EconomyTrackerClient mod;private final Screen parent;private final TextBox box,original;private MultiLineEditBox input;private String draft,notice="Enter makes a new line. Done keeps your draft; parent Done saves it.";
    public TextContentScreen(EconomyTrackerClient mod,Screen parent,TextBox box){super(Component.literal("Text content"));this.mod=mod;this.parent=parent;this.box=box;original=box.copy();draft=box.text;}
    @Override protected void initMenu(){var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,()->{box.restore(original);MenuNavigation.open(parent);}));
        input=MultiLineEditBox.builder().setX(m.margin()).setY(m.top()+8).setPlaceholder(Component.literal("Type your text here. Press Enter for a new line.")).setTextColor(0xFFF0E6EB).setTextShadow(false).setShowBackground(false).setShowDecorations(false).build(font,m.contentWidth(),Math.max(82,(height-m.top())/2-10),Component.literal("Text content"));input.setCharacterLimit(1024);input.setLineLimit(24);input.setValue(draft);input.setValueListener(value->{draft=value;apply();});addRenderableWidget(input);
    }
    private boolean apply(){try{var next=original.copy();next.text=draft;next.typography=true;next.trimStyles();next.validate();box.restore(next);notice=next.lines().length+" / 24 lines | "+draft.length()+" / 1024 characters";return true;}catch(IllegalArgumentException ex){notice=ex.getMessage();return false;}}
    @Override protected void drawMenu(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta){var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,"Text content / real multiline editor");UiSurfaces.fill(g,input.getX(),input.getY(),input.getWidth(),input.getHeight(),0xF51B131B);UiSurfaces.outline(g,input.getX(),input.getY(),input.getWidth(),input.getHeight(),input.isFocused()?0xFFBC7775:0x805E4148);int y=m.top()+8+Math.max(82,(height-m.top())/2-10)+12;FestiveUi.panel(g,m.margin(),y,m.contentWidth(),height-y-30,mod.config.menuAccent);FestiveUi.text(g,"LIVE PREVIEW",m.margin()+10,y+8,0xFFB7A5B4,.65f,true);StyledTextHud.preview(g,box,m.margin()+10,y+25,m.contentWidth()-20,height-y-60);m.footer(g,notice);super.drawMenu(g,mx,my,delta);}
    @Override public void onClose(){if(apply())MenuNavigation.open(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
