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
public final class DisplayProbe implements ClientModInitializer {
 private EconomyTrackerClient mod;private DashboardScreen host;private int ticks,step,checks;private long deadline;private MenuBackgroundOptions original;private InventoryOptions inventory;private String ledger;private int texts;private AccessOptions access;
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
  ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{if(screen instanceof DashboardScreen)ScreenEvents.afterExtract(screen).register((s,g,mx,my,delta)->g.text(client.font,"NATIVE 26.3 DISPLAY TEST - OFFLINE SAMPLE SKIN",8,client.getWindow().getGuiScaledHeight()-10,0xFFFFFFFF));});
  ClientLifecycleEvents.CLIENT_STARTED.register(client->{try{mod=new EconomyTrackerClient();DesignManager.initialize(mod);original=MenuBackgrounds.current();access=DesignAccess.current();DesignAccess.install(new AccessOptions());inventory=InventoryThemes.current();ledger=DesignLook.JSON.toJson(mod.ledger.snapshot());host=new DashboardScreen(mod);client.gui.setScreen(host);deadline=System.currentTimeMillis()+150000;Files.deleteIfExists(game().resolve("probe-result.txt"));}catch(Exception e){throw new RuntimeException(e);}});
  ClientTickEvents.END_CLIENT_TICK.register(client->{if(mod==null||++ticks%15!=0||client.gui.overlay()!=null)return;try{if(System.currentTimeMillis()>deadline)throw new AssertionError("Panel smoke timeout at "+step);run(client);}catch(Throwable ex){ex.printStackTrace();try{Files.writeString(game().resolve("probe-result.txt"),"FAIL "+ex);}catch(Exception ignored){}mod=null;client.stop();}});
 }
 private MenuBackgroundOptions draft(){return ((MenuBackgroundScreen)MenuNavigation.active()).previewOptions();}
 private void openDisplay()throws Exception{nav("Settings");click("Backgrounds");click("Display");}
 private void run(Minecraft client)throws Exception{
  switch(step){
   case 0->{if(client.gui.screen()!=host){client.gui.setScreen(host);return;}for(int scale:new int[]{100,115,130}){var next=new AccessOptions();next.textScale=scale;next.reducedMotion=true;DesignAccess.install(next);for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720}}){host.init(size[0],size[1]);openDisplay();for(String section:new String[]{"Character","Transparency","Palettes"}){click(section);inspect();}}}DesignAccess.install(new AccessOptions());host.init(client.getWindow().getGuiScaledWidth(),client.getWindow().getGuiScaledHeight());openDisplay();click("Palettes");click("Aurora");click("Show palette background");step++;}
   case 1->{Screenshot.grab(client,false);var method=CharacterSceneRenderer.class.getDeclaredMethod("mouseAvatar",MenuDisplay.Look.class);method.setAccessible(true);var a=(net.minecraft.client.renderer.entity.state.AvatarRenderState)method.invoke(null,new MenuDisplay.Look(195,25,-20));var b=(net.minecraft.client.renderer.entity.state.AvatarRenderState)method.invoke(null,new MenuDisplay.Look(165,-25,20));check(a!=b&&a.skin!=null&&b.skin!=null,"Avatar states/own-skin fallback invalid");check(a.bodyRot==195&&a.yRot==25&&a.xRot==-20&&b.bodyRot==165,"Second avatar changed earlier render state");check(a.pose==net.minecraft.world.entity.Pose.STANDING&&!a.isInvisible&&a.lightCoords==((15<<4)|(15<<20)),"Avatar pose/light invalid");click("Character");if(draft().miniAvatar)click("Mini avatar: on");if(draft().avatarFollowsMouse)click("Follow mouse: on");click("Avatar size +");check(!MenuBackgrounds.visible().miniAvatar&&!MenuBackgrounds.visible().avatarFollowsMouse,"Avatar live toggles did not preview");click("Cancel");check(MenuBackgrounds.visible().miniAvatar==original.miniAvatar&&MenuBackgrounds.visible().avatarSize==original.avatarSize,"Avatar Cancel did not restore current options");openDisplay();click("Transparency");click("Soft glass");check(draft().textOpacity==85&&draft().cards==35,"Transparency preset did not update live draft");step++;}
   case 2->{Screenshot.grab(client,false);click("Text -");click("Panels -");click("Done");var saved=MenuBackgrounds.current();check(saved.textOpacity==80&&saved.cards==30,"Opacity save failed");var disk=MenuBackgrounds.store().load();check(disk.textOpacity==80&&disk.cards==30,"Opacity did not persist");openDisplay();click("Palettes");click("Candy");click("Show palette background");check(MenuBackgrounds.visible().palette==MenuPalette.CANDY&&MenuBackgrounds.current().palette==saved.palette,"Palette preview leaked into saved settings");step++;}
   case 3->{Screenshot.grab(client,false);click("Done");check(MenuBackgrounds.current().palette==MenuPalette.CANDY&&MenuBackgrounds.store().load().palette==MenuPalette.CANDY,"Palette save/reload failed");openDisplay();click("Character");if(!draft().miniAvatar)click("Mini avatar: off");if(!draft().avatarFollowsMouse)click("Follow mouse: off");click("Avatar size +");int size=draft().avatarSize;click("Done");check(MenuBackgrounds.current().miniAvatar&&MenuBackgrounds.current().avatarFollowsMouse&&MenuBackgrounds.store().load().avatarSize==size,"Character preferences did not persist");openDisplay();click("Palettes");click("Mint");click("Show palette background");step++;}
   case 4->{Screenshot.grab(client,false);click("Sky");step++;}
   case 5->{Screenshot.grab(client,false);click("Sunset");step++;}
   case 6->{Screenshot.grab(client,false);click("Classic");step++;}
   case 7->{Screenshot.grab(client,false);click("Cancel");check(MenuBackgrounds.current().palette==MenuPalette.CANDY,"Palette Cancel lost saved look");openDisplay();click("Transparency");var next=new AccessOptions();next.highContrast=true;DesignAccess.install(next);click("Soft glass");check(DesignAccess.contrast()&&MenuBackgrounds.visible().textOpacity==85,"High-contrast preference test failed");step++;}
   case 8->{Screenshot.grab(client,false);click("Cancel");DesignAccess.install(new AccessOptions());check(ledger.equals(DesignLook.JSON.toJson(mod.ledger.snapshot())),"Cosmetic menu settings changed recorded data");check(InventoryThemes.current().equals(inventory),"Menu display changed inventory settings");MenuBackgrounds.save(original);DesignAccess.install(access);Files.writeString(game().resolve("probe-result.txt"),"PASS "+checks+" menu display layouts at four sizes and three text scales; real own-skin PIP avatar, independent snapshots/pose/light, live avatar/follow/size, palette and opacity preview, Save/reload/Cancel, all six palette GPU textures, high-contrast display, inventory independence and unchanged data. Offline sample profile.");mod=null;client.stop();}
  }
 }
}
