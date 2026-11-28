package dev.dsmp.economy.menubackground;

import com.google.gson.Gson;
import dev.dsmp.economy.ui.MenuWindow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.*;

class MenuDisplayTest {
    @TempDir Path folder;
    @Test void oldPreferencesGetBrightDefaultsAndAnIndependentDisplayCopy(){
        var old=new Gson().fromJson("{\"scene\":\"ORIGINAL\",\"cards\":70}",MenuBackgroundOptions.class);old.validate();assertEquals(MenuPalette.AURORA,old.palette);assertTrue(old.miniAvatar);assertTrue(old.avatarFollowsMouse);assertEquals(100,old.avatarSize);assertEquals(100,old.textOpacity);
        var copy=old.copy();copy.palette=MenuPalette.CANDY;copy.avatarSize=140;copy.textOpacity=25;copy.cards=10;copy.miniAvatar=false;copy.avatarFollowsMouse=false;assertEquals(MenuPalette.AURORA,old.palette);assertTrue(old.miniAvatar);assertEquals(70,old.cards);assertEquals(100,old.textOpacity);
    }
    @Test void preferencesRoundTripAndKeepCharacterSceneIndependent()throws Exception{
        var options=new MenuBackgroundOptions();options.palette=MenuPalette.MINT;options.miniAvatar=false;options.avatarFollowsMouse=false;options.avatarSize=130;options.textOpacity=85;options.cards=35;options.character.title="Existing scene";
        var store=new MenuBackgroundStore(folder);store.save(options);var saved=store.load();assertEquals(MenuPalette.MINT,saved.palette);assertFalse(saved.miniAvatar);assertFalse(saved.avatarFollowsMouse);assertEquals(130,saved.avatarSize);assertEquals(85,saved.textOpacity);assertEquals(35,saved.cards);assertEquals("Existing scene",saved.character.title);assertEquals(new Gson().toJson(saved),new Gson().toJson(saved.copy()));
    }
    @Test void invalidDisplayRangesAreRejected(){
        var o=new MenuBackgroundOptions();o.textOpacity=24;assertThrows(IllegalArgumentException.class,o::validate);o.textOpacity=25;o.cards=10;o.avatarSize=60;o.validate();o.avatarSize=59;assertThrows(IllegalArgumentException.class,o::validate);o.avatarSize=140;o.validate();o.cards=9;assertThrows(IllegalArgumentException.class,o::validate);o.cards=100;o.palette=null;assertThrows(IllegalArgumentException.class,o::validate);
    }
    @Test void alphaScalingPreservesRgbAndIsMonotone(){
        assertEquals(0x40ABCDEF,MenuDisplay.alpha(0x80ABCDEF,50));assertEquals(0x80ABCDEF,MenuDisplay.alpha(0x80ABCDEF,100));assertEquals(0x00ABCDEF,MenuDisplay.alpha(0x80ABCDEF,-5));assertEquals(0x80ABCDEF,MenuDisplay.alpha(0x80ABCDEF,200));int last=-1;for(int i=0;i<=100;i++){int color=MenuDisplay.alpha(0xFF123456,i);assertEquals(0x123456,color&0xFFFFFF);assertTrue((color>>>24)>=last);last=color>>>24;}
    }
    @Test void characterStaysOnScreenAndOutsideNormalMenuWindows(){
        for(int[] size:new int[][]{{160,100},{320,240},{480,360},{640,480},{960,700},{1280,720},{1920,1080},{3840,2160}})for(int percent:new int[]{60,100,140}){
            var w=MenuWindow.of(size[0],size[1]);double right=w.x()+w.width()*w.scale(),bottom=w.y()+w.height()*w.scale();var b=MenuDisplay.avatar(size[0],size[1],right,bottom,percent);assertTrue(b.x()>=0&&b.y()>=0&&b.width()>0&&b.height()>0);assertTrue(b.right()<=size[0]&&b.bottom()<=size[1]);if(size[0]>=320&&size[1]>=240)assertTrue(b.x()>=right||b.y()>=bottom,"Avatar covered the menu");
        }
    }
    @Test void headAndBodyFollowMouseWithBoundedAnglesAndCanBeFrozen(){
        var b=new MenuDisplay.AvatarBounds(900,590,55,100);var left=MenuDisplay.look(b,0,0,true);var right=MenuDisplay.look(b,2000,2000,true);assertTrue(left.headYaw()>0);assertTrue(right.headYaw()<0);assertTrue(left.pitch()<0);assertTrue(right.pitch()>0);
        for(double x:new double[]{-1000000,0,500,1000000})for(double y:new double[]{-1000000,0,500,1000000}){var look=MenuDisplay.look(b,x,y,true);assertTrue(Math.abs(look.bodyYaw()-180)<17);assertTrue(Math.abs(look.headYaw())<31);assertTrue(Math.abs(look.pitch())<32);}
        assertEquals(new MenuDisplay.Look(180,0,-3),MenuDisplay.look(b,0,0,false));assertEquals(new MenuDisplay.Look(180,0,-3),MenuDisplay.look(b,Double.NaN,0,true));
    }
    @Test void palettesHaveDistinctBrightBackgroundsAndDoNotReplaceSolidColors(){
        var colors=new HashSet<Integer>();for(var palette:MenuPalette.values()){var o=new MenuBackgroundOptions();o.palette=palette;var image=WindowBackdrop.create(32,24,o);int color=image.getRGB(10,8)&0xFFFFFF;colors.add(color);if(palette.colorful()){double light=((color>>16&255)+(color>>8&255)+(color&255))/3d;assertTrue(light>55);}WindowBackdrop.solid(o,0x112233);assertEquals(0x112233,WindowBackdrop.create(10,10,o).getRGB(5,5)&0xFFFFFF);assertEquals(palette,o.palette);}
        assertEquals(MenuPalette.values().length,colors.size());
    }
}
