package dev.dsmp.economy.privacy;

import dev.dsmp.economy.core.*;
import dev.dsmp.economy.auction.AuctionService;
import dev.dsmp.economy.design.DesignLook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class TrackingControlsTest {
    @Test void optOutsAreIndependentAndDoNotMutateAppearanceVisibility(){
        var options=new TrackingOptions();boolean[] views={true,true,true,true};assertArrayEquals(views,options.visibleViews(views));options.profitHidden=true;assertArrayEquals(new boolean[]{true,true,false,true},options.visibleViews(views));assertTrue(options.recordsBets(true));options.playerResultsHidden=true;assertArrayEquals(new boolean[]{false,false,false,true},options.visibleViews(views));assertTrue(options.blocksModule(4));assertFalse(options.blocksModule(5));assertArrayEquals(new boolean[]{true,true,true,true},views);options.turnOffAll();assertFalse(options.recordsBets(true));assertArrayEquals(new boolean[]{false,false,false,true},options.visibleViews(views));
    }
    @Test void pausedReceiptsStillUpdateBalanceWithoutCreatingResults(){
        AuctionService.close();var options=new TrackingOptions();options.betRecordingOff=true;var ledger=new Ledger();ledger.recordBet(new Ledger.Bet("old","EarlierPlayer",100,1));var oldBets=DesignLook.JSON.toJson(ledger.recentBets());var oldPlayers=DesignLook.JSON.toJson(ledger.playerNets());long profit=ledger.displayedProfit();var wallet=new WalletTracker();wallet.manualBalance(Money.parse("10"),ledger,1000);var detector=new PaymentDetector();
        var incoming=detector.receiveDetailed("NewPlayer paid you $5",ledger,2000,60_000,"Me",options.recordsBets(true));assertFalse(incoming.betChanged());assertEquals(Money.parse("5"),incoming.walletDelta());wallet.payment(incoming.walletDelta(),ledger,2000);
        var outgoing=detector.receiveDetailed("You paid NewPlayer $1 (+$1 tax)",ledger,3000,60_000,"Me",options.recordsBets(true));assertFalse(outgoing.betChanged());wallet.payment(outgoing.walletDelta(),ledger,3000);
        assertEquals(Money.parse("13"),ledger.balance().orElseThrow());assertEquals(oldBets,DesignLook.JSON.toJson(ledger.recentBets()));assertEquals(oldPlayers,DesignLook.JSON.toJson(ledger.playerNets()));assertEquals(profit,ledger.displayedProfit());
    }
    @Test void choicesPersistInTheirOwnFileAndLeaveRecordedDataUntouched(@TempDir Path dir)throws Exception{
        var file=dir.resolve("tracking-controls.json");var store=new TrackingStore(file);assertFalse(store.load().betRecordingOff);Files.writeString(dir.resolve("ledger.json"),"existing record");var options=new TrackingOptions();options.turnOffAll();store.save(options);var loaded=store.load();assertTrue(loaded.betRecordingOff&&loaded.profitHidden&&loaded.playerResultsHidden);assertEquals("existing record",Files.readString(dir.resolve("ledger.json")));var copy=loaded.copy();copy.profitHidden=false;assertTrue(loaded.profitHidden);
    }
    @Test void damagedOversizedAndLinkedFilesArePreserved(@TempDir Path dir)throws Exception{
        var file=dir.resolve("controls.json");var store=new TrackingStore(file);for(String value:new String[]{"broken","null","x".repeat(4097)}){Files.writeString(file,value);assertThrows(IOException.class,store::load);assertEquals(value,Files.readString(file));}Files.delete(file);var target=dir.resolve("original");Files.writeString(target,"retained");Files.createSymbolicLink(file,target);assertThrows(IOException.class,store::load);assertThrows(IOException.class,()->store.save(new TrackingOptions()));assertEquals("retained",Files.readString(target));
    }
    @Test void appearanceProfilesCannotStoreOrRestoreTrackingOptOuts(){assertFalse(DesignLook.FIELDS.contains("betRecordingOff"));assertFalse(DesignLook.FIELDS.contains("profitHidden"));assertFalse(DesignLook.FIELDS.contains("playerResultsHidden"));}
}
