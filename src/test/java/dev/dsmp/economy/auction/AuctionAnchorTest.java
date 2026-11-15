package dev.dsmp.economy.auction;

import com.google.gson.Gson;
import java.nio.file.Path;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AuctionAnchorTest {
    @AfterEach void close(){AuctionService.close();}
    @Test void rejectsInvalidCoordinatesAndDimensions(){
        assertThrows(IllegalArgumentException.class,()->new AuctionAnchor(null,0,64,0));assertThrows(IllegalArgumentException.class,()->new AuctionAnchor("bad dimension",0,64,0));
        for(double value:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,32_000_001,-32_000_001}){assertThrows(IllegalArgumentException.class,()->new AuctionAnchor("minecraft:overworld",value,64,0));assertThrows(IllegalArgumentException.class,()->new AuctionAnchor("minecraft:overworld",0,value,0));assertThrows(IllegalArgumentException.class,()->new AuctionAnchor("minecraft:overworld",0,64,value));}
    }
    @Test void distanceAndDimensionChecksDontMoveTheAnchor(){
        var anchor=new AuctionAnchor("minecraft:overworld",12.5,64,-8.25);assertTrue(anchor.nearby("minecraft:overworld",12.5,64,-8.25));assertTrue(anchor.nearby("minecraft:overworld",76.5,64,-8.25));assertFalse(anchor.nearby("minecraft:overworld",76.51,64,-8.25));assertFalse(anchor.nearby("minecraft:the_nether",12.5,64,-8.25));assertFalse(anchor.nearby("minecraft:overworld",Double.NaN,64,-8.25));assertEquals(12.5,anchor.x());
    }
    @Test void bidsEndCancellationAndHidePreserveTheStartPosition(){
        var anchor=new AuctionAnchor("minecraft:overworld",12.5,64,-8.25);var r=AuctionRound.start("minecraft:elytra",2,60,1000).at(anchor);
        r=r.receive("Alex",100,1001);assertEquals(anchor,r.anchor());assertEquals(anchor,r.advance(61000).anchor());assertEquals(anchor,r.withState(AuctionRound.State.CANCELLED).hide().anchor());assertEquals(anchor,r.withState(AuctionRound.State.INTERRUPTED).anchor());
    }
    @Test void locationPersistsWithTheRoundAndRemainsIsolatedByAccount(@TempDir Path dir)throws Exception{
        var anchor=new AuctionAnchor("minecraft:overworld",12.5,64,-8.25);AuctionService.open("server|a",dir);AuctionService.start("minecraft:elytra",2,60,1000,anchor);AuctionService.receive("Alex",100,1001);AuctionService.close();AuctionService.open("server|a",dir);assertEquals(anchor,AuctionService.current().anchor());assertEquals(AuctionRound.State.INTERRUPTED,AuctionService.current().state());AuctionService.open("server|b",dir);assertNull(AuctionService.current());
    }
    @Test void oldSavedLotsKeepTheirBidsAndDoNotAcquireAGuessedLocation(){
        String json="{\"version\":1,\"item\":\"minecraft:elytra\",\"quantity\":2,\"startedAt\":1000,\"endsAt\":61000,\"state\":\"ENDED\",\"visible\":true,\"bids\":[{\"player\":\"Alex\",\"cents\":100,\"order\":1}]}";
        var r=new Gson().fromJson(json,AuctionRound.class);assertNull(r.anchor());assertEquals("Alex",r.leader().orElseThrow().player());assertEquals(AuctionRound.State.ENDED,r.state());
    }
}
