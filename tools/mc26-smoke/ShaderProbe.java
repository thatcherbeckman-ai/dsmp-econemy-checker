package dev.dsmp.economy.probe;
import dev.dsmp.economy.*;
import dev.dsmp.economy.ui.*;
import dev.dsmp.economy.design.*;
import dev.dsmp.economy.menubackground.*;
import dev.dsmp.economy.inventory.*;
import dev.dsmp.economy.shaders.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.*;
import java.util.*;

/** Offline, test-only checks for persistent dashboard navigation and real menu-background drafts. */
public final class ShaderProbe implements ClientModInitializer {
 private EconomyTrackerClient mod;private DashboardScreen host;private int ticks,step,checks;private long deadline;private MenuBackgroundOptions original;private InventoryOptions inventory;private String ledger;private int texts;private ShaderSettingsScreen shaderPage;private boolean enabled;private int toggles;private String samplePack="None";private Object returnParent;
 private static final String[] NAV={"Overview","HUD modules","Appearance","Text boxes","HUD layout","Loans","Tracking","Settings","Auctions","Inventory themes","Player background","Design tools","Spotify","Shaders"};
 private Path game(){return Path.of(System.getProperty("user.dir"));}
 private static void check(boolean ok,String reason){if(!ok)throw new AssertionError(reason);}
 private float slide(WindowedScreen screen)throws Exception{var m=WindowedScreen.class.getDeclaredMethod("tabOffset");m.setAccessible(true);return (float)m.invoke(screen);}
 private void click(String label)throws Exception{click(label,false);}
 private void nav(String label)throws Exception{click(label,true);}
 private void click(String label,boolean navigation)throws Exception{
  var page=navigation?host:MenuNavigation.active();var button=page.children().stream().filter(e->e instanceof Button b&&b.getMessage().getString().equals(label)).map(e->(Button)e).findFirst().orElseThrow(()->new AssertionError("Missing "+label+" in "+page.getClass()));clickWidget(page,button);
 }
 private void clickWidget(Screen page,AbstractWidget widget)throws Exception{
  double x=widget.getX()+widget.getWidth()/2d,y=widget.getY()+widget.getHeight()/2d;var d=MenuLayout.Dashboard.of(host.width,host.height);
  if(page!=host){var p=PanelViewport.of(d.left(),d.bodyTop(),d.contentWidth(),host.height-d.bodyTop()-28);x=p.x(x+slide((WindowedScreen)page))+slide(host);y=p.y(y);}else if(x>=d.left()&&y>=d.bodyTop())x+=slide(host);
  var w=host.menuWindow();var event=new MouseButtonEvent(w.screenX(x),w.screenY(y),new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT,0));check(host.mouseClicked(event,false),"Pointer did not activate "+widget.getMessage().getString());host.mouseReleased(event);
 }
 private void inspect()throws Exception{
  check(Minecraft.getInstance().gui.screen()==host,"Sidebar replaced by "+Minecraft.getInstance().gui.screen().getClass());check(host.children().stream().filter(e->e instanceof SettingsButton b&&Arrays.asList(NAV).contains(b.getMessage().getString())).count()==14,"Sidebar sections disappeared");
  var active=MenuNavigation.active();for(var c:active.children())if(c instanceof AbstractWidget w){check(w.getX()>=0&&w.getY()>=0&&w.getRight()<=active.width&&w.getBottom()<=active.height,"Offscreen panel widget "+w.getMessage().getString());if(w instanceof SettingsButton b)check(b.fullLabelFits(),"Panel label clipped");}checks++;
 }
 public void onInitializeClient(){
  ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{if(screen instanceof DashboardScreen)ScreenEvents.afterExtract(screen).register((s,g,mx,my,delta)->g.text(client.font,"NATIVE 26.3 IRIS API TEST - SAMPLE DATA / NO REAL SHADER",8,client.getWindow().getGuiScaledHeight()-10,0xFFFFFFFF));});
  ClientLifecycleEvents.CLIENT_STARTED.register(client->{try{mod=new EconomyTrackerClient();DesignManager.initialize(mod);original=MenuBackgrounds.current();inventory=InventoryThemes.current();ledger=DesignLook.JSON.toJson(mod.ledger.snapshot());host=new DashboardScreen(mod);client.gui.setScreen(host);deadline=System.currentTimeMillis()+150000;Files.deleteIfExists(game().resolve("probe-result.txt"));}catch(Exception e){throw new RuntimeException(e);}});
  ClientTickEvents.END_CLIENT_TICK.register(client->{if(mod==null||++ticks%15!=0||client.gui.overlay()!=null)return;try{if(System.currentTimeMillis()>deadline)throw new AssertionError("Panel smoke timeout at "+step);run(client);}catch(Throwable ex){ex.printStackTrace();try{Files.writeString(game().resolve("probe-result.txt"),"FAIL "+ex);}catch(Exception ignored){}mod=null;client.stop();}});
 }
 public interface Config {boolean areShadersEnabled();void setShadersEnabledAndApply(boolean value);}
 public interface Api {static Api getInstance(){return activeApi;}Config getConfig();boolean isShaderPackInUse();Object openMainIrisScreenObj(Object parent);}
 private static Api activeApi;
 private ShaderSettingsScreen mockPage(){activeApi=new Api(){final Config config=new Config(){public boolean areShadersEnabled(){return enabled;}public void setShadersEnabledAndApply(boolean value){check(Thread.currentThread().getName().equals("Render thread"),"Iris GPU change was moved off render thread");enabled=value;toggles++;}};public Config getConfig(){return config;}public boolean isShaderPackInUse(){return enabled&&!samplePack.equals("None");}public Object openMainIrisScreenObj(Object parent){System.out.println("SHADER_PROBE_OPEN");returnParent=parent;return new SamplePicker((Screen)parent);}};return new ShaderSettingsScreen(mod,host,()->IrisBridge.fromApiTypes("API contract test / no live Iris",Api.class,Config.class));}
 private final class SamplePicker extends Screen {
  final Screen parent;SamplePicker(Screen parent){super(net.minecraft.network.chat.Component.literal("TEST PICKER - NO REAL IRIS"));this.parent=parent;}
  @Override protected void init(){addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.literal("Select sample pack"),b->{samplePack="Sample shader (test only)";enabled=true;System.out.println("SHADER_PROBE_SELECTED");}).bounds(width/2-100,height/2-22,200,20).build());addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.literal("Done"),b->onClose()).bounds(width/2-100,height/2+4,200,20).build());}
  @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,float d){g.fill(0,0,width,height,0xFF102022);g.text(Minecraft.getInstance().font,"IRIS PUBLIC API ROUTING TEST - NO REAL SHADERPACK",12,12,0xFFFFFFFF);super.extractRenderState(g,x,y,d);}
  @Override public void onClose(){System.out.println("SHADER_PROBE_CLOSE_TO "+parent.getClass().getName());Minecraft.getInstance().gui.setScreen(parent);}
 }
 private void external(String label){var screen=Minecraft.getInstance().gui.screen();var b=screen.children().stream().filter(e->e instanceof Button button&&button.getMessage().getString().equals(label)).map(e->(Button)e).findFirst().orElseThrow();var event=new MouseButtonEvent(b.getX()+b.getWidth()/2d,b.getY()+b.getHeight()/2d,new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT,0));check(screen.mouseClicked(event,false),"External picker control failed");screen.mouseReleased(event);}
 private void run(Minecraft client)throws Exception{
  switch(step){
   case 0->{if(client.gui.screen()!=host){client.gui.setScreen(host);return;}var access=DesignAccess.current();for(int scale:new int[]{100,115,130}){var next=new AccessOptions();next.textScale=scale;next.reducedMotion=true;DesignAccess.install(next);for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720}}){host.init(size[0],size[1]);nav("Shaders");inspect();var button=MenuNavigation.active().children().stream().filter(e->e instanceof Button b&&b.getMessage().getString().equals("Shader packs & options")).map(e->(Button)e).findFirst().orElseThrow();check(!button.active,"Missing Iris enabled pack picker");check(IrisIntegration.current().availability()==IrisBridge.Availability.ABSENT,"Isolated profile unexpectedly has Iris");}}DesignAccess.install(access);host.init(client.getWindow().getGuiScaledWidth(),client.getWindow().getGuiScaledHeight());nav("Shaders");step++;}
   case 1->{Screenshot.grab(client,false);shaderPage=mockPage();MenuNavigation.open(shaderPage);var access=DesignAccess.current();for(int scale:new int[]{100,115,130}){var next=new AccessOptions();next.textScale=scale;next.reducedMotion=true;DesignAccess.install(next);for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720}}){host.init(size[0],size[1]);inspect();}}DesignAccess.install(access);host.init(client.getWindow().getGuiScaledWidth(),client.getWindow().getGuiScaledHeight());check(toggles==0,"Opening Shaders changed Iris settings");click("Shaders on");check(enabled&&toggles==1,"Public Iris toggle did not apply");click("Shaders on");check(toggles==1,"Same-state click reloaded shaders");click("Shaders off");check(!enabled&&toggles==2,"Iris off failed");step++;}
   case 2->{Screenshot.grab(client,false);click("Shader packs & options");check(client.gui.screen() instanceof SamplePicker,"Public Iris factory did not open its own screen");check(returnParent instanceof IrisReturnScreen,"Iris parent is not a safe dashboard return target");step++;}
   case 3->{external("Select sample pack");check(enabled&&samplePack.startsWith("Sample"),"Native picker selection did not apply");step++;}
   case 4->{external("Done");step++;}
   case 5->{check(client.gui.screen()==host&&MenuNavigation.active()==shaderPage,"Iris Done did not restore same menu/panel: actual="+client.gui.screen().getClass().getName()+" active="+MenuNavigation.active().getClass().getName()+" sameHost="+(client.gui.screen()==host)+" samePage="+(MenuNavigation.active()==shaderPage));check(enabled&&samplePack.startsWith("Sample"),"Native picker changes lost on return");inspect();Screenshot.grab(client,false);click("Shader packs & options");check(client.gui.screen() instanceof SamplePicker,"Repeated Iris open failed");step++;}
   case 6->{client.gui.screen().keyPressed(new KeyEvent(InputConstants.KEY_ESCAPE,0,0));step++;}
   case 7->{check(client.gui.screen()==host&&MenuNavigation.active()==shaderPage,"Iris Esc did not restore sidebar");click("Cancel");check(enabled,"Menu Cancel undid immediate Iris changes");nav("Shaders");check(MenuNavigation.active() instanceof ShaderSettingsScreen,"Shaders sidebar did not reopen");check(ledger.equals(DesignLook.JSON.toJson(mod.ledger.snapshot())),"Iris controls changed recorded data");Files.writeString(game().resolve("probe-result.txt"),"PASS "+checks+" Iris menu layouts/returns at four sizes and three text scales; optional missing-Iris state, disabled picker, public API fixture on/off/same-state handling on render thread, full-screen picker/selection, Done/Esc and repeated return to same panel, immediate-action Cancel semantics and unchanged data. Public API signatures checked against official Iris 26.3 source; real Iris/shader rendering is untested.");mod=null;client.stop();}
  }
 }
}
