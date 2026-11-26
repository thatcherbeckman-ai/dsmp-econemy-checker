package dev.dsmp.economy.menubackground;
import dev.dsmp.economy.inventory.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MenuHolidayTest {
    @Test void allInventoryHolidaysAndSeasonsCanBeChosenIndependently(){var inventory=InventoryOptions.defaults().theme(InventoryTheme.HALLOWEEN);for(var theme:InventoryTheme.values()){var menu=new MenuBackgroundOptions();WindowBackdrop.holiday(menu,theme);menu.validate();assertEquals(theme,menu.menuTheme);assertEquals(theme==InventoryTheme.VANILLA?MenuScene.ORIGINAL:MenuScene.HOLIDAY,menu.scene);assertEquals(InventoryTheme.HALLOWEEN,inventory.theme());assertEquals(theme,WindowBackdrop.animation(menu).theme());}}
    @Test void themeDraftsAndPersistenceKeepInventorySettingsSeparate(@TempDir Path dir)throws Exception{var saved=new MenuBackgroundOptions();var draft=saved.copy();WindowBackdrop.holiday(draft,InventoryTheme.SPRING);draft.speed=175;draft.density=2;draft.animations=false;assertEquals(MenuScene.ORIGINAL,saved.scene);var store=new MenuBackgroundStore(dir);store.save(draft);var loaded=store.load();assertEquals(InventoryTheme.SPRING,loaded.menuTheme);assertEquals(175,loaded.speed);assertEquals(2,loaded.density);assertFalse(loaded.animations);assertEquals(InventoryTheme.SPRING,loaded.copy().menuTheme);}
    @Test void menuMotifsAreBoundedDistinctAndFreezeWithAnimationOff(){var distinct=new HashSet<Integer>();for(var theme:InventoryTheme.values()){var menu=new MenuBackgroundOptions();WindowBackdrop.holiday(menu,theme);menu.animations=false;var options=WindowBackdrop.animation(menu);var a=HolidayScene.ambient(options,320,200,1);assertEquals(a,HolidayScene.ambient(options,320,200,900));distinct.add(a.hashCode());for(var r:HolidayScene.header(options,320)){assertTrue(r.x()>=0&&r.y()>=0&&r.x()+r.width()<=320&&r.y()+r.height()<=12);}for(var r:a)assertTrue(r.x()>=0&&r.y()>=0&&r.x()+r.width()<=320&&r.y()+r.height()<=200);}assertTrue(distinct.size()>=16);}
    @Test void themeImageUsesItsOwnPaletteAndClearingThemeRestoresFestive(){var menu=new MenuBackgroundOptions();WindowBackdrop.holiday(menu,InventoryTheme.HALLOWEEN);var image=WindowBackdrop.create(40,20,menu);assertNotEquals(0x301316,image.getRGB(20,0)&0xFFFFFF);menu.solidColor=0x112233;WindowBackdrop.holiday(menu,InventoryTheme.VANILLA);assertEquals(0x301316,menu.solidColor);assertEquals(MenuScene.ORIGINAL,menu.scene);}
}
