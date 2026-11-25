package dev.dsmp.economy.menubackground;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

class MenuBackgroundTest {
    @Test void oldAndNewPreferencesRoundTripWithoutChangingOtherFiles(@TempDir Path dir)throws Exception{
        var store=new MenuBackgroundStore(dir);assertEquals(MenuScene.ORIGINAL,store.load().scene);
        Files.writeString(dir.resolve("config.json"),"old settings");Files.writeString(dir.resolve("menu-background.json"),"{\"scene\":\"CHERRY_DAWN\"}");var prefs=store.load();assertEquals(100,prefs.brightness);assertTrue(prefs.animations);
        prefs.blur=3;prefs.particles=MenuScene.Particles.PETALS;prefs.cards=70;prefs.overlay=40;store.save(prefs);var saved=store.load();assertEquals(MenuScene.CHERRY_DAWN,saved.scene);assertEquals(3,saved.blur);assertEquals(70,saved.cards);assertEquals(40,saved.overlay);assertEquals("old settings",Files.readString(dir.resolve("config.json")));
    }
    @Test void draftsAreDeeplyIndependentAndInvalidOptionsAreRejected(){
        var prefs=new MenuBackgroundOptions();var draft=prefs.copy();draft.scene=MenuScene.SPACE_NEBULA;draft.customImage="a".repeat(64)+".png";assertEquals(MenuScene.ORIGINAL,prefs.scene);assertEquals("",prefs.customImage);
        for(int value:new int[]{-1,81}){var bad=prefs.copy();bad.overlay=value;assertThrows(IllegalArgumentException.class,bad::validate);}var bad=prefs.copy();bad.scene=null;assertThrows(IllegalArgumentException.class,bad::validate);bad=prefs.copy();bad.customImage="../photo.png";assertThrows(IllegalArgumentException.class,bad::validate);
    }
    @Test void brokenAndOversizedPreferencesStayUntouched(@TempDir Path dir)throws Exception{
        var file=dir.resolve("menu-background.json");var store=new MenuBackgroundStore(dir);
        for(String text:new String[]{"null","broken","{\"scene\":\"BAD\"}"," ".repeat(33000)}){Files.writeString(file,text);assertThrows(IOException.class,store::load);assertEquals(text,Files.readString(file));}
    }
    @Test void pngImportsKeepOriginalsAndUseSafeContentNames(@TempDir Path dir)throws Exception{
        var original=dir.resolve("photo.png");ImageIO.write(new BufferedImage(32,16,BufferedImage.TYPE_INT_ARGB),"PNG",original.toFile());byte[] before=Files.readAllBytes(original);var store=new MenuBackgroundStore(dir.resolve("config"));String key=store.importPng(original);assertTrue(key.matches("[a-f0-9]{64}\\.png"));assertEquals(key,store.importPng(original));assertArrayEquals(before,store.image(key));assertArrayEquals(before,Files.readAllBytes(original));assertThrows(IOException.class,()->store.image("../photo.png"));
        Files.writeString(dir.resolve("bad.png"),"not a PNG");assertThrows(IOException.class,()->store.importPng(dir.resolve("bad.png")));
        Path link=dir.resolve("link.png");Files.createSymbolicLink(link,original);assertThrows(IOException.class,()->store.importPng(link));
    }
    @Test void importSizeAndPixelLimitsRejectOversizedPictures(@TempDir Path dir)throws Exception{
        Path large=dir.resolve("large.png");try(var file=new java.io.RandomAccessFile(large.toFile(),"rw")){file.setLength(8L*1024*1024+1);}var store=new MenuBackgroundStore(dir.resolve("config"));assertThrows(IOException.class,()->store.importPng(large));
        ImageIO.write(new BufferedImage(4097,1,BufferedImage.TYPE_INT_RGB),"PNG",large.toFile());assertThrows(IOException.class,()->store.importPng(large));
    }
    @Test void failedSavesAndLinksKeepExistingFiles(@TempDir Path dir)throws Exception{
        Path original=dir.resolve("original");Files.writeString(original,"retained");Files.createSymbolicLink(dir.resolve("menu-background.json"),original);var store=new MenuBackgroundStore(dir);assertThrows(IOException.class,store::load);assertThrows(IOException.class,()->store.save(new MenuBackgroundOptions()));assertEquals("retained",Files.readString(original));assertThrows(IOException.class,()->new MenuBackgroundStore(original).save(new MenuBackgroundOptions()));
    }
    @Test void imageEffectsAreBoundedAndLeaveTheSourceIntact(){
        var image=new BufferedImage(16,8,BufferedImage.TYPE_INT_RGB);for(int x=0;x<16;x++)for(int y=0;y<8;y++)image.setRGB(x,y,x<8?0:0xFFFFFF);var options=new MenuBackgroundOptions();options.blur=3;var blurred=MenuImageEffects.prepare(image,options);int edge=blurred.getRGB(7,3)&255;assertTrue(edge>0&&edge<255);assertEquals(0,image.getRGB(7,3)&0xFFFFFF);
        options.blur=0;options.brightness=50;assertEquals(128,MenuImageEffects.prepare(image,options).getRGB(12,3)&255);options.brightness=100;options.tint=1;options.tintAmount=50;assertNotEquals(0xFFFFFF,MenuImageEffects.prepare(image,options).getRGB(12,3)&0xFFFFFF);
        var scaled=MenuImageEffects.prepare(new BufferedImage(2048,1024,BufferedImage.TYPE_INT_RGB),options);assertEquals(1024,scaled.getWidth());assertEquals(512,scaled.getHeight());
    }
    @Test void particlesStayBoundedCanFreezeAndRespectOffAndDensity(){
        for(var kind:MenuScene.Particles.values())for(int[] size:new int[][]{{320,240},{960,700}}){var options=new MenuBackgroundOptions();options.particles=kind;options.density=2;options.animations=false;var a=MenuParticles.frame(options,size[0],size[1],1);assertEquals(a,MenuParticles.frame(options,size[0],size[1],999));assertTrue(a.size()<700);for(var r:a){assertTrue(r.x()>=0&&r.y()>=0&&r.x()+r.width()<=size[0]&&r.y()+r.height()<=size[1]);assertTrue(r.width()>0&&r.height()>0);}}
        var options=new MenuBackgroundOptions();options.scene=MenuScene.CHERRY_DAWN;assertEquals(MenuScene.Particles.PETALS,options.effectiveParticles());options.density=0;var low=MenuParticles.frame(options,960,700,7);options.density=2;assertTrue(MenuParticles.frame(options,960,700,7).size()>low.size()*2);options.particles=MenuScene.Particles.NONE;assertTrue(MenuParticles.frame(options,960,700,7).isEmpty());
    }
    @Test void allScenicTilesArePackagedAndHaveTheExpectedAtlasGeometry()throws Exception{
        for(var scene:MenuScene.values())if(!scene.atlas.isEmpty()){
            String path="assets/economy_tracker/textures/menu/"+scene.atlas+".png";
            try(var stream=getClass().getClassLoader().getResourceAsStream(path)){
                // Source compilation uses resources on disk; the packaged run reads actual JAR bytes.
                var image=stream!=null?ImageIO.read(stream):ImageIO.read(Path.of("src/mc26/resources").resolve(path).toFile());assertEquals(0,image.getWidth()%3);assertEquals(0,image.getHeight()%2);assertTrue(scene.tile>=0&&scene.tile<6);assertTrue(image.getWidth()>=1200&&image.getHeight()>=800);
            }
        }
    }
}
