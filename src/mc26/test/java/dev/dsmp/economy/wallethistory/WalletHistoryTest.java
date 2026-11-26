package dev.dsmp.economy.wallethistory;

import dev.dsmp.economy.core.Ledger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WalletHistoryTest {
    private static final long DAY=86_400_000L,NOW=500*DAY;
    private static Ledger.Point p(long time,long cents){return new Ledger.Point(time,cents);}
    @Test void allFourRollingRangesUseTheirOwnOpeningReading(){
        var points=List.of(p(NOW-400*DAY,100),p(NOW-365*DAY,200),p(NOW-28*DAY,400),p(NOW-7*DAY,600),p(NOW-DAY,800),p(NOW,1000));
        long[] expected={200,400,600,800};for(int i=0;i<BalanceWindow.DAYS.size();i++){var window=BalanceWindow.of(points,BalanceWindow.DAYS.get(i),NOW);assertTrue(window.complete());assertEquals(expected[i],window.change().orElseThrow());assertEquals(1,window.position(NOW));assertEquals(0,window.position(window.from()));}
    }
    @Test void lastKnownOpeningReadingIsUsedAndNoZeroBalanceInvented(){
        var window=BalanceWindow.of(List.of(p(NOW-2*DAY,500),p(NOW-DAY/2,700),p(NOW,900)),1,NOW);assertTrue(window.complete());assertEquals(400,window.change().orElseThrow());assertEquals(500,window.points().getFirst().cents());assertEquals(0,window.position(NOW-2*DAY));
    }
    @Test void missingCoverageIsExplicitlyPartialAndNotAnEntirePeriodClaim(){
        var points=List.of(p(NOW-1000,500),p(NOW,700));for(int days:BalanceWindow.DAYS){var window=BalanceWindow.of(points,days,NOW);assertFalse(window.complete());assertEquals(200,window.change().orElseThrow());assertEquals(NOW-1000,window.firstReading());assertEquals(2,window.points().size());}
    }
    @Test void emptySingleStaleFutureAndOverflowHaveNoInventedChange(){
        assertTrue(BalanceWindow.of(List.of(),1,NOW).points().isEmpty());assertTrue(BalanceWindow.of(List.of(p(NOW,123)),1,NOW).change().isEmpty());assertTrue(BalanceWindow.of(List.of(p(NOW-2*DAY,123)),1,NOW).points().isEmpty());assertTrue(BalanceWindow.of(List.of(p(NOW+1,123)),1,NOW).points().isEmpty());assertTrue(BalanceWindow.of(List.of(p(NOW-1,Long.MIN_VALUE),p(NOW,Long.MAX_VALUE)),1,NOW).change().isEmpty());assertThrows(IllegalArgumentException.class,()->BalanceWindow.of(List.of(),2,NOW));
    }
    @Test void unsortedDuplicatesAreChronologicalAndKeepLatestValue(){
        var window=BalanceWindow.of(List.of(p(NOW,500),p(NOW-1000,100),p(NOW,700)),7,NOW);assertEquals(List.of(p(NOW-1000,100),p(NOW,700)),window.points());assertEquals(600,window.change().orElseThrow());
    }
    @Test void yearRetentionIsBoundedAndPreservesActualTimesAndLatest(){
        var input=new ArrayList<Ledger.Point>();input.add(p(NOW-400*DAY,1));for(long t=NOW-367*DAY;t<NOW-2*DAY;t+=3_600_000)input.add(p(t,t/1000));for(long t=NOW-2*DAY;t<=NOW;t+=60_000)input.add(p(t,t/1000));var model=new WalletHistory();model.merge(input,NOW);assertTrue(model.points().size()<=WalletHistory.MAX_POINTS);assertEquals(input.getFirst(),model.points().getFirst());assertEquals(input.getLast(),model.points().getLast());assertTrue(model.points().stream().allMatch(input::contains));assertTrue(BalanceWindow.of(model.points(),365,NOW).complete());assertEquals(model.points(),WalletHistory.restore(model.points(),NOW).points());
    }
    @Test void minuteCompactionRetainsFirstCoverageAndNewestWithoutChangingValues(){
        var history=new WalletHistory();long minute=NOW/60_000*60_000;history.merge(List.of(p(minute+1,100),p(minute+2,200)),NOW+100);assertEquals(List.of(p(minute+1,100),p(minute+2,200)),history.points());history.merge(List.of(p(NOW+600_000,999)),NOW);assertEquals(List.of(p(minute+1,100),p(minute+2,200)),history.points());
    }
    @Test void walletFilesAreScopedRoundTripAndDoNotContainResultData(@TempDir Path dir)throws Exception{
        var a=new WalletHistoryStore(dir,"server|account-a");var b=new WalletHistoryStore(dir,"server|account-b");var c=new WalletHistoryStore(dir,"other-server|account-a");assertNotEquals(a.path(),b.path());assertNotEquals(a.path(),c.path());assertEquals(64,a.path().getFileName().toString().replace(".json","").length());var history=new WalletHistory();history.merge(List.of(p(NOW-2*DAY,100),p(NOW,300)),NOW);a.save(history);assertEquals(history.points(),a.load(NOW).points());assertTrue(b.load(NOW).points().isEmpty());String json=Files.readString(a.path());assertFalse(json.contains("player"));assertFalse(json.contains("profit"));assertFalse(json.contains("bet"));
    }
    @Test void corruptOversizedLinkedAndUnsupportedFilesArePreserved(@TempDir Path dir)throws Exception{
        var store=new WalletHistoryStore(dir,"account");for(String value:List.of("broken","null","{\"version\":2,\"points\":[]}","{\"version\":1,\"points\":[null]}","x".repeat(2_097_153))){Files.writeString(store.path(),value);assertThrows(IOException.class,()->store.load(NOW));assertEquals(value,Files.readString(store.path()));}Files.delete(store.path());var target=dir.resolve("original");Files.writeString(target,"retained");Files.createSymbolicLink(store.path(),target);assertThrows(IOException.class,()->store.load(NOW));assertThrows(IOException.class,()->store.save(new WalletHistory()));assertEquals("retained",Files.readString(target));
    }
    @Test void passiveServiceImportsCapturesFlushesAndIsolatesAccounts(@TempDir Path dir)throws Exception{
        long now=System.currentTimeMillis();var ledger=new Ledger();ledger.recordBalance(100,now-1000);try{WalletHistoryService.open("server|a",dir,ledger.balances());ledger.recordBalance(200,now);WalletHistoryService.capture(ledger.balances(),now);assertEquals(100,BalanceWindow.of(WalletHistoryService.points(ledger),1,now).change().orElseThrow());WalletHistoryService.close();assertEquals(200,new WalletHistoryStore(dir,"server|a").load(now).points().getLast().cents());WalletHistoryService.open("server|b",dir,List.of());assertTrue(WalletHistoryService.points(new Ledger()).isEmpty());}finally{WalletHistoryService.close();}
    }
    @Test void serviceReadFailureNeverOverwritesTheOriginal(@TempDir Path dir)throws Exception{
        var store=new WalletHistoryStore(dir,"broken");Files.writeString(store.path(),"retained bad JSON");var ledger=new Ledger();ledger.recordBalance(500,System.currentTimeMillis());try{WalletHistoryService.open("broken",dir,ledger.balances());assertNotNull(WalletHistoryService.error());assertEquals(ledger.balances(),WalletHistoryService.points(ledger));WalletHistoryService.capture(ledger.balances(),System.currentTimeMillis());WalletHistoryService.flush();assertEquals("retained bad JSON",Files.readString(store.path()));}finally{WalletHistoryService.close();}
    }
}
