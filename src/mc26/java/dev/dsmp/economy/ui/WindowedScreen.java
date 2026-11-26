package dev.dsmp.economy.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/** Keeps native widgets in one coordinate space; rendering and every pointer event use its inverse. */
public abstract class WindowedScreen extends Screen {
    private MenuWindow window;private final TabMotion motion=new TabMotion();
    protected WindowedScreen(Component title){super(title);}
    public final MenuWindow menuWindow(){return window;}
    private void configure(){window=MenuWindow.of(width,height);width=window.width();height=window.height();}
    @Override protected final void init(){if(window==null){configure();motion.start(System.nanoTime(),1);}initMenu();}
    @Override protected final void repositionElements(){configure();super.rebuildWidgets();}
    protected abstract void initMenu();
    protected boolean slideWholeMenu(){return true;}
    protected final float tabOffset(){return motion.offset(System.nanoTime(),dev.dsmp.economy.design.DesignAccess.reduced());}
    protected final void tabTransition(int direction){motion.start(System.nanoTime(),direction);minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BUBBLE_POP,1.15f,.32f));}
    @Override public final void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){}
    @Override public final void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        if(window==null)return;
        g.fill(0,0,window.viewportWidth(),window.viewportHeight(),0x35000000);
        int x=Math.round(window.x()),y=Math.round(window.y()),w=Math.round(width*window.scale()),h=Math.round(height*window.scale());
        UiSurfaces.shadow(g,x-2,y-2,w+4,h+4);UiSurfaces.fill(g,x-2,y-2,w+4,h+4,0xED351417);UiSurfaces.outline(g,x-2,y-2,w+4,h+4,0xB57E3837);
        g.enableScissor(x,y,x+w,y+h);g.pose().pushMatrix();g.pose().translate(window.x(),window.y());g.pose().scale(window.scale());
        FestiveUi.windowed(true);
        try{float offset=slideWholeMenu()?tabOffset():0;g.pose().pushMatrix();g.pose().translate(offset,0);try{drawMenu(g,(int)(window.localX(mx)-offset),(int)window.localY(my),delta);}finally{g.pose().popMatrix();}MenuChrome.lights(g,width,height);}
        finally{FestiveUi.windowed(false);g.pose().popMatrix();g.disableScissor();}
    }
    protected void drawMenu(GuiGraphicsExtractor g,int mx,int my,float delta){super.extractRenderState(g,mx,my,delta);}
    private MouseButtonEvent local(MouseButtonEvent event){return new MouseButtonEvent(window.localX(event.x())-(slideWholeMenu()?tabOffset():0),window.localY(event.y()),event.buttonInfo());}
    @Override public final boolean mouseClicked(MouseButtonEvent event,boolean doubled){return window!=null&&window.contains(event.x(),event.y())&&menuMouseClicked(local(event),doubled);}
    protected boolean menuMouseClicked(MouseButtonEvent event,boolean doubled){return super.mouseClicked(event,doubled);}
    @Override public final boolean mouseDragged(MouseButtonEvent event,double dx,double dy){return window!=null&&menuMouseDragged(local(event),dx/window.scale(),dy/window.scale());}
    protected boolean menuMouseDragged(MouseButtonEvent event,double dx,double dy){return super.mouseDragged(event,dx,dy);}
    @Override public final boolean mouseReleased(MouseButtonEvent event){return window!=null&&menuMouseReleased(local(event));}
    protected boolean menuMouseReleased(MouseButtonEvent event){return super.mouseReleased(event);}
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){return window!=null&&window.contains(x,y)&&super.mouseScrolled(window.localX(x)-(slideWholeMenu()?tabOffset():0),window.localY(y),dx,dy);}
    @Override public void mouseMoved(double x,double y){if(window!=null)super.mouseMoved(window.localX(x)-(slideWholeMenu()?tabOffset():0),window.localY(y));}
    @Override public boolean isMouseOver(double x,double y){return window!=null&&window.contains(x,y);}
}
