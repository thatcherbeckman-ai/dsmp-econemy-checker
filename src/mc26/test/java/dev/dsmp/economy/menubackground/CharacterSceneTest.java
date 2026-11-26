package dev.dsmp.economy.menubackground;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class CharacterSceneTest {
    @Test void oldMenuPreferencesDefaultToNoCharacter(@TempDir Path dir)throws Exception{
        Files.writeString(dir.resolve("menu-background.json"),"{\"scene\":\"CHERRY_DAWN\",\"overlay\":25}");var options=new MenuBackgroundStore(dir).load();assertFalse(options.character.enabled);assertEquals(MenuScene.CHERRY_DAWN,options.scene);assertEquals(25,options.overlay);options.validate();
    }
    @Test void characterTextAndSceneryRoundTripTogether(@TempDir Path dir)throws Exception{
        var options=new MenuBackgroundOptions();options.scene=MenuScene.AURORA_LAKE;options.character.enabled=true;options.character.platform=CharacterComposition.Platform.SNOW;options.character.view=CharacterComposition.View.LEFT;options.character.title="Hello {player}";options.character.subtitle="My world\nMy adventure";options.character.titleColor=0xFFD1E3;options.character.font=2;options.character.x=28;
        var store=new MenuBackgroundStore(dir);store.save(options);var restored=store.load();assertEquals(MenuScene.AURORA_LAKE,restored.scene);assertTrue(restored.character.enabled);assertEquals(CharacterComposition.Platform.SNOW,restored.character.platform);assertEquals(options.character.subtitle,restored.character.subtitle);assertEquals(0xFFD1E3,restored.character.titleColor);assertEquals(2,restored.character.font);assertEquals(28,restored.character.x);
    }
    @Test void cancelledDraftCopiesDoNotShareCompositionFields(){
        var saved=new MenuBackgroundOptions();var draft=saved.copy();draft.character.title="New";draft.character.subtitle="Caption";draft.character.enabled=true;draft.character.view=CharacterComposition.View.FRONT;draft.character.titleColor=0x123456;assertFalse(saved.character.enabled);assertEquals("{player}",saved.character.title);assertEquals("",saved.character.subtitle);assertEquals(CharacterComposition.View.REAR,saved.character.view);assertEquals(0xFFFFFF,saved.character.titleColor);
    }
    @Test void textAllowsUnicodeAndNewlinesButRejectsControlsAndExcessLines(){
        var c=new CharacterComposition();c.title="Hello \u2605 {player}";c.subtitle="One\nTwo\nThree\nFour";c.validate();c.subtitle="1\n2\n3\n4\n5";assertThrows(IllegalArgumentException.class,c::validate);c.subtitle="bad\rtext";assertThrows(IllegalArgumentException.class,c::validate);c.subtitle="x".repeat(241);assertThrows(IllegalArgumentException.class,c::validate);c.subtitle="";c.title=null;assertThrows(IllegalArgumentException.class,c::validate);
    }
    @Test void invalidCameraAndTextSettingsAreRejected(){
        var c=new CharacterComposition();c.turn=181;assertThrows(IllegalArgumentException.class,c::validate);c.turn=0;c.size=90;assertThrows(IllegalArgumentException.class,c::validate);c.size=38;c.titleColor=-1;assertThrows(IllegalArgumentException.class,c::validate);c.titleColor=0;c.view=null;assertThrows(IllegalArgumentException.class,c::validate);
    }
    @Test void allPlatformsAndExtremeLayoutsFitTheVisibleComposition(){
        for(var material:CharacterComposition.Platform.values())for(int[] size:new int[][]{{320,240},{480,360},{960,700},{1280,720},{150,90}})for(int x:new int[]{0,50,100})for(int body:new int[]{20,55})for(int platform:new int[]{50,150}){
            var c=new CharacterComposition();c.platform=material;c.x=x;c.size=body;c.platformSize=platform;var p=CharacterPlacement.of(c,size[0],size[1]);assertTrue(p.bodyHeight()>0);assertTrue(p.feetY()-p.bodyHeight()>=0);assertTrue(p.feetY()+p.platformSize()*.55<=size[1]);assertTrue(p.centerX()-p.platformSize()/2>=0);assertTrue(p.centerX()+p.platformSize()/2<=size[0]);
        }
    }
    @Test void texturedPlatformsHaveRealDetailAndTheLedgeMeetsTheBottom(){
        var texture=new java.awt.image.BufferedImage(16,16,java.awt.image.BufferedImage.TYPE_INT_ARGB);for(int y=0;y<16;y++)for(int x=0;x<16;x++)texture.setRGB(x,y,(x+y)%2==0?0xFFC4AD87:0xFF776348);
        var options=new CharacterComposition();options.base=CharacterComposition.Base.BLOCK;var block=PlatformSurface.create(texture,texture,options,MenuScene.CHERRY_DAWN,512);assertEquals(0,block.getRGB(256,511)>>>24);
        options.base=CharacterComposition.Base.LEDGE;var ledge=PlatformSurface.create(texture,texture,options,MenuScene.CHERRY_DAWN,768);assertEquals(768,ledge.getHeight());assertTrue((ledge.getRGB(256,767)>>>24)>0);assertEquals(0,ledge.getRGB(0,0)>>>24);
        var colors=new java.util.HashSet<Integer>();for(int y=50;y<250;y+=7)for(int x=100;x<400;x+=7)colors.add(ledge.getRGB(x,y));assertTrue(colors.size()>10);assertTrue(PlatformSurface.create(texture,texture,options,MenuScene.WINTER_CABIN,768).getRGB(256,128)!=ledge.getRGB(256,128));
    }
    @Test void allEightViewsAndLightingSettingsAreIndependentCopies(){
        assertEquals(8,CharacterComposition.View.values().length);var options=new CharacterComposition();options.base=CharacterComposition.Base.DECK;options.matchLighting=false;var copy=options.copy();assertEquals(CharacterComposition.Base.DECK,copy.base);assertFalse(copy.matchLighting);copy.base=CharacterComposition.Base.BLOCK;assertEquals(CharacterComposition.Base.DECK,options.base);
    }
    @Test void corruptNestedOptionsArePreservedAndOtherCosmeticsAreUntouched(@TempDir Path dir)throws Exception{
        var file=dir.resolve("menu-background.json");String broken="{\"character\":{\"view\":\"UNKNOWN\"}}";Files.writeString(file,broken);Files.writeString(dir.resolve("inventory-themes.json"),"original inventory settings");var store=new MenuBackgroundStore(dir);assertThrows(IOException.class,store::load);assertEquals(broken,Files.readString(file));assertEquals("original inventory settings",Files.readString(dir.resolve("inventory-themes.json")));
    }
}
