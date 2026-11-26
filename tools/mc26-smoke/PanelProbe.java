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
public final class PanelProbe implements ClientModInitializer {
 private EconomyTrackerClient mod;private DashboardScreen host;private int ticks,step,checks;private long deadline;private MenuBackgroundOptions original;private InventoryOptions inventory;private String ledger;private int texts;
 private static final String[] NAV={"Overview","HUD modules","Appearance","Text boxes","HUD layout","Loans","Tracking","Settings","Auctions","Inventory themes","Player background","Design tools"};
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
  check(Minecraft.getInstance().gui.screen()==host,"Sidebar replaced by "+Minecraft.getInstance().gui.screen().getClass());check(host.children().stream().filter(e->e instanceof SettingsButton b&&Arrays.asList(NAV).contains(b.getMessage().getString())).count()==12,"Sidebar sections disappeared");
  var active=MenuNavigation.active();for(var c:active.children())if(c instanceof AbstractWidget w){check(w.getX()>=0&&w.getY()>=0&&w.getRight()<=active.width&&w.getBottom()<=active.height,"Offscreen panel widget "+w.getMessage().getString());if(w instanceof SettingsButton b)check(b.fullLabelFits(),"Panel label clipped");}checks++;
 }
 public void onInitializeClient(){
  ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{if(screen instanceof DashboardScreen)ScreenEvents.afterExtract(screen).register((s,g,mx,my,delta)->g.text(client.font,"NATIVE 26.3 PANEL TEST - SAMPLE DATA",8,client.getWindow().getGuiScaledHeight()-10,0xFFFFFFFF));});
  ClientLifecycleEvents.CLIENT_STARTED.register(client->{try{mod=new EconomyTrackerClient();DesignManager.initialize(mod);original=MenuBackgrounds.current();inventory=InventoryThemes.current();ledger=DesignLook.JSON.toJson(mod.ledger.snapshot());host=new DashboardScreen(mod);client.gui.setScreen(host);deadline=System.currentTimeMillis()+150000;Files.deleteIfExists(game().resolve("probe-result.txt"));}catch(Exception e){throw new RuntimeException(e);}});
  ClientTickEvents.END_CLIENT_TICK.register(client->{if(mod==null||++ticks%15!=0||client.gui.overlay()!=null)return;try{if(System.currentTimeMillis()>deadline)throw new AssertionError("Panel smoke timeout at "+step);run(client);}catch(Throwable ex){ex.printStackTrace();try{Files.writeString(game().resolve("probe-result.txt"),"FAIL "+ex);}catch(Exception ignored){}mod=null;client.stop();}});
 }
 private void run(Minecraft client)throws Exception{
  switch(step){
   case 0->{if(client.gui.screen()!=host){client.gui.setScreen(host);return;}var access=DesignAccess.current();for(int scale:new int[]{100,115,130}){var next=new AccessOptions();next.textScale=scale;next.reducedMotion=true;DesignAccess.install(next);for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720}}){host.init(size[0],size[1]);for(String section:NAV){nav(section);inspect();}}}DesignAccess.install(access);host.init(client.getWindow().getGuiScaledWidth(),client.getWindow().getGuiScaledHeight());nav("Appearance");step++;}
   case 1->{Screenshot.grab(client,false);nav("Text boxes");texts=mod.config.textBoxes.size();click("New");var page=MenuNavigation.active();var field=page.children().stream().filter(e->e instanceof EditBox).map(e->(EditBox)e).findFirst().orElseThrow();clickWidget(page,field);field.moveCursorToEnd(false);check(host.charTyped(new CharacterEvent('X')),"Embedded text input did not route to field");click("Lines");check(MenuNavigation.active() instanceof LineStylesScreen,"Nested line editor lost host");inspect();click("Cancel");check(MenuNavigation.active() instanceof TextBoxesScreen,"Nested Cancel lost text editor");click("Cancel");check(mod.config.textBoxes.size()==texts,"Text draft Cancel changed saved boxes");nav("Inventory themes");click("Christmas");step++;}
   case 2->{Screenshot.grab(client,false);click("Cancel");check(InventoryThemes.current().equals(inventory),"Inventory Cancel changed saved theme");nav("Settings");click("Backgrounds");click("Colors");click("Navy");check(MenuBackgrounds.visible().scene==MenuScene.SOLID&&MenuBackgrounds.visible().solidColor==0x121D38,"New menu is not using the active solid-color draft");check(MenuBackgrounds.current().scene==original.scene,"Background draft saved before Done");step++;}
   case 3->{Screenshot.grab(client,false);click("Cancel");check(MenuBackgrounds.visible().scene==MenuBackgrounds.current().scene,"Cancel left background preview active");click("Backgrounds");click("Colors");var page=MenuNavigation.active();var field=page.children().stream().filter(e->e instanceof EditBox).map(e->(EditBox)e).findFirst().orElseThrow();field.setValue("11223");clickWidget(page,field);field.moveCursorToEnd(false);check(host.charTyped(new CharacterEvent('3')),"Color field typing failed");click("Use hex color");check(MenuBackgrounds.visible().solidColor==0x112233,"Custom hex not applied to menu");click("Done");check(MenuBackgrounds.current().solidColor==0x112233,"Solid menu color did not save");check(MenuBackgrounds.store().load().solidColor==0x112233,"Menu color did not persist");click("Backgrounds");click("Colors");step++;}
   case 4->{Screenshot.grab(client,false);click("Look");click("Brightness -");check(MenuBackgrounds.visible().brightness==90,"Look controls do not edit actual new menu");click("Cancel");check(MenuBackgrounds.current().brightness==100,"Cancel saved appearance effects");nav("Player background");click("Edit scene");click("Layout");click("Full preview");check(client.gui.screen() instanceof CharacterPreviewScreen,"Explicit preview did not fill screen");client.gui.screen().onClose();check(client.gui.screen()==host&&MenuNavigation.active() instanceof CharacterSceneScreen,"Preview return lost sidebar/editor");click("Cancel");click("Start");check(client.gui.screen() instanceof CharacterPreviewScreen,"Presentation failed");client.gui.screen().onClose();check(client.gui.screen()==host,"Presentation return lost dashboard");nav("Design tools");click("Search");step++;}
   case 5->{Screenshot.grab(client,false);nav("Settings");click("Backgrounds");click("Themes");click("Halloween");check(MenuBackgrounds.visible().scene==MenuScene.HOLIDAY&&MenuBackgrounds.visible().menuTheme==InventoryTheme.HALLOWEEN,"Halloween menu theme did not preview");check(InventoryThemes.current().equals(inventory),"Menu theme changed inventory settings");step++;}
   case 6->{Screenshot.grab(client,false);click("New Year");var draft=((MenuBackgroundScreen)MenuNavigation.active()).previewOptions();if(draft.animations)click("Animation: on");click("Done");check(MenuBackgrounds.current().menuTheme==InventoryTheme.NEW_YEAR&&!MenuBackgrounds.current().animations,"Menu holiday/save/animation preferences did not persist");check(MenuBackgrounds.store().load().menuTheme==InventoryTheme.NEW_YEAR,"Menu holiday disk reload failed");click("Backgrounds");click("Themes");click("Next themes");click("Spring");step++;}
   case 7->{Screenshot.grab(client,false);click("Cancel");check(MenuBackgrounds.current().menuTheme==InventoryTheme.NEW_YEAR,"Theme Cancel changed saved menu");check(InventoryThemes.current().equals(inventory),"Menu holiday settings touched inventory");check(ledger.equals(DesignLook.JSON.toJson(mod.ledger.snapshot())),"Navigation changed recorded data");MenuBackgrounds.save(original);Files.writeString(game().resolve("probe-result.txt"),"PASS "+checks+" sidebar/panel layouts; all 12 sections preserve host; nested return, typing/cancel, solid/hex live menu/save/reload, effects cancel, holiday Halloween/New Year/Spring preview, theme animation/save/cancel, inventory independence, presentation return and unchanged recorded data");mod=null;client.stop();}

  }
 }
}
