package dev.dsmp.economy.design;

import dev.dsmp.economy.*;
import dev.dsmp.economy.inventory.*;
import dev.dsmp.economy.menubackground.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DesignToolsTest {
    private DesignLook look(){return DesignLook.capture(new TrackerConfig(),new MenuBackgroundOptions(),InventoryOptions.defaults(),new AccessOptions());}
    @Test void profilesRestoreCosmeticsWithoutChangingTrackingPreferences(){
        var source=new TrackerConfig();source.menuAccent=0xABCDEF;source.scale=1.2f;var look=DesignLook.capture(source,new MenuBackgroundOptions(),InventoryOptions.defaults(),new AccessOptions());
        var active=new TrackerConfig();active.serverAddress="my-server";active.autoDetectPayments=false;active.scoreboardBalance=false;active.paymentSettlementSeconds=175;active.betPattern="";var next=look.applyTo(active);
        assertEquals(0xABCDEF,next.menuAccent);assertEquals(1.2f,next.scale);assertEquals(active.serverAddress,next.serverAddress);assertEquals(175,next.paymentSettlementSeconds);assertFalse(next.autoDetectPayments);assertFalse(next.scoreboardBalance);assertNotEquals(next.menuAccent,active.menuAccent);
        String json=DesignLook.JSON.toJson(look);assertFalse(json.contains("serverAddress"));assertFalse(json.contains("paymentSettlementSeconds"));look.appearance.addProperty("trackingEnabled",true);assertThrows(IllegalArgumentException.class,look::validate);
    }
    @Test void profileCopiesAndNamedOverwritesAreIndependent(@TempDir Path dir)throws Exception{
        var archive=new DesignArchive(dir);var original=look();var entry=archive.saveProfile("Forest",original);original.background.character.title="Changed";assertEquals("{player}",archive.load(false,entry.id()).look().background.character.title);
        original.background.character.title="New";var replacement=archive.saveProfile("forest",original);assertEquals(entry.id(),replacement.id());assertEquals(1,archive.list(false).size());assertEquals("New",archive.load(false,entry.id()).look().background.character.title);
        Files.writeString(dir.resolve("ledger.json"),"untouched record");archive.deleteProfile(entry.id());assertTrue(archive.list(false).isEmpty());assertEquals("untouched record",Files.readString(dir.resolve("ledger.json")));
    }
    @Test void archiveLimitsAndNamesAreBounded(@TempDir Path dir)throws Exception{
        var archive=new DesignArchive(dir);for(int i=0;i<24;i++)archive.saveProfile("Look "+i,look());assertThrows(IOException.class,()->archive.saveProfile("One too many",look()));assertThrows(IllegalArgumentException.class,()->archive.saveProfile("bad\nname",look()));assertThrows(IOException.class,()->archive.load(false,"../../settings"));
    }
    @Test void automaticBackupsDeduplicateAndRetainTwelveNewestAcrossRestart(@TempDir Path dir)throws Exception{
        var archive=new DesignArchive(dir);var look=look();archive.backup("Before save",look);archive.backup("Before save",look);assertEquals(1,archive.list(true).size());for(int i=0;i<20;i++){look.background.character.title="Revision "+i;new DesignArchive(dir).backup("Before edit",look);}var rows=archive.list(true);assertEquals(12,rows.size());assertEquals("Revision 19",archive.load(true,rows.getFirst().id()).look().background.character.title);
    }
    @Test void corruptAndLinkedDesignsAreRetainedAndOtherProfilesStayAccessible(@TempDir Path dir)throws Exception{
        var archive=new DesignArchive(dir);var entry=archive.saveProfile("Good",look());Path bad=dir.resolve("profiles/11111111-1111-1111-1111-111111111111.json");Files.writeString(bad,"broken");assertEquals(2,archive.list(false).size());assertEquals(1,archive.list(false).stream().filter(DesignArchive.Summary::valid).count());assertThrows(IOException.class,()->archive.load(false,"11111111-1111-1111-1111-111111111111"));assertEquals("broken",Files.readString(bad));
        Path target=dir.resolve("outside.json");Files.writeString(target,"retained");Path link=dir.resolve("profiles/22222222-2222-2222-2222-222222222222.json");Files.createSymbolicLink(link,target);assertThrows(IOException.class,()->archive.load(false,"22222222-2222-2222-2222-222222222222"));assertEquals("retained",Files.readString(target));assertNotNull(archive.load(false,entry.id()));
    }
    @Test void multiFileSavePreflightsAllDestinationsBeforeReplacingAnything(@TempDir Path dir)throws Exception{
        Path a=dir.resolve("a.json"),blocked=dir.resolve("blocked");Files.writeString(a,"original");Files.createDirectory(blocked);var edits=new LinkedHashMap<Path,String>();edits.put(a,"changed");edits.put(blocked,"new");assertThrows(IOException.class,()->DesignFiles.commit(edits));assertEquals("original",Files.readString(a));try(var files=Files.list(dir)){assertEquals(2,files.count());}
        edits.remove(blocked);edits.put(dir.resolve("b.json"),"second");DesignFiles.commit(edits);assertEquals("changed\n",Files.readString(a));assertEquals("second\n",Files.readString(dir.resolve("b.json")));
    }
    @Test void accessibilityValidationAndCorruptFilesPreserveOriginals(@TempDir Path dir)throws Exception{
        var file=dir.resolve("access.json");var store=new AccessStore(file);var access=new AccessOptions();access.textScale=130;access.highContrast=access.reducedMotion=true;store.save(access);var loaded=store.load();assertEquals(130,loaded.textScale);assertTrue(loaded.highContrast&&loaded.reducedMotion);access.textScale=150;assertThrows(IllegalArgumentException.class,()->store.save(access));Files.writeString(file,"{broken");assertThrows(IOException.class,store::load);assertEquals("{broken",Files.readString(file));
    }
    @Test void undoRedoAreDeepIndependentAndNewBranchesClearRedo(){
        var history=new SceneHistory();var scene=new MenuBackgroundOptions();var before=scene.copy();scene.character.title="First";history.record(before,scene);before=scene.copy();scene.character.title="Second";history.record(before,scene);scene=history.undo(scene);assertEquals("First",scene.character.title);scene=history.undo(scene);assertEquals("{player}",scene.character.title);scene=history.redo(scene);assertEquals("First",scene.character.title);before=scene.copy();scene.character.title="Branch";history.record(before,scene);assertFalse(history.canRedo());assertEquals("First",history.undo(scene).character.title);
    }
    @Test void historyLimitAndNoOpChangesArePredictable(){
        var h=new SceneHistory();var scene=new MenuBackgroundOptions();h.record(scene,scene.copy());assertFalse(h.canUndo());for(int i=0;i<100;i++){var old=scene.copy();scene.character.title="Edit "+i;h.record(old,scene);}int count=0;while(h.canUndo()){scene=h.undo(scene);count++;}assertEquals(64,count);
    }
    @Test void dragCoordinatesClampAndOnlySnapCloseToGuides(){
        var c=new CharacterComposition();SceneDrag.character(c,159,250,320,300);assertEquals(50,c.x);assertEquals(83,c.ground);c.snapGuides=false;SceneDrag.character(c,149,-200,320,300);assertEquals(47,c.x);assertEquals(55,c.ground);SceneDrag.caption(c,-100,900,320,300);assertTrue(c.freeCaption);assertEquals(0,c.captionX);assertEquals(100,c.captionY);
    }
    @Test void lightingAndCaptionOptionsRoundTripAndAffectShadows(){
        var c=new CharacterComposition();c.lightDirection=0;c.shadowSoftness=0;double right=SceneShadow.opacity(.4,0,c);c.lightDirection=180;assertTrue(right>SceneShadow.opacity(.4,0,c));c.shadowSoftness=90;var copy=c.copy();assertEquals(90,copy.shadowSoftness);copy.lightStrength=151;assertThrows(IllegalArgumentException.class,copy::validate);c.freeCaption=true;c.captionX=30;c.captionY=50;var background=new MenuBackgroundOptions();background.character=c;var restored=DesignLook.JSON.fromJson(DesignLook.JSON.toJson(background),MenuBackgroundOptions.class);restored.validate();assertTrue(restored.character.freeCaption);assertEquals(30,restored.character.captionX);
    }
    @Test void searchSupportsWordsSynonymsAndEmptyResults(){assertEquals("scene",SettingSearch.find("skin angle").getFirst().target());assertTrue(SettingSearch.find("shadow").stream().anyMatch(i->i.tab()==4));assertFalse(SettingSearch.find("contrast").isEmpty());assertTrue(SettingSearch.find("nonexistent xyz").isEmpty());assertTrue(SettingSearch.find("").size()>10);}
}
