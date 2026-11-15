import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.commands.AuctionAction;
import dev.dsmp.economy.ui.*;
import net.minecraft.*;
import java.nio.file.Files;

/** Exercises actual hologram code and patched ticks with Minecraft-native API stand-ins. */
public final class AuctionHologramSmoke {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void click(class_437 screen,String label){screen.widgets.stream().filter(b->b.label.trim().equals(label)).findFirst().orElseThrow().click();}
    private static class_1542 item(class_638 world){return world.entities.values().stream().filter(e->e instanceof class_1542).map(e->(class_1542)e).findFirst().orElseThrow();}
    private static class_8113.class_8123 label(class_638 world){return world.entities.values().stream().filter(e->e instanceof class_8113.class_8123).map(e->(class_8113.class_8123)e).findFirst().orElseThrow();}
    public static void main(String[] args)throws Exception{
        var client=class_310.method_1551();client.field_1687=new class_638("minecraft:overworld");client.field_1724=new class_746();client.field_1724.method_5814(12.5,64,-8.25);var world=client.field_1687;
        var mod=new EconomyTrackerClient(Files.createTempDirectory("hologram-ui-"));mod.openLedger("server|a");mod.openMenu();click(client.screen,"Auctions");var editor=(AuctionScreen)client.screen;click(editor,"Start");
        var round=AuctionService.current();check(round.anchor().equals(new AuctionAnchor("minecraft:overworld",12.5,64,-8.25)),"Start did not capture feet/dimension");
        check(world.entities.size()==2,"Missing world item and billboard label");var item=item(world);var label=label(world);
        check(item.method_23317()==12.5&&Math.abs(item.method_23318()-64.08)<.0001&&item.method_23321()==-8.25,"Item not anchored near the ground");
        check(item.glowing&&item.noGravity&&item.noPickup&&item.neverDespawn&&item.stack.count==2,"Dropped-item hologram not configured as a permanent visual");item.field_6012=42;check(item.method_6985()==42,"Never-despawn froze the bob/rotation clock");check(AuctionItems.find("minecraft:elytra").stack().count==1,"Item registry preview stack was mutated");
        check(label.method_23318()>item.method_23318()&&label.billboard==class_8113.class_8114.field_42409&&label.text.contains("No bids yet"),"Missing facing label above item");check(item.method_5628()<0&&label.method_5628()<0&&item.method_5628()!=label.method_5628(),"Local entity IDs must be distinct negative IDs");
        client.field_1724.method_5814(25,64,7);item.method_5814(99,99,99);item.method_18800(1,1,1);mod.auctionTick();check(item(world)==item&&item.method_23317()==12.5&&item.method_23321()==-8.25&&item.vx==0,"Hologram followed player or drifted");
        var gui=new class_332();TextHud.draw(gui,mod.config,640,480,false);check(gui.items.isEmpty(),"Auction still renders as screen overlay");
        AuctionService.receive("Alex",100,round.startedAt()+1);AuctionService.receive("Blake",200,round.startedAt()+2);mod.auctionTick();check(label.text.contains("Blake")&&!label.text.contains("Winner:"),"Leading receipt bid did not update world text");
        AuctionService.end();mod.auctionTick();check(!item.glowing&&label.text.contains("Winner: Blake"),"Final winner missing from above item");AuctionService.hide();mod.auctionTick();check(world.entities.isEmpty(),"Hide result left world entities behind");
        ((AuctionAction)(Object)mod).auctionAction(()->AuctionService.start("minecraft:diamond",1,60,System.currentTimeMillis(),AuctionHologram.capture()));mod.auctionTick();check(item(world).method_23317()==25&&item(world).method_23321()==7,"New auction reused old location");
        AuctionService.cancel();mod.auctionTick();check(!item(world).glowing&&label(world).text.contains("cancelled")&&!label(world).text.contains("Winner:"),"Cancelled auction kept glowing or declared a winner");AuctionService.start("minecraft:diamond",1,60,System.currentTimeMillis(),AuctionHologram.capture());mod.auctionTick();
        client.field_1724.method_5814(1000,64,1000);mod.auctionTick();check(world.entities.isEmpty(),"Distant hologram kept local entities loaded");client.field_1724.method_5814(25,64,7);mod.auctionTick();check(world.entities.size()==2,"Returning to location did not restore hologram");
        var nether=new class_638("minecraft:the_nether");client.field_1687=nether;mod.auctionTick();check(world.entities.isEmpty()&&nether.entities.isEmpty(),"Auction leaked into another dimension");client.field_1687=world;mod.auctionTick();check(world.entities.size()==2,"Returning from another dimension lost location");
        var oldItem=item(world);var unrelated=new class_1297();unrelated.method_5838(oldItem.method_5628());world.method_53875(unrelated);mod.auctionTick();check(world.method_8469(unrelated.method_5628())==unrelated&&world.entities.size()==3,"Cleanup deleted an entity owned by someone else");
        mod.disconnect();check(AuctionService.current()==null&&world.entities.size()==1&&world.method_8469(unrelated.method_5628())==unrelated,"Disconnect did not remove only owned hologram entities");mod.openLedger("server|a");mod.auctionTick();check(AuctionService.current().state()==AuctionRound.State.INTERRUPTED&&!item(world).glowing&&label(world).text.contains("interrupted")&&!label(world).text.contains("Winner:"),"Interrupted location/result was not restored honestly");
        mod.disconnect();mod.openLedger("server|b");mod.auctionTick();check(world.entities.size()==1,"Account switch resurrected another account's hologram");
        AuctionService.start("minecraft:elytra",2,60,System.currentTimeMillis());mod.auctionTick();check(world.entities.size()==1,"Legacy lot acquired a guessed position");AuctionService.cancel();
        client.field_1687=null;try{AuctionHologram.capture();throw new AssertionError("Allowed placement without world");}catch(IllegalStateException expected){}
        mod.auctionTick();check(world.entities.size()==1,"Null world cleanup failed");mod.disconnect();client.field_1724=null;
        System.out.println("World hologram smoke passed: UI start location, native world item/text, fixed position, no HUD overlay, bid/winner updates, hide, distance, dimensions, ID ownership, disconnect/reconnect, account isolation and legacy data");
    }
}
