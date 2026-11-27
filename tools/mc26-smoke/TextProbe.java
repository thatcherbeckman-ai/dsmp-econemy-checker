package dev.dsmp.economy.probe;
import dev.dsmp.economy.*;
import dev.dsmp.economy.ui.*;
import dev.dsmp.economy.design.*;
import dev.dsmp.economy.menubackground.*;
import dev.dsmp.economy.inventory.*;
import dev.dsmp.economy.text.*;
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
public final class TextProbe implements ClientModInitializer {
 private EconomyTrackerClient mod;private DashboardScreen host;private int ticks,step,checks;private long deadline;private MenuBackgroundOptions original;private InventoryOptions inventory;private String ledger;private int texts;private TextBox textBox;private String beforeStyle,savedStyle;
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
  ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{if(screen instanceof DashboardScreen)ScreenEvents.afterExtract(screen).register((s,g,mx,my,delta)->g.text(client.font,"NATIVE 26.3 TEXT TEST - SAMPLE DATA",8,client.getWindow().getGuiScaledHeight()-10,0xFFFFFFFF));});
  ClientLifecycleEvents.CLIENT_STARTED.register(client->{try{mod=new EconomyTrackerClient();DesignManager.initialize(mod);original=MenuBackgrounds.current();inventory=InventoryThemes.current();ledger=DesignLook.JSON.toJson(mod.ledger.snapshot());host=new DashboardScreen(mod);client.gui.setScreen(host);deadline=System.currentTimeMillis()+150000;Files.deleteIfExists(game().resolve("probe-result.txt"));}catch(Exception e){throw new RuntimeException(e);}});
  ClientTickEvents.END_CLIENT_TICK.register(client->{if(mod==null||++ticks%15!=0||client.gui.overlay()!=null)return;try{if(System.currentTimeMillis()>deadline)throw new AssertionError("Panel smoke timeout at "+step);run(client);}catch(Throwable ex){ex.printStackTrace();try{Files.writeString(game().resolve("probe-result.txt"),"FAIL "+ex);}catch(Exception ignored){}mod=null;client.stop();}});
 }
 private void run(Minecraft client)throws Exception{
  switch(step){
   case 0->{if(client.gui.screen()!=host){client.gui.setScreen(host);return;}nav("Text boxes");click("New");textBox=mod.config.textBoxes.getLast();textBox.text="HELLO WORLD\nColorful text\nThird line";var access=DesignAccess.current();for(int scale:new int[]{100,115,130}){var a=new AccessOptions();a.textScale=scale;a.reducedMotion=true;DesignAccess.install(a);for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720}}){host.init(size[0],size[1]);click("Style");for(String tab:new String[]{"Fonts","Colors","Gradient","Effects","Spacing","Presets"}){click(tab);inspect();}click("Cancel");}}DesignAccess.install(access);host.init(client.getWindow().getGuiScaledWidth(),client.getWindow().getGuiScaledHeight());click("Text");step++;}
   case 1->{var page=MenuNavigation.active();var field=page.children().stream().filter(e->e instanceof MultiLineEditBox).map(e->(MultiLineEditBox)e).findFirst().orElseThrow();field.setValue("Hello");clickWidget(page,field);check(host.keyPressed(new KeyEvent(InputConstants.KEY_RETURN,13,0)),"Multiline Enter was not handled");check(host.charTyped(new CharacterEvent('W')),"Multiline typing failed");check(field.getValue().contains("\n"),"Enter did not insert a real line break");field.setValue("HELLO WORLD\nColorful text\nThird line");Screenshot.grab(client,false);click("Done");beforeStyle=DesignLook.JSON.toJson(textBox);click("Style");click("Presets");click("Ocean");check(textBox.startColor==TextTypography.PAIRS[2][0],"Preset not applied");click("Undo");check(beforeStyle.equals(DesignLook.JSON.toJson(textBox)),"Style Undo did not restore complete snapshot");click("Redo");check(textBox.startColor==TextTypography.PAIRS[2][0],"Style Redo failed");step++;}
   case 2->{Screenshot.grab(client,false);click("Colors");click("Red");check(textBox.startColor==0xFF5454,"Named swatch failed");click("Next colors");click("Gold");check(textBox.startColor==0xEDC86A,"Second palette page failed");var page=MenuNavigation.active();var field=page.children().stream().filter(e->e instanceof EditBox).map(e->(EditBox)e).findFirst().orElseThrow();field.setValue("F0804");clickWidget(page,field);field.moveCursorToEnd(false);check(host.charTyped(new CharacterEvent('0')),"Hex typing failed");click("Apply hex");check(textBox.startColor==0xF08040,"Custom hex failed");step++;}
   case 3->{Screenshot.grab(client,false);click("Gradient");click("Down");check(textBox.gradient==TextBox.DOWN,"Vertical gradient not selected");step++;}
   case 4->{Screenshot.grab(client,false);click("Effects");if(!TextTypography.anyOutline(textBox))click("Outline: off");if(!textBox.shadow)click("Shadow: off");click("Panel background");click("Spacing");var page=MenuNavigation.active();var plus=page.children().stream().filter(e->e instanceof Button b&&b.getMessage().getString().equals("+")).map(e->(Button)e).findFirst().orElseThrow();clickWidget(page,plus);check(textBox.letterSpacing==.5f,"Letter spacing did not update");click("Fonts");if(!textBox.bold)click("Bold: off");click("Italic: off");step++;}
   case 5->{Screenshot.grab(client,false);click("Done");savedStyle=DesignLook.JSON.toJson(textBox);click("Done");check(savedStyle.equals(DesignLook.JSON.toJson(mod.store.loadConfig().textBoxes.getLast())),"New text styles did not persist");nav("Text boxes");click("Style");click("Presets");click("Candy");click("Cancel");check(savedStyle.equals(DesignLook.JSON.toJson(mod.config.textBoxes.getLast())),"Style Cancel lost saved typography");click("Text");var field=MenuNavigation.active().children().stream().filter(e->e instanceof MultiLineEditBox).map(e->(MultiLineEditBox)e).findFirst().orElseThrow();field.setValue("Changed draft");click("Cancel");check(savedStyle.equals(DesignLook.JSON.toJson(mod.config.textBoxes.getLast())),"Multiline Cancel did not restore styles and content");click("Style");click("Colors");step++;}
   case 6->{Screenshot.grab(client,false);check(ledger.equals(DesignLook.JSON.toJson(mod.ledger.snapshot())),"Text editing changed recorded data");Files.writeString(game().resolve("probe-result.txt"),"PASS "+checks+" text-style layouts; native multiline Enter/typing, 12 presets, Undo/Redo, both color pages/custom hex, transformed vertical gradient, outline/shadow/spacing/bold/italic, persistence, style/content Cancel and unchanged recorded data");mod=null;client.stop();}
  }
 }
}
