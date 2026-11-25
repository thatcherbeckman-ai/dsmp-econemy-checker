package dev.dsmp.economy.probe;

import dev.dsmp.economy.*;
import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.core.*;
import dev.dsmp.economy.text.*;
import dev.dsmp.economy.ui.*;
import dev.dsmp.economy.inventory.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.level.GameType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Display;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Test-only mod, never included in release artifacts. Uses an isolated offline game directory. */
public final class NativeProbe implements ClientModInitializer {
 private EconomyTrackerClient mod;private Screen dashboard,textEditor;private int step,ticks,frames;private String imported;private AuctionAnchor anchor;private long deadline;private boolean creating;private int holiday;private InventoryOptions originalInventory;
 private Path game(){return Path.of(System.getProperty("user.dir"));}
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 private void click(Minecraft client,String label){Screen screen=client.gui.screen();var button=screen.children().stream().filter(e->e instanceof Button b&&b.getMessage().getString().equals(label)).map(e->(Button)e).findFirst().orElseThrow(()->new AssertionError("No button "+label+" in "+screen.getClass()));button.onPress(new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT,0));}
 private void press(Screen screen,String label){var button=screen.children().stream().filter(e->e instanceof Button b&&b.getMessage().getString().equals(label)).map(e->(Button)e).findFirst().orElseThrow(()->new AssertionError("Missing "+label));button.onPress(new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT,0));}
 private int layoutChecks;
 private void layout(Screen screen){
  var widgets=screen.children().stream().filter(e->e instanceof AbstractWidget).map(e->(AbstractWidget)e).filter(w->w.visible).toList();
  for(var a:widgets){check(a.getWidth()>=20&&a.getHeight()>=12&&a.getX()>=0&&a.getY()>=0&&a.getRight()<=screen.width&&a.getBottom()<=screen.height,"Offscreen control "+a.getMessage().getString()+" in "+screen.getClass()+" "+screen.width+"x"+screen.height);for(var b:widgets)if(a!=b)check(a.getX()>=b.getRight()||b.getX()>=a.getRight()||a.getY()>=b.getBottom()||b.getY()>=a.getBottom(),"Overlapping controls "+a.getMessage().getString()+" / "+b.getMessage().getString()+" in "+screen.getClass()+" "+screen.width+"x"+screen.height);}
  layoutChecks++;
 }
 private void layouts(){
  for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720}}){
   var d=new DashboardScreen(mod);d.init(size[0],size[1]);layout(d);for(String page:new String[]{"HUD modules","Loans","Tracking","Settings"}){press(d,page);layout(d);}
   var a=new AppearanceScreen(mod,d);a.init(size[0],size[1]);layout(a);press(a,"Accent");layout(a);
   var t=new TextBoxesScreen(mod,d);t.init(size[0],size[1]);layout(t);
   var l=new LineStylesScreen(mod,t,new TextBox());l.init(size[0],size[1]);layout(l);press(l,"Row");layout(l);press(l,"Image");layout(l);
   var h=new TrackerScreen(mod);h.init(size[0],size[1]);layout(h);press(h,"Trackers");layout(h);press(h,"Widgets");layout(h);
   var auction=new AuctionScreen(mod,d);auction.init(size[0],size[1]);layout(auction);
   var inventory=new InventoryThemesScreen(mod,d);inventory.init(size[0],size[1]);for(int p=0;p<3;p++){layout(inventory);press(inventory,"Next");}
  }
  check(layoutChecks==72,"Incomplete layout coverage");mark("72_NATIVE_LAYOUTS_WITHOUT_OVERLAPS");
 }
 private void mark(String message){System.out.println("ECONOMY_NATIVE_PROBE "+message);try{Files.writeString(game().resolve("probe-progress.txt"),message);}catch(Exception ignored){}}
 public void onInitializeClient(){
  ScreenEvents.AFTER_INIT.register((client,screen,width,height)->{if(screen.getClass().getName().startsWith("dev.dsmp.economy.ui."))ScreenEvents.afterExtract(screen).register((s,g,mx,my,delta)->{frames++;g.text(client.font,"NATIVE 26.3 TEST - SAMPLE DATA",8,s.height-10,0xFFFFFFFF);});});
  ClientLifecycleEvents.CLIENT_STARTED.register(client->{mod=new EconomyTrackerClient();mod.ledger.recordBalance(Money.parse("123M"),System.currentTimeMillis());mod.ledger.recordBet(new Ledger.Bet("sample-a","ProbeWinner",Money.parse("1M"),System.currentTimeMillis()));mod.ledger.recordBet(new Ledger.Bet("sample-b","ProbeLoser",-Money.parse("2M"),System.currentTimeMillis()));deadline=System.currentTimeMillis()+150000;client.gui.setScreen(new DashboardScreen(mod));dashboard=client.gui.screen();mark("STARTED");});
  ClientTickEvents.END_CLIENT_TICK.register(client->{if(mod==null)return;if(++ticks%15!=0)return;try{if(System.currentTimeMillis()>deadline)throw new AssertionError("Native smoke timeout at step "+step);run(client);}catch(Throwable ex){ex.printStackTrace();mark("FAIL "+ex);try{Files.writeString(game().resolve("probe-result.txt"),"FAIL "+ex);}catch(Exception ignored){}mod=null;client.stop();}});
 }
 private void run(Minecraft client)throws Exception{
  switch(step){
   case 0->{if(client.gui.overlay()!=null)return;if(client.gui.screen()!=dashboard){client.gui.setScreen(dashboard);return;}if(frames<3)return;layouts();click(client,"HUD modules");mark("DASHBOARD_RENDERED");step++;}
   case 1->{Screenshot.grab(client,false);click(client,"Appearance");step++;}
   case 2->{Screenshot.grab(client,false);click(client,"Size +");click(client,"Done");check(client.gui.screen()==dashboard,"Appearance navigation");click(client,"Text boxes");textEditor=client.gui.screen();click(client,"New");click(client,"Preset");check(mod.config.textBoxes.getFirst().text.startsWith("IGN EconBuildCheck"),"Current username preset");step++;}
   case 3->{Screenshot.grab(client,false);click(client,"Lines");click(client,"Next");click(client,"Next");click(client,"Next");click(client,"Image");var image=new BufferedImage(32,16,BufferedImage.TYPE_INT_ARGB);for(int x=0;x<32;x++)for(int y=0;y<16;y++)image.setRGB(x,y,x<16?0xFFFF4466:0xFF33DDBB);Path path=game().resolve("probe-image.png");ImageIO.write(image,"PNG",path.toFile());imported=TextImages.importPng(path);mod.config.textBoxes.getFirst().editLine(3).image=imported;step++;}
   case 4->{var cache=TextImages.class.getDeclaredField("CACHE");cache.setAccessible(true);check(((Map<?,?>)cache.get(null)).containsKey(imported),"Imported PNG did not upload to native texture manager");click(client,"Text");List<EditBox> fields=client.gui.screen().children().stream().filter(e->e instanceof EditBox).map(e->(EditBox)e).toList();fields.get(2).setValue("00FFCC");fields.get(3).setValue("FF44AA");click(client,"Solid");click(client,"Across");step++;}
   case 5->{Screenshot.grab(client,false);click(client,"Done");check(client.gui.screen()==textEditor,"Line return");var box=mod.config.textBoxes.getFirst();var b=TextHud.bounds(box,textEditor.width,textEditor.height-24,true);var mouse=new MouseButtonEvent(b.x()+1,b.y()+1,new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT,0));check(textEditor.mouseClicked(mouse,false),"SDL mouse cannot select styled text");var target=new MouseButtonEvent(1,TextLayout.EDITOR_TOP+1,new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT,0));textEditor.mouseDragged(target,0,0);textEditor.mouseReleased(target);check(box.x==0&&box.y==0,"SDL text drag bounds");step++;}
   case 6->{click(client,"Done");check(client.gui.screen()==dashboard,"Text save return");check(mod.store.loadConfig().textBoxes.getFirst().style(3).image.equals(imported),"Native save/load");click(client,"Auctions");check(AuctionItems.all().isEmpty(),"Title-screen catalogue must wait for bound world components");mark("MENUS_TEXT_IMAGES_DRAG_SAVED");step++;}
   case 7->{Screenshot.grab(client,false);click(client,"Done");CreateWorldScreen.testWorld(client,()->client.gui.setScreen(new TitleScreen()));creating=true;step++;}
   case 8->{if(client.level!=null&&client.player!=null)client.gui.setScreen(null);if(client.level==null||client.player==null||client.player.tickCount<50){if(ticks%120==0){var screen=client.gui.screen();mark("WAIT_WORLD "+(screen==null?"none":screen.getClass().getName())+" player="+(client.player==null?"none":client.player.tickCount));if(screen!=null)System.out.println(screen.children().stream().filter(e->e instanceof Button).map(e->((Button)e).getMessage().getString()).toList());}if(client.gui.screen() instanceof CreateWorldScreen)click(client,"Create New World");else if(client.gui.screen() instanceof ConfirmScreen){var buttons=client.gui.screen().children().stream().filter(e->e instanceof Button).map(e->(Button)e).toList();if(!buttons.isEmpty())buttons.getFirst().onPress(new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT,0));}return;}client.gui.setScreen(null);if(++frames<20)return;check(AuctionItems.all().size()>1000,"Live world item registry incomplete");AuctionService.open("native-probe|offline",game().resolve("auction-test"));anchor=AuctionHologram.capture();AuctionService.start("minecraft:elytra",2,60,System.currentTimeMillis(),anchor);client.player.setXRot(75);step++;mark("LOCAL_WORLD_AUCTION_STARTED");}
   case 9->{var entities=new ArrayList<net.minecraft.world.entity.Entity>();client.level.entitiesForRendering().forEach(entities::add);var item=entities.stream().filter(e->e.getId()<0&&e instanceof ItemEntity).findFirst().orElseThrow(()->new AssertionError("No world auction item"));check(item.isCurrentlyGlowing(),"Running item not glowing");check(Math.abs(item.getX()-anchor.x())<.001&&Math.abs(item.getZ()-anchor.z())<.001,"Hologram moved");check(entities.stream().anyMatch(e->e.getId()<0&&e instanceof Display.TextDisplay),"No world billboard label");AuctionService.receive("ProbeBidder",500,System.currentTimeMillis());AuctionService.end();step++;}
   case 10->{var entities=new ArrayList<net.minecraft.world.entity.Entity>();client.level.entitiesForRendering().forEach(entities::add);check(entities.stream().filter(e->e.getId()<0&&e instanceof ItemEntity).noneMatch(net.minecraft.world.entity.Entity::isCurrentlyGlowing),"Finished item still glowing");check(AuctionService.current().leader().orElseThrow().player().equals("ProbeBidder"),"Winner");Screenshot.grab(client,false);AuctionService.hide();step++;}
   case 11->{var entities=new ArrayList<net.minecraft.world.entity.Entity>();client.level.entitiesForRendering().forEach(entities::add);check(entities.stream().noneMatch(e->e.getId()<0&&(e instanceof ItemEntity||e instanceof Display.TextDisplay)),"Hidden hologram left entities");client.gui.setScreen(new DashboardScreen(mod));step++;mark("WORLD_ITEM_GLOW_WINNER_HIDE_PASSED");}
   case 12->{Screenshot.grab(client,false);step++;}
   case 13->{
    originalInventory=InventoryThemes.current();
    click(client,"Inventory themes");check(client.gui.screen() instanceof InventoryThemesScreen,"Inventory menu missing");
    click(client,"Christmas");click(client,"Animation: on");click(client,"Faster");click(client,"Cancel");check(InventoryThemes.current().equals(originalInventory),"Theme Cancel changed global options");
    click(client,"Inventory themes");click(client,"Halloween");click(client,"Animation: on");click(client,"Faster");click(client,"Done");
    check(InventoryThemes.current().theme()==InventoryTheme.HALLOWEEN&&!InventoryThemes.current().animations()&&InventoryThemes.current().speed()==125,"Saved inventory draft");
    check(new InventoryThemeStore(game().resolve("config/economy-tracker/inventory-themes.json")).load().equals(InventoryThemes.current()),"Inventory preferences did not persist");
    var server=client.getSingleplayerServer();server.execute(()->server.getPlayerList().getPlayer(client.player.getUUID()).setGameMode(GameType.SURVIVAL));
    client.gui.setScreen(null);step++;
   }
   case 14->{if(client.player.hasInfiniteMaterials())return;InventoryThemes.save(InventoryOptions.defaults().theme(InventoryTheme.CHRISTMAS).intensity(2));client.gui.setScreen(new InventoryScreen(client.player));step++;}
   case 15->{
    check(client.gui.screen() instanceof InventoryScreen,"Vanilla inventory was replaced");
    check(client.player.inventoryMenu.slots.size()==46,"Theme altered inventory slots");Screenshot.grab(client,false);mark("CHRISTMAS_INVENTORY_NATIVE");
    InventoryThemes.save(InventoryOptions.defaults().theme(InventoryTheme.HALLOWEEN).intensity(2));step++;
   }
   case 16->{Screenshot.grab(client,false);mark("HALLOWEEN_INVENTORY_NATIVE");InventoryThemes.save(InventoryOptions.defaults().theme(InventoryTheme.NEW_YEAR).intensity(2));step++;}
   case 17->{Screenshot.grab(client,false);mark("NEW_YEAR_INVENTORY_NATIVE");client.gui.setScreen(new InventoryThemesScreen(mod,new DashboardScreen(mod)));step++;}
   case 18->{Screenshot.grab(client,false);InventoryThemes.save(originalInventory);check(frames>30,"Too few native GUI frames");Files.writeString(game().resolve("probe-result.txt"),"PASS 72 responsive native layouts without overlapping/offscreen controls; holiday picker/cancel/save/load, native Christmas/Halloween/New Year inventory; native 26.3 menus, text/images, SDL drag, save/load, live item registry, local-world item/text entities, active glow, winner and hide; frames="+frames);mark("PASS frames="+frames);mod=null;client.stop();}
  }
 }
}
