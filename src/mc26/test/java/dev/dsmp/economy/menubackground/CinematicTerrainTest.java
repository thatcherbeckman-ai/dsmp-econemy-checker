package dev.dsmp.economy.menubackground;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class CinematicTerrainTest {
    private BufferedImage texture(){var image=new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setRGB(x,y,(x+y)%3==0?0xD9E5EC:0xF2F8FF);return image;}
    @Test void referencePresetPersistsWithoutChangingTheSavedDraft(@TempDir Path dir)throws Exception{
        var saved=new MenuBackgroundOptions();var draft=saved.copy();CinematicTerrain.preset(draft);draft.validate();
        assertEquals(MenuScene.ORIGINAL,saved.scene);assertFalse(saved.character.nameplate);
        var store=new MenuBackgroundStore(dir);store.save(draft);var loaded=store.load();
        assertEquals(MenuScene.MINECRAFT_SNOW,loaded.scene);assertTrue(loaded.character.nameplate);assertTrue(loaded.character.enabled);
        assertEquals(20,loaded.character.view.yaw+loaded.character.turn);assertEquals("",loaded.character.title);assertEquals(0,loaded.overlay);
    }
    @Test void texturedTerrainCoversTheFootingAndMatchesTheProjectedCharacter(){
        var options=new MenuBackgroundOptions();CinematicTerrain.preset(options);var texture=texture();
        for(int[] size:new int[][]{{192,140},{320,180}}){
            var result=CinematicTerrain.create(size[0],size[1],options.character,texture,texture,texture);
            var p=CharacterPlacement.of(options.character,size[0],size[1]);
            assertTrue((result.getRGB(p.centerX(),p.feetY()+2)&255)>150,"Snow must be under the character's feet");
            assertNotEquals(result.getRGB(0,0),result.getRGB(0,size[1]-1));
            var colors=new java.util.HashSet<Integer>();for(int y=size[1]/2;y<size[1];y++)for(int x=0;x<size[0];x++)colors.add(result.getRGB(x,y));
            assertTrue(colors.size()>50,"Perspective, block lighting and distance fog must remain visible");
            assertEquals(0xFFF2F8FF,texture.getRGB(1,0));
        }
        assertThrows(IllegalArgumentException.class,()->CinematicTerrain.create(4096,1024,options.character,texture,texture,texture));
    }
    @Test void everyEarlierScenicChoiceHasADistinctCinematicPaletteAndKeepsPhotoMode(){
        var options=new MenuBackgroundOptions();assertFalse(CinematicTerrain.usesBlocks(options));options.character.sceneryStyle=CharacterComposition.SceneryStyle.BLOCKS;
        var images=new java.util.HashSet<Integer>();int scenes=0;
        for(var scene:MenuScene.values())if(!scene.atlas.isEmpty()){
            options.scene=scene;assertTrue(CinematicTerrain.usesBlocks(options));var palette=BlockScenery.of(scene);
            var textures=new java.util.HashMap<String,BufferedImage>();for(var name:new String[]{palette.top(),palette.side(),palette.rock(),palette.trunk(),palette.leaves()})textures.put(name,texture());
            var image=CinematicTerrain.create(80,60,options.character,scene,textures);images.add(java.util.Arrays.hashCode(image.getRGB(0,0,80,60,null,0,80)));scenes++;
        }
        assertEquals(18,scenes);assertTrue(images.size()>=17);assertEquals(CharacterComposition.SceneryStyle.BLOCKS,options.copy().character.sceneryStyle);
        options.scene=MenuScene.CUSTOM;assertFalse(CinematicTerrain.usesBlocks(options));options.scene=MenuScene.CHERRY_DAWN;options.character.sceneryStyle=CharacterComposition.SceneryStyle.PHOTO;assertFalse(CinematicTerrain.usesBlocks(options));
    }
    @Test void illustratedForegroundKeepsTheSkyTransparentAndSupportsTheCharactersFeet(){
        var options=new MenuBackgroundOptions();options.character.enabled=true;options.character.size=36;options.character.ground=83;
        for(var scene:new MenuScene[]{MenuScene.LAVENDER_FIELDS,MenuScene.STARRY_MOUNTAINS,MenuScene.RAINY_CITY,MenuScene.SPACE_NEBULA,MenuScene.FLOATING_ISLANDS,MenuScene.BAMBOO_FOREST}){
            options.scene=scene;assertTrue(CinematicTerrain.usesIllustrated(options));var palette=BlockScenery.of(scene);var textures=new java.util.HashMap<String,BufferedImage>();for(var name:new String[]{palette.top(),palette.side(),palette.rock()})textures.put(name,texture());
            var image=CinematicTerrain.foreground(192,140,options.character,scene,textures);var p=CharacterPlacement.of(options.character,192,140);
            assertEquals(0,image.getRGB(96,0)>>>24);assertTrue((image.getRGB(p.centerX(),p.feetY()+2)>>>24)>240);assertTrue((image.getRGB(96,139)>>>24)>240);
            boolean feathered=false;for(int y=0;y<140;y++)for(int x=0;x<192;x++){int alpha=image.getRGB(x,y)>>>24;if(alpha>5&&alpha<240)feathered=true;}assertTrue(feathered,"The ridge must feather into the illustrated background");
        }
    }
}
