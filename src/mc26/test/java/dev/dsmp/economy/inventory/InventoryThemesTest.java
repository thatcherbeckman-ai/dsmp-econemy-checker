package dev.dsmp.economy.inventory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class InventoryThemesTest {
    @Test void absentSettingsKeepVanillaAndAllPreferencesRoundTrip(@TempDir Path dir)throws Exception{
        var store=new InventoryThemeStore(dir.resolve("inventory-themes.json"));assertEquals(InventoryOptions.defaults(),store.load());
        var preferences=new InventoryOptions(InventoryTheme.HALLOWEEN,false,175,2);store.save(preferences);assertEquals(preferences,store.load());
        assertEquals(1,Files.list(dir).count());
    }
    @Test void immutableDraftAndBoundsKeepCancelAndCorruptValuesSafe(){
        var original=InventoryOptions.defaults();var draft=original.theme(InventoryTheme.CHRISTMAS).animations(false).speed(300).intensity(2);
        assertEquals(InventoryTheme.VANILLA,original.theme());assertTrue(original.animations());assertEquals(200,draft.speed());assertEquals(25,draft.speed(-100).speed());
        assertThrows(IllegalArgumentException.class,()->new InventoryOptions(null,true,100,1));assertThrows(IllegalArgumentException.class,()->new InventoryOptions(InventoryTheme.WINTER,true,0,1));assertThrows(IllegalArgumentException.class,()->original.intensity(3));
    }
    @Test void brokenAndOversizedFilesAreRetained(@TempDir Path dir)throws Exception{
        Path file=dir.resolve("inventory-themes.json");var store=new InventoryThemeStore(file);
        for(String bad:new String[]{"null","{broken","{\"theme\":\"UNKNOWN\",\"speed\":100,\"intensity\":1}"," ".repeat(17000)}){Files.writeString(file,bad);assertThrows(IOException.class,store::load);assertEquals(bad,Files.readString(file));}
    }
    @Test void themeSavesDoNotChangeOtherSettingsAndFailedSaveKeepsPreviousFile(@TempDir Path dir)throws Exception{
        Path other=dir.resolve("config.json");Files.writeString(other,"existing settings");Path file=dir.resolve("inventory-themes.json");var store=new InventoryThemeStore(file);
        store.save(InventoryOptions.defaults());String before=Files.readString(file);assertThrows(IllegalArgumentException.class,()->store.save(null));assertEquals(before,Files.readString(file));assertEquals("existing settings",Files.readString(other));
        Path blocked=dir.resolve("blocked");Files.writeString(blocked,"not a directory");assertThrows(IOException.class,()->new InventoryThemeStore(blocked.resolve("prefs.json")).save(InventoryOptions.defaults()));assertEquals("not a directory",Files.readString(blocked));
    }
    @Test void symbolicLinksAreRejectedWithoutChangingTheirTarget(@TempDir Path dir)throws Exception{
        Path target=dir.resolve("original.json"),link=dir.resolve("inventory-themes.json");Files.writeString(target,"retained");Files.createSymbolicLink(link,target);
        var store=new InventoryThemeStore(link);assertThrows(IOException.class,store::load);assertThrows(IOException.class,()->store.save(InventoryOptions.defaults()));assertEquals("retained",Files.readString(target));
    }
    @Test void holidayArtworkIsDistinctBoundedAndAffordable(){
        var unique=new java.util.HashSet<Object>();
        for(var theme:InventoryTheme.values()){
            var options=InventoryOptions.defaults().theme(theme).intensity(2);var panel=HolidayScene.panel(options,123);
            if(theme==InventoryTheme.VANILLA){assertTrue(panel.isEmpty());continue;}
            assertFalse(panel.isEmpty());assertTrue(panel.size()<600);unique.add(panel);
            panel.forEach(r->bounds(r,176,166));
            for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720}}){var ambient=HolidayScene.ambient(options,size[0],size[1],123);assertTrue(ambient.size()<1800);ambient.forEach(r->bounds(r,size[0],size[1]));}
        }
        assertEquals(InventoryTheme.values().length-1,unique.size());
    }
    @Test void animationFreezesAndSpeedChangesMotionWithoutInvalidGeometry(){
        for(var theme:InventoryTheme.values()){
            var frozen=InventoryOptions.defaults().theme(theme).animations(false);assertEquals(HolidayScene.panel(frozen,1),HolidayScene.panel(frozen,777));assertEquals(HolidayScene.ambient(frozen,640,480,1),HolidayScene.ambient(frozen,640,480,777));
            var animated=frozen.animations(true);
            if(theme!=InventoryTheme.VANILLA){assertNotEquals(HolidayScene.panel(animated,1),HolidayScene.panel(animated,17));assertNotEquals(HolidayScene.ambient(animated,640,480,1),HolidayScene.ambient(animated.speed(200),640,480,1));}
            for(double time:new double[]{Double.NaN,Double.POSITIVE_INFINITY,-20,Double.MAX_VALUE})HolidayScene.panel(animated,time).forEach(r->bounds(r,176,166));
        }
    }
    @Test void denserEffectsFillTheViewportInsteadOfFormingADiagonalBand(){
        for(var theme:InventoryTheme.values()){
            if(theme==InventoryTheme.VANILLA)continue;
            var low=InventoryOptions.defaults().theme(theme).intensity(0);
            var medium=low.intensity(1);var high=low.intensity(2);
            for(double time:new double[]{0,1,17}){
                var light=HolidayScene.ambient(low,960,700,time);var normal=HolidayScene.ambient(medium,960,700,time);var full=HolidayScene.ambient(high,960,700,time);
                assertTrue(normal.size()>light.size(),theme.label);assertTrue(full.size()>normal.size(),theme.label);assertTrue(full.size()>light.size()*2,theme.label);
                var regions=new java.util.HashSet<Integer>();for(var r:normal){int column=Math.min(2,(r.x()+r.width()/2)*3/960),row=Math.min(2,(r.y()+r.height()/2)*3/700);regions.add(row*3+column);}
                assertTrue(regions.size()>=8,"Sparse screen coverage: "+theme.label);
            }
        }
    }
    private static void bounds(HolidayScene.Rect r,int width,int height){assertTrue(r.x()>=0&&r.y()>=0&&r.width()>0&&r.height()>0);assertTrue(r.x()+r.width()<=width&&r.y()+r.height()<=height);}
}
