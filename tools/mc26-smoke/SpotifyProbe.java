package dev.dsmp.economy.probe;
import dev.dsmp.economy.*;
import dev.dsmp.economy.ui.*;
import dev.dsmp.economy.design.*;
import dev.dsmp.economy.menubackground.*;
import dev.dsmp.economy.inventory.*;
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
public final class SpotifyProbe implements ClientModInitializer {
 private EconomyTrackerClient mod;private DashboardScreen host;private int ticks,step,checks;private long deadline;private MenuBackgroundOptions original;private InventoryOptions inventory;private String ledger;private int texts;private dev.dsmp.economy.spotify.SpotifyOptions originalSpotify;
 private static final String[] NAV={"Overview","HUD modules","Appearance","Text boxes","HUD layout","Loans","Tracking","Settings","Auctions","Inventory themes","Player background","Design tools","Spotify"};
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
  check(Minecraft.getInstance().gui.screen()==host,"Sidebar replaced by "+Minecraft.getInstance().gui.screen().getClass());check(host.children().stream().filter(e->e instanceof SettingsButton b&&Arrays.asList(NAV).contains(b.getMessage().getString())).count()==13,"Sidebar sections disappeared");
  var active=MenuNavigation.active();for(var c:active.children())if(c instanceof AbstractWidget w){check(w.getX()>=0&&w.getY()>=0&&w.getRight()<=active.width&&w.getBottom()<=active.height,"Offscreen panel widget "+w.getMessage().getString());if(w instanceof SettingsButton b)check(b.fullLabelFits(),"Panel label clipped");}var widgets=active.children().stream().filter(e->e instanceof AbstractWidget).map(e->(AbstractWidget)e).toList();for(var a:widgets)for(var b:widgets)if(a!=b)check(a.getX()>=b.getRight()||b.getX()>=a.getRight()||a.getY()>=b.getBottom()||b.getY()>=a.getBottom(),"Overlapping Spotify controls");checks++;
 }
 public void onInitializeClient(){
  ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{if(screen instanceof DashboardScreen)ScreenEvents.afterExtract(screen).register((s,g,mx,my,delta)->g.text(client.font,"NATIVE 26.3 SPOTIFY TEST - SAMPLE DATA",8,client.getWindow().getGuiScaledHeight()-10,0xFFFFFFFF));});
  ClientLifecycleEvents.CLIENT_STARTED.register(client->{try{mod=new EconomyTrackerClient();DesignManager.initialize(mod);original=MenuBackgrounds.current();inventory=InventoryThemes.current();ledger=DesignLook.JSON.toJson(mod.ledger.snapshot());originalSpotify=dev.dsmp.economy.spotify.Spotify.service().options();host=new DashboardScreen(mod);client.gui.setScreen(host);deadline=System.currentTimeMillis()+150000;Files.deleteIfExists(game().resolve("probe-result.txt"));}catch(Exception e){throw new RuntimeException(e);}});
  ClientTickEvents.END_CLIENT_TICK.register(client->{if(mod==null||++ticks%15!=0||client.gui.overlay()!=null)return;try{if(System.currentTimeMillis()>deadline)throw new AssertionError("Panel smoke timeout at "+step);run(client);}catch(Throwable ex){ex.printStackTrace();try{Files.writeString(game().resolve("probe-result.txt"),"FAIL "+ex);}catch(Exception ignored){}mod=null;client.stop();}});
 }
 private void run(Minecraft client)throws Exception{
  switch(step){
   case 0->{if(client.gui.screen()!=host){client.gui.setScreen(host);return;}var access=DesignAccess.current();for(int scale:new int[]{100,115,130}){var next=new AccessOptions();next.textScale=scale;next.reducedMotion=true;DesignAccess.install(next);for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720}}){host.init(size[0],size[1]);nav("Spotify");inspect();click("Overlay");inspect();}}DesignAccess.install(access);host.init(client.getWindow().getGuiScaledWidth(),client.getWindow().getGuiScaledHeight());nav("Spotify");click("Account");step++;}
   case 1->{Screenshot.grab(client,false);var field=MenuNavigation.active().children().stream().filter(e->e instanceof EditBox).map(e->(EditBox)e).findFirst().orElseThrow();field.setValue("invalid");click("Connect Spotify");check(!dev.dsmp.economy.spotify.Spotify.service().connecting(),"Invalid ID opened sign-in");clickWidget(MenuNavigation.active(),field);field.moveCursorToEnd(false);check(host.charTyped(new CharacterEvent('X')),"Spotify ID typing did not route through panel");click("Cancel");check(dev.dsmp.economy.spotify.Spotify.service().options().equals(originalSpotify),"Account Cancel saved invalid ID");nav("Spotify");click("Overlay");step++;}
   case 2->{Screenshot.grab(client,false);var options=dev.dsmp.economy.spotify.Spotify.service().options();if(!options.enabled())click("Overlay: off");click("Middle right");click("Larger");click("Color: "+dev.dsmp.economy.spotify.SpotifyOptions.PALETTES[options.palette()]);click("Done");var saved=dev.dsmp.economy.spotify.Spotify.service().options();check(saved.enabled()&&saved.position()==5&&saved.scale()==Math.min(150,options.scale()+10),"Spotify overlay preferences did not apply");check(dev.dsmp.economy.spotify.SpotifyOptions.load(game().resolve("config/economy-tracker/spotify.json")).equals(saved),"Spotify options did not persist");nav("Spotify");click("Overlay");click("Top left");click("Smaller");click("Cancel");check(saved.equals(dev.dsmp.economy.spotify.Spotify.service().options()),"Overlay Cancel saved appearance");nav("Spotify");click("Overlay");var trackField=dev.dsmp.economy.spotify.SpotifyService.class.getDeclaredField("track");trackField.setAccessible(true);trackField.set(dev.dsmp.economy.spotify.Spotify.service(),new dev.dsmp.economy.spotify.SpotifyApi.Track("Sample track / offline test","Sample artist","",true,42000,180000,System.currentTimeMillis()));step++;}
   case 3->{Screenshot.grab(client,false);check(client.gui.screen()==host&&MenuNavigation.active() instanceof SpotifyScreen,"Spotify section replaced sidebar");check(ledger.equals(DesignLook.JSON.toJson(mod.ledger.snapshot())),"Spotify changed recorded data");client.gui.setScreen(new Screen(net.minecraft.network.chat.Component.literal("Offline Spotify HUD test")){@Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta){g.fill(0,0,width,height,0xFF162623);g.text(client.font,"NATIVE 26.3 SPOTIFY HUD - OFFLINE SAMPLE",12,12,0xFFFFFFFF);dev.dsmp.economy.ui.SpotifyHud.draw(g,width,height);}});step++;}
   case 4->{Screenshot.grab(client,false);var trackField=dev.dsmp.economy.spotify.SpotifyService.class.getDeclaredField("track");trackField.setAccessible(true);trackField.set(dev.dsmp.economy.spotify.Spotify.service(),null);dev.dsmp.economy.spotify.Spotify.service().save(originalSpotify);Files.writeString(game().resolve("probe-result.txt"),"PASS "+checks+" Spotify layouts at four sizes and three text scales; real right-panel pointer/text routing, invalid ID, Account/Overlay tabs, controls, save/reload, Cancel, sample track and actual HUD GPU render and independent data. No real Spotify account used.");mod=null;client.stop();}
  }
 }
}
