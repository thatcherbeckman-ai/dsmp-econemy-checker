package dev.dsmp.economy.ui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.dsmp.economy.spotify.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;

/** Full-screen Spotify-only canvas using the live HUD's exact GUI coordinate system. */
public final class SpotifyPlacementScreen extends Screen {
    private final Screen parent;private final Consumer<SpotifyOptions> accept;private final int accent;
    private SpotifyOptions draft;private final Map<AbstractWidget,Integer> rows=new LinkedHashMap<>();
    private SettingsEditBox xField,yField,sizeField;private boolean dragging,fieldsDirty,synchronizing;private double grabX,grabY;
    private int toolbarX,toolbarY,toolbarWidth,cell;
    private String notice="Drag the box. Arrows: 1 px; Shift: 10 px. Wheel on box: resize. Esc cancels.";
    public SpotifyPlacementScreen(Screen parent,SpotifyOptions options,int accent,Consumer<SpotifyOptions> accept){super(Component.literal("Spotify position and size"));this.parent=parent;this.draft=options;this.accent=accent;this.accept=accept;}
    public SpotifyOptions previewOptions(){return draft;}
    public SpotifyPlacement.Bounds previewBounds(){return SpotifyPlacement.bounds(draft,width,height);}
    @Override protected void init(){
        rows.clear();dragging=false;fieldsDirty=false;toolbarWidth=Math.min(620,Math.max(120,width-16));toolbarX=(width-toolbarWidth)/2;cell=(toolbarWidth-20)/6;
        button(0,0,"Save",true,this::save);button(1,0,"Cancel",false,this::onClose);button(2,0,"-5%",false,()->resize(-5));
        sizeField=field(3,0,0,"Size percentage, 25 to 150");button(4,0,"+5%",false,()->resize(5));button(5,0,"Apply",false,this::applyFields);
        xField=field(0,1,10,"X position in screen pixels");yField=field(1,1,10,"Y position in screen pixels");
        button(2,1,"Left",false,()->move(-step(),0));button(3,1,"Right",false,()->move(step(),0));button(4,1,"Up",false,()->move(0,-step()));button(5,1,"Down",false,()->move(0,step()));
        layoutControls();syncFields();
    }
    private void button(int column,int row,String label,boolean selected,Runnable action){var b=new SettingsButton(toolbarX+column*(cell+4),0,cell,20,label,selected,accent,action);addRenderableWidget(b);rows.put(b,row*24+b.getY());}
    private SettingsEditBox field(int column,int row,int indent,String label){var f=new SettingsEditBox(font,toolbarX+column*(cell+4)+indent,0,Math.max(14,cell-indent),20,Component.literal(label));f.setMaxLength(6);f.setTooltip(Tooltip.create(Component.literal(label)));f.setResponder(value->{if(!synchronizing)fieldsDirty=true;});addRenderableWidget(f);rows.put(f,row*24);return f;}
    private void layoutControls(){var b=previewBounds();toolbarY=b.y()+b.height()/2<height/2?Math.max(4,height-70):6;rows.forEach((widget,row)->widget.setY(toolbarY+6+row));}
    private void syncFields(){if(xField==null||fieldsDirty)return;var b=previewBounds();synchronizing=true;try{if(!xField.isFocused())xField.setValue(Integer.toString(b.x()));if(!yField.isFocused())yField.setValue(Integer.toString(b.y()));if(!sizeField.isFocused())sizeField.setValue(Integer.toString(draft.scale()));}finally{synchronizing=false;}}
    private static int step(){return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)||InputConstants.isKeyDown(InputConstants.KEY_RSHIFT)?10:1;}
    private void move(int dx,int dy){draft=SpotifyPlacement.move(draft,dx,dy,width,height);setFocused(null);fieldsDirty=false;syncFields();layoutControls();}
    private void resize(int change){draft=SpotifyPlacement.resize(draft,draft.scale()+change,width,height);setFocused(null);fieldsDirty=false;syncFields();layoutControls();}
    private void applyFields(){try{int percent=Integer.parseInt(sizeField.getValue()),x=Integer.parseInt(xField.getValue()),y=Integer.parseInt(yField.getValue());draft=SpotifyPlacement.atPixels(draft.size(percent),x,y,width,height);setFocused(null);fieldsDirty=false;syncFields();layoutControls();notice="Exact position applied to preview. Save keeps it; Cancel discards it.";}catch(NumberFormatException ex){notice="Enter whole numbers in the size, X and Y fields, then Apply.";}}
    private void save(){if(fieldsDirty){applyFields();if(fieldsDirty)return;}try{Spotify.service().save(draft);accept.accept(draft);MenuNavigation.open(parent);}catch(java.io.IOException ex){notice="Could not save Spotify placement. Retry Save or use Cancel.";}}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        g.fill(0,0,width,height,0x65081216);
        for(int x=width/4;x<width;x+=Math.max(1,width/4))g.fill(x,0,x+1,height,0x12C8EEE1);
        for(int y=height/4;y<height;y+=Math.max(1,height/4))g.fill(0,y,width,y+1,0x12C8EEE1);
        var b=previewBounds();var current=Spotify.service().track();var track=current==null?new SpotifyApi.Track("Your song title","Artist name","",true,42_000,180_000,System.currentTimeMillis()):current;
        g.pose().pushMatrix();g.pose().translate(b.x(),b.y());g.pose().scale(b.scale());try{SpotifyHud.card(g,draft,track,"",current==null);}finally{g.pose().popMatrix();}
        if(dragging||b.contains(mx,my))UiSurfaces.cardOutline(g,b.x()-1,b.y()-1,b.width()+2,b.height()+2,0xFF7CE3B4);
        layoutControls();syncFields();UiSurfaces.card(g,toolbarX-4,toolbarY,toolbarWidth+8,64,0xEF172021);
        FestiveUi.text(g,"X",toolbarX+1,toolbarY+36,0xFFB2CFC4,.6f,true);FestiveUi.text(g,"Y",toolbarX+cell+5,toolbarY+36,0xFFB2CFC4,.6f,true);
        FestiveUi.textFit(g,"X: "+b.x()+" px   Y: "+b.y()+" px   Size: "+draft.scale()+"%   |   "+notice,toolbarX+4,toolbarY+55,toolbarWidth-8,0xFFBCD3C9,.62f,false);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubled){layoutControls();if(super.mouseClicked(event,doubled))return true;if(event.button()!=InputConstants.MOUSE_BUTTON_LEFT)return false;var b=previewBounds();if(!b.contains(event.x(),event.y()))return false;setFocused(null);fieldsDirty=false;dragging=true;grabX=event.x()-b.x();grabY=event.y()-b.y();return true;}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){if(!dragging)return super.mouseDragged(event,dx,dy);draft=SpotifyPlacement.atPixels(draft,event.x()-grabX,event.y()-grabY,width,height);syncFields();layoutControls();return true;}
    @Override public boolean mouseReleased(MouseButtonEvent event){if(dragging){dragging=false;return true;}return super.mouseReleased(event);}
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){if(previewBounds().contains(x,y)&&dy!=0){resize((dy>0?1:-1)*(step()==10?1:5));return true;}return super.mouseScrolled(x,y,dx,dy);}
    @Override public boolean keyPressed(KeyEvent event){
        boolean editing=getFocused() instanceof SettingsEditBox;
        if(editing&&event.key()==InputConstants.KEY_RETURN){applyFields();return true;}
        if(!editing){int step=event.hasShiftDown()?10:1;switch(event.key()){case InputConstants.KEY_LEFT-> {move(-step,0);return true;}case InputConstants.KEY_RIGHT->{move(step,0);return true;}case InputConstants.KEY_UP->{move(0,-step);return true;}case InputConstants.KEY_DOWN->{move(0,step);return true;}}}
        return super.keyPressed(event);
    }
    @Override public void onClose(){MenuNavigation.open(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
