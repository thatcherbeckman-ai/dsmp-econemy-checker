package dev.dsmp.economy.spotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;

class SpotifyPlacementTest {
    @TempDir Path folder;
    @Test void legacySettingsKeepPresetAnchorsAndNewFieldsRoundTrip()throws Exception{
        Path file=folder.resolve("spotify.json");Files.writeString(file,"{\"clientId\":\"\",\"enabled\":true,\"hideIdle\":true,\"position\":8,\"scale\":100,\"palette\":0,\"source\":\"DESKTOP\"}");
        var old=SpotifyOptions.load(file);assertFalse(old.freePosition());var b=SpotifyPlacement.bounds(old,1000,700);assertEquals(744,b.x());assertEquals(616,b.y());
        var exact=SpotifyPlacement.atPixels(old.size(25),137,89,1000,700);exact.save(file);assertEquals(exact,SpotifyOptions.load(file));assertTrue(exact.freePosition());assertEquals(25,exact.scale());assertEquals(137,SpotifyPlacement.bounds(exact,1000,700).x());
    }
    @Test void allAnchorsAndCustomEdgesStayVisibleAcrossSizesAndScales(){
        for(int[] size:new int[][]{{1,1},{160,100},{320,240},{480,360},{960,700},{1920,1080},{3840,2160}})for(int scale:new int[]{25,26,37,50,70,100,125,150})for(int anchor=0;anchor<9;anchor++){
            var options=SpotifyOptions.defaults().size(scale).anchor(anchor);checkBounds(options,size[0],size[1]);
            for(double coordinate:new double[]{0,.25,.5,.99,1})checkBounds(options.at(coordinate,coordinate),size[0],size[1]);
        }
        assertThrows(IllegalArgumentException.class,()->SpotifyPlacement.bounds(SpotifyOptions.defaults(),0,700));
    }
    private static void checkBounds(SpotifyOptions options,int width,int height){var b=SpotifyPlacement.bounds(options,width,height);assertTrue(b.x()>=0&&b.y()>=0&&b.width()>0&&b.height()>0);assertTrue(b.x()+b.width()<=width&&b.y()+b.height()<=height);assertTrue(b.scale()>0);assertTrue(b.contains(b.x(),b.y()));assertFalse(b.contains(b.x()+b.width(),b.y()));}
    @Test void exactPixelPlacementNudgesAndDragClamp(){
        var options=SpotifyPlacement.atPixels(SpotifyOptions.defaults().size(37),137,89,960,700);var b=SpotifyPlacement.bounds(options,960,700);assertEquals(137,b.x());assertEquals(89,b.y());
        var moved=SpotifyPlacement.move(options,1,-10,960,700);b=SpotifyPlacement.bounds(moved,960,700);assertEquals(138,b.x());assertEquals(79,b.y());assertEquals(37,moved.scale());
        b=SpotifyPlacement.bounds(SpotifyPlacement.atPixels(options,-10000,-10000,960,700),960,700);assertEquals(0,b.x());assertEquals(0,b.y());
        b=SpotifyPlacement.bounds(SpotifyPlacement.atPixels(options,10000,10000,960,700),960,700);assertEquals(960-b.width(),b.x());assertEquals(700-b.height(),b.y());
    }
    @Test void resizingAroundCenterKeepsPositionPredictableAndWithinBounds(){
        var options=SpotifyPlacement.atPixels(SpotifyOptions.defaults(),300,250,1000,700);var before=SpotifyPlacement.bounds(options,1000,700);
        for(int scale:new int[]{25,26,37,50,149,150}){var resized=SpotifyPlacement.resize(options,scale,1000,700);var after=SpotifyPlacement.bounds(resized,1000,700);assertEquals(scale,resized.scale());assertEquals(before.x()+before.width()/2d,after.x()+after.width()/2d,1);assertEquals(before.y()+before.height()/2d,after.y()+after.height()/2d,1);checkBounds(resized,1000,700);}
        assertEquals(25,SpotifyPlacement.resize(options,-5,1000,700).scale());assertEquals(150,SpotifyPlacement.resize(options,999,1000,700).scale());
    }
    @Test void customPositionAdaptsToResolutionAndPresetRestoresAnchor(){
        var options=SpotifyOptions.defaults().at(.37,.68);for(int[] size:new int[][]{{320,240},{960,700},{1920,1080}}){var b=SpotifyPlacement.bounds(options,size[0],size[1]);assertEquals(.37,b.x()/(double)(size[0]-b.width()),.01);assertEquals(.68,b.y()/(double)(size[1]-b.height()),.01);}
        var preset=options.anchor(2);assertFalse(preset.freePosition());var b=SpotifyPlacement.bounds(preset,1000,700);assertEquals(744,b.x());assertEquals(6,b.y());
    }
    @Test void cosmeticAndConnectionChangesPreserveCustomPlacement(){
        var original=SpotifyOptions.defaults().at(.237,.619).size(31);var styled=original.appearance(true,false,45,3).source(SpotifySource.WEB_API).clientId(SpotifyTest.ID);
        assertTrue(styled.freePosition());assertEquals(original.x(),styled.x());assertEquals(original.y(),styled.y());assertEquals(SpotifyTest.ID,styled.clientId());assertEquals(45,styled.scale());assertEquals(3,styled.palette());assertFalse(original.enabled());
        var invalid=original.at(Double.NaN,Double.POSITIVE_INFINITY);assertEquals(0,invalid.x());assertEquals(0,invalid.y());assertEquals(0,original.at(-1,-1).x());assertEquals(1,original.at(2,2).y());
    }
    @Test void startingAndStoppingDesktopDoNotResetLayout()throws Exception{
        var media=new DesktopMedia(){public Snapshot read(long now){return new Snapshot(true,null,"Ready");}public void close(){}};
        try(var service=new SpotifyService(folder.resolve("spotify.json"),new SpotifyApi((u,b,a)->{fail("Placement must not fetch account data");return null;}),media)){
            var value=SpotifyPlacement.atPixels(SpotifyOptions.defaults().size(25),137,89,960,700);service.save(value);service.startDesktop();assertTrue(service.options().freePosition());assertEquals(value.x(),service.options().x());assertEquals(value.y(),service.options().y());assertEquals(25,service.options().scale());service.stopDesktop();assertFalse(service.options().enabled());assertTrue(service.options().freePosition());assertEquals(value.x(),service.options().x());assertEquals(value.y(),service.options().y());
        }
    }
}
