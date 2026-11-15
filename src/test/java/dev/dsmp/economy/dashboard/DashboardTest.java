package dev.dsmp.economy.dashboard;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.LocalStore;
import dev.dsmp.economy.core.Ledger;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DashboardTest {
    @Test void oldSettingsGetTheWinterThemeAndPreferencesPersist(@TempDir Path dir)throws Exception{
        var cfg=new Gson().fromJson("{\"scale\":0.8,\"opacity\":155}",TrackerConfig.class);cfg.validate();assertTrue(cfg.festiveBackground);assertTrue(cfg.animateSnow);assertEquals(.8f,cfg.scale);assertEquals(155,cfg.opacity);
        cfg.festiveBackground=false;cfg.animateSnow=false;cfg.menuAccent=0x4EC29C;var store=new LocalStore(dir);store.saveConfig(cfg);var restored=store.loadConfig();assertFalse(restored.festiveBackground);assertFalse(restored.animateSnow);assertEquals(cfg.menuAccent,restored.menuAccent);
    }
    @Test void moduleTogglesOnlyChangeTheSelectedModule(){
        var cfg=new TrackerConfig();var model=new DashboardSettings(cfg);for(int id=0;id<6;id++){assertTrue(model.visible(id));model.visible(id,false);assertFalse(model.visible(id));for(int other=0;other<6;other++)if(other!=id)assertTrue(model.visible(other));model.visible(id,true);}cfg.validate();
    }
    @Test void cancelRestoresUnsavedSettingsAndCheckpointsKeepChildSaves(){
        var cfg=new TrackerConfig();var model=new DashboardSettings(cfg);model.visible(2,false);cfg.animateSnow=false;var restored=model.cancel();assertTrue(restored.panels[2].visible);assertTrue(restored.animateSnow);
        model.checkpoint(cfg);model.visible(0,false);cfg.menuAccent=0x123456;restored=model.cancel();assertFalse(restored.panels[2].visible);assertFalse(restored.animateSnow);assertTrue(restored.panels[0].visible);assertEquals(0x8B6CF2,restored.menuAccent);
    }
    @Test void sharedControlsRespectTheirBoundsAndInvalidAccentIsRejected(){
        var cfg=new TrackerConfig();var model=new DashboardSettings(cfg);model.waitSeconds(-999);assertEquals(5,cfg.paymentSettlementSeconds);model.waitSeconds(999);assertEquals(600,cfg.paymentSettlementSeconds);model.scale(-99);assertEquals(.4f,cfg.scale);model.scale(99);assertEquals(1.5f,cfg.scale);model.opacity(-999);assertEquals(40,cfg.opacity);model.opacity(999);assertEquals(240,cfg.opacity);
        int accent=cfg.menuAccent;for(int i=0;i<5;i++)model.nextAccent();assertEquals(accent,cfg.menuAccent);cfg.menuAccent=-1;assertThrows(IllegalArgumentException.class,cfg::validate);
    }
    @Test void winterArtworkAndWeatherStayInsideResponsiveViewports(){
        for(int[] size:new int[][]{{320,240},{640,480},{1280,720}}){var rects=new ArrayList<>(ChristmasScene.background(size[0],size[1],0x8B6CF2,true));rects.addAll(ChristmasScene.weather(size[0],size[1],23,true,true));for(var r:rects){assertTrue(r.x1()>=0&&r.y1()>=0);assertTrue(r.x2()<=size[0]&&r.y2()<=size[1]);assertTrue(r.x2()>r.x1()&&r.y2()>r.y1());}}
    }
    @Test void animationCanBeFrozenOrTheChristmasBackgroundDisabled(){
        assertEquals(ChristmasScene.weather(640,480,1,true,false),ChristmasScene.weather(640,480,99,true,false));assertNotEquals(ChristmasScene.weather(640,480,1,true,true),ChristmasScene.weather(640,480,99,true,true));assertTrue(ChristmasScene.weather(640,480,9,false,true).isEmpty());assertTrue(ChristmasScene.background(640,480,0x8B6CF2,true).size()>ChristmasScene.background(640,480,0x8B6CF2,false).size());
    }
    @Test void compactLayoutKeepsNavigationModulesAndLoanPaginationAccessible(){
        for(int[] size:new int[][]{{320,240},{640,480},{1280,720}}){var d=DashboardLayout.of(size[0],size[1]);assertTrue(d.left()+d.contentWidth()<=size[0]);assertTrue(d.navY(8)+d.navStep()-2<size[1]-16);assertTrue(d.rowY(5)+d.rowHeight()-4<size[1]-18);assertTrue(d.loanRows()>=1);}
    }
    @Test void balanceCurveUsesOnlyObservedExtremaWithoutAnInventedZero(){
        var points=List.of(new Ledger.Point(1,1000),new Ledger.Point(2,2000),new Ledger.Point(3,1500),new Ledger.Point(4,500));var plot=BalanceCurve.build(points);assertEquals(500,plot.min());assertEquals(2000,plot.max());assertEquals(1000,plot.samples().getFirst().cents());assertEquals(500,plot.samples().getLast().cents());assertEquals(0,plot.samples().getFirst().x());assertEquals(1,plot.samples().getLast().x());assertTrue(plot.samples().stream().allMatch(p->p.cents()>=500&&p.cents()<=2000));
    }
    @Test void emptyAndSingleObservationPlotsDontFabricateActivity(){
        assertTrue(BalanceCurve.build(List.of()).samples().isEmpty());var single=BalanceCurve.build(List.of(new Ledger.Point(1,123400)));assertEquals(1,single.samples().size());assertEquals(.5,single.samples().getFirst().x());assertEquals(123400,single.min());assertEquals(123400,single.max());
    }
}
