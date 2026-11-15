import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.commands.AuctionAction;
import dev.dsmp.economy.ui.*;
import dev.dsmp.economy.core.*;
import net.minecraft.*;
import java.nio.file.Files;

public final class AuctionPatchSmoke {
    private static void click(class_437 screen,String label){screen.widgets.stream().filter(b->b.label.trim().equals(label)).findFirst().orElseThrow(()->new AssertionError(label)).click();}
    public static void main(String[] args)throws Exception{
        var mod=new EconomyTrackerClient(Files.createTempDirectory("auction-ui-"));var game=class_310.method_1551();game.field_1724=new class_746();game.field_1687=new class_638("minecraft:overworld");game.session.username="MyRealAccount";
        if(((AuctionAction)(Object)mod).auctionAction(()->{throw new AssertionError("Ran without scope");})!=0)throw new AssertionError("Missing server guard");
        mod.openLedger("server|account");mod.openMenu();var dashboard=(DashboardScreen)game.screen;var g=new class_332();dashboard.method_25394(g,0,0,0);
        if(g.text.stream().noneMatch(s->s.equals("Welcome, MyRealAccount"))||class_490.lastEntity!=game.field_1724)throw new AssertionError("Welcome banner did not use current user and local entity");
        game.field_1724=null;dashboard.method_25394(new class_332(),0,0,0);game.field_1724=new class_746();
        click(dashboard,"Auctions");var screen=(AuctionScreen)game.screen;if(mod.showHud(screen))throw new AssertionError("Auction editor HUD gate failed");
        screen.fields.get(0).method_1852("diamond");click(screen,"Search");if(screen.widgets.stream().noneMatch(b->b.label.trim().equals("Diamond"))||screen.widgets.stream().anyMatch(b->b.label.trim().equals("Elytra")))throw new AssertionError("Item search failed");click(screen,"Diamond");screen.fields.get(1).method_1852("2");screen.fields.get(2).method_1852("73");click(screen,"Start");
        var r=AuctionService.current();if(r==null||!r.item().equals("minecraft:diamond")||r.quantity()!=2||r.endsAt()-r.startedAt()!=73000)throw new AssertionError("Auction setup failed");
        var detector=new PaymentDetector();var before=mod.ledger.snapshot();var receipt=detector.receiveDetailed("Alex paid you $ 10",mod.ledger,r.startedAt()+1,60000,"MyRealAccount",true);if(mod.customFallback(receipt))throw new AssertionError("Auction receipt fell through to custom bet parser");if(!before.equals(mod.ledger.snapshot()))throw new AssertionError("Auction bid changed bet ledger");
        var lot=new class_332();AuctionHud.drawLot(lot,AuctionService.current(),300,50,1,r.startedAt()+1);if(lot.items.size()!=2||lot.text.stream().noneMatch(s->s.contains("Alex")))throw new AssertionError("Live lot/highest bid not rendered");
        click(screen,"End early");lot=new class_332();AuctionHud.drawLot(lot,AuctionService.current(),300,50,1,r.endsAt());if(lot.text.stream().noneMatch(s->s.startsWith("Winner: Alex")))throw new AssertionError("Winner above item missing");
        click(screen,"Hide result");if(AuctionService.current().visible())throw new AssertionError("Hide result failed");click(screen,"Done");if(game.screen!=dashboard)throw new AssertionError("Done navigation failed");
        ((AuctionAction)(Object)mod).auctionAction(()->AuctionService.start("minecraft:elytra",1,1,System.currentTimeMillis()-2000));mod.auctionTick();if(AuctionService.current().state()!=AuctionRound.State.ENDED)throw new AssertionError("Hidden HUD tick failed");
        ((AuctionAction)(Object)mod).auctionAction(()->AuctionService.start("minecraft:elytra",1,60,System.currentTimeMillis()));mod.disconnect();if(AuctionService.current()!=null)throw new AssertionError("Auction leaked across disconnect");mod.openLedger("server|account");if(AuctionService.current().state()!=AuctionRound.State.INTERRUPTED)throw new AssertionError("Disconnected auction declared winner");
        for(int[] size:new int[][]{{320,240},{640,480},{1280,720}}){screen=new AuctionScreen(mod,dashboard);screen.field_22789=size[0];screen.field_22790=size[1];game.method_1507(screen);screen.method_25394(new class_332(),0,0,0);for(var b:screen.widgets)if(b.x<0||b.y<0||b.x+b.width>size[0]||b.y+b.height>size[1])throw new AssertionError("Auction controls outside viewport");}
        mod.disconnect();System.out.println("Auction UI smoke passed: guarded start, live registry search, quantity/timer, receipt isolation, highest bid/winner, item rendering calls, timer tick, interrupted disconnect and real local player welcome");
    }
}
