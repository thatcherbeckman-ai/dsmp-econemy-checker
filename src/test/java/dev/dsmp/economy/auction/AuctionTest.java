package dev.dsmp.economy.auction;

import dev.dsmp.economy.core.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {
    @AfterEach void close(){AuctionService.close();}
    @Test void cumulativeBidsAreCaseInsensitiveAndTiesFavorFirstToReachTheTotal(){
        var r=AuctionRound.start("minecraft:elytra",2,60,1000).receive("Alex",100,1001).receive("Blake",250,1002).receive("aLeX",150,1003);
        assertEquals(2,r.bids().size());assertEquals("Blake",r.leader().orElseThrow().player());assertEquals(250,r.leader().orElseThrow().cents());r=r.receive("ALEX",1,1004);assertEquals("Alex",r.leader().orElseThrow().player());assertEquals(251,r.leader().orElseThrow().cents());
    }
    @Test void biddingWindowIsInclusiveAtStartAndExclusiveAtDeadline(){
        var r=AuctionRound.start("minecraft:diamond",1,60,1000);assertFalse(r.accepts(999));assertTrue(r.accepts(1000));assertTrue(r.accepts(60999));assertFalse(r.accepts(61000));assertEquals(r,r.receive("Alex",100,999));assertTrue(r.receive("Alex",100,61000).bids().isEmpty());assertEquals(AuctionRound.State.ENDED,r.advance(61000).state());
    }
    @Test void countdownRoundsUpAndHandlesLongTimersAndEndedLots(){
        var r=AuctionRound.start("minecraft:elytra",2,73,1000);assertEquals("01:13",r.countdown(1000));assertEquals("00:01",r.countdown(73999));assertEquals("00:00",r.countdown(74000));assertEquals("00:00",r.countdown(999999));assertEquals("1440:00",AuctionRound.start("minecraft:diamond",64,86400,0).countdown(0));
    }
    @Test void rejectsBadItemsNamesQuantityDurationAndOverflow(){
        assertThrows(IllegalArgumentException.class,()->AuctionRound.start("minecraft:air",1,60,0));assertThrows(IllegalArgumentException.class,()->AuctionRound.start("bad ID",1,60,0));assertThrows(IllegalArgumentException.class,()->AuctionRound.start("minecraft:diamond",0,60,0));assertThrows(IllegalArgumentException.class,()->AuctionRound.start("minecraft:diamond",65,60,0));assertThrows(IllegalArgumentException.class,()->AuctionRound.start("minecraft:diamond",1,0,0));assertThrows(IllegalArgumentException.class,()->AuctionRound.start("minecraft:diamond",1,86401,0));assertThrows(ArithmeticException.class,()->AuctionRound.start("minecraft:diamond",1,60,Long.MAX_VALUE));
        var r=AuctionRound.start("minecraft:diamond",1,60,1000);assertThrows(IllegalArgumentException.class,()->r.receive("invalid name",1,1001));assertThrows(IllegalArgumentException.class,()->r.receive("Alex",0,1001));var huge=r.receive("Alex",Long.MAX_VALUE,1001);assertThrows(ArithmeticException.class,()->huge.receive("Alex",1,1002));assertEquals(Long.MAX_VALUE,huge.leader().orElseThrow().cents());
    }
    @Test void immutableSnapshotAndNoBidsProduceNoWinner(){
        var r=AuctionRound.start("minecraft:elytra",2,60,0);assertTrue(r.advance(60000).leader().isEmpty());assertThrows(UnsupportedOperationException.class,()->r.bids().add(new AuctionRound.Bid("Alex",1,1)));assertThrows(IllegalStateException.class,r::hide);assertFalse(r.withState(AuctionRound.State.CANCELLED).hide().visible());
    }
    @Test void persistenceSeparatesAccountsAndPreservesCorruptFiles(@TempDir Path dir)throws Exception{
        var a=new AuctionStore(dir,"donutsmp|a");var b=new AuctionStore(dir,"donutsmp|b");var r=AuctionRound.start("minecraft:elytra",2,60,1000).receive("Alex",123400,1001);a.save(r);assertEquals(r,a.load());assertNull(b.load());Path file=Files.list(dir).findFirst().orElseThrow();Files.writeString(file,"{broken");assertThrows(java.io.IOException.class,a::load);assertEquals("{broken",Files.readString(file));
    }
    @Test void confirmedAuctionReceiptsUpdateWalletWithoutStakesProfitOrRakeback(@TempDir Path dir){
        AuctionService.open("donutsmp|account",dir);AuctionService.start("minecraft:diamond",1,60,1000);var ledger=new Ledger();var before=ledger.snapshot();var detector=new PaymentDetector();var payment=detector.receiveDetailed("Alex paid you $ 5",ledger,1001,60000,"Host",true);
        assertEquals(500L,payment.walletDelta());assertTrue(payment.auctionPayment());assertTrue(payment.suppressesCustom());assertFalse(payment.betChanged());assertEquals(before,ledger.snapshot());assertEquals(500,AuctionService.current().leader().orElseThrow().cents());
        detector.receiveDetailed("ALEX paid you $ 2",ledger,1002,60000,"Host",false);assertEquals(700,AuctionService.current().leader().orElseThrow().cents());
        detector.receiveDetailed("Host paid you $ 99",ledger,1003,60000,"Host",true);assertEquals(700,AuctionService.current().leader().orElseThrow().cents());
        var late=detector.receiveDetailed("Blake paid you $ 1",ledger,61000,60000,"Host",true);assertFalse(late.auctionPayment());assertTrue(late.betChanged());assertEquals(AuctionRound.State.ENDED,AuctionService.current().state());assertEquals("Alex",AuctionService.current().leader().orElseThrow().player());
    }
    @Test void ordinaryOutgoingPaymentsStillIncludeTaxAndRecordPlayerWins(@TempDir Path dir){
        AuctionService.open("server|account",dir);AuctionService.start("minecraft:diamond",1,60,1000);var ledger=new Ledger();var payment=new PaymentDetector().receiveDetailed("You paid Alex $ 5 (+$ 1 tax)",ledger,1001,60000,"Host",true);assertEquals(-600L,payment.walletDelta());assertTrue(payment.betChanged());assertFalse(payment.auctionPayment());assertTrue(AuctionService.current().bids().isEmpty());
    }
    @Test void endsOnTickPersistsResultsAndDisconnectNeverDeclaresWinner(@TempDir Path dir){
        AuctionService.open("server|a",dir);AuctionService.start("minecraft:elytra",2,1,1000);AuctionService.receive("Alex",100,1001);assertThrows(IllegalStateException.class,()->AuctionService.start("minecraft:diamond",1,60,1002));AuctionService.tick(2000);assertEquals(AuctionRound.State.ENDED,AuctionService.current().state());AuctionService.hide();assertFalse(AuctionService.current().visible());AuctionService.close();AuctionService.open("server|a",dir);assertEquals("Alex",AuctionService.current().leader().orElseThrow().player());assertFalse(AuctionService.current().visible());
        AuctionService.start("minecraft:diamond",1,60,5000);AuctionService.receive("Blake",200,5001);AuctionService.close();AuctionService.open("server|a",dir);assertEquals(AuctionRound.State.INTERRUPTED,AuctionService.current().state());assertEquals("Blake",AuctionService.current().leader().orElseThrow().player());assertFalse(AuctionService.receive("Alex",300,5002));AuctionService.open("server|b",dir);assertNull(AuctionService.current());
    }
    @Test void restartInterruptsSavedRunningRoundAndFailedWritesKeepOriginal(@TempDir Path dir)throws Exception{
        var store=new AuctionStore(dir,"server|a");store.save(AuctionRound.start("minecraft:elytra",2,60,1000));AuctionService.open("server|a",dir);assertEquals(AuctionRound.State.INTERRUPTED,AuctionService.current().state());assertEquals(AuctionRound.State.INTERRUPTED,store.load().state());AuctionService.close();
        Path blocked=dir.resolve("blocked");Files.writeString(blocked,"keep");AuctionService.open("server|b",blocked);assertThrows(IllegalStateException.class,()->AuctionService.start("minecraft:diamond",1,60,0));assertEquals("keep",Files.readString(blocked));assertNull(AuctionService.current());
    }
    @Test void storageFailureStopsBiddingAndDoesNotTurnAuctionReceiptsIntoBets(@TempDir Path dir)throws Exception{
        Path target=dir.resolve("auctions");AuctionService.open("server|a",target);AuctionService.start("minecraft:elytra",2,60,1000);AuctionService.receive("Alex",100,1001);
        Path saved;try(var paths=Files.list(target)){saved=paths.findFirst().orElseThrow();}Files.delete(saved);Files.delete(target);Files.writeString(target,"blocked");
        assertTrue(AuctionService.receive("Blake",200,1002));assertNotNull(AuctionService.error());assertEquals(AuctionRound.State.INTERRUPTED,AuctionService.current().state());assertEquals("Alex",AuctionService.current().leader().orElseThrow().player());
        var ledger=new Ledger();var before=ledger.snapshot();var payment=new PaymentDetector().receiveDetailed("Casey paid you $ 3",ledger,1003,60000,"Host",true);assertTrue(payment.auctionPayment());assertTrue(payment.suppressesCustom());assertEquals(300L,payment.walletDelta());assertEquals(before,ledger.snapshot());assertFalse(AuctionService.receive("Alex",100,61000));assertEquals("blocked",Files.readString(target));
    }
    @Test void cancellationAndEarlyEndKeepTheBidLedgerAndStopIntake(@TempDir Path dir){
        AuctionService.open("server|a",dir);AuctionService.start("minecraft:elytra",2,60,0);AuctionService.receive("Alex",100,1);AuctionService.cancel();assertEquals(AuctionRound.State.CANCELLED,AuctionService.current().state());assertFalse(AuctionService.receive("Blake",999,2));assertEquals("Alex",AuctionService.current().leader().orElseThrow().player());AuctionService.start("minecraft:elytra",2,60,3);AuctionService.receive("Blake",200,4);AuctionService.end();assertEquals(AuctionRound.State.ENDED,AuctionService.current().state());assertFalse(AuctionService.receive("Alex",999,5));
    }
}
