package dev.dsmp.economy.menubackground;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
class WindowBackdropTest {
    @Test void everySolidPresetIsExactlyUniform(){for(int color:WindowBackdrop.COLORS){var o=new MenuBackgroundOptions();WindowBackdrop.solid(o,color);var image=WindowBackdrop.create(35,23,o);for(int x=0;x<35;x++)for(int y=0;y<23;y++)assertEquals(color,image.getRGB(x,y)&0xFFFFFF);assertEquals(MenuScene.Particles.NONE,o.effectiveParticles());}}
    @Test void festivePreviewAndMenuUseTheSameEditableImageEffects(){var o=new MenuBackgroundOptions();var original=WindowBackdrop.create(40,30,o);assertNotEquals(original.getRGB(20,0),original.getRGB(20,29));o.brightness=50;var changed=MenuImageEffects.prepare(original,o);assertTrue((changed.getRGB(20,10)&255)<(original.getRGB(20,10)&255));}
    @Test void customColorsPersistAndDraftCopiesStayIndependent(@TempDir Path dir)throws Exception{var saved=new MenuBackgroundOptions();var draft=saved.copy();WindowBackdrop.solid(draft,0x123456);assertEquals(MenuScene.ORIGINAL,saved.scene);var store=new MenuBackgroundStore(dir);store.save(draft);var loaded=store.load();assertEquals(MenuScene.SOLID,loaded.scene);assertEquals(0x123456,loaded.solidColor);assertEquals(0x123456,loaded.copy().solidColor);}
    @Test void olderPreferencesGetTheFestiveColorAndInvalidColorsAreRejected(){var old=new com.google.gson.Gson().fromJson("{\"scene\":\"ORIGINAL\"}",MenuBackgroundOptions.class);assertEquals(0x301316,old.solidColor);old.validate();assertThrows(IllegalArgumentException.class,()->WindowBackdrop.solid(old,-1));old.solidColor=0x1000000;assertThrows(IllegalArgumentException.class,old::validate);}
}
