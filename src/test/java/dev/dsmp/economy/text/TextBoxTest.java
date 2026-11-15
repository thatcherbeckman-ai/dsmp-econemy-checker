package dev.dsmp.economy.text;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.LocalStore;
import dev.dsmp.economy.appearance.*;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TextBoxTest {
    @Test void legacySettingsStayEmptyAndNewBoxesPersist(@TempDir Path dir)throws Exception {
        var config=new Gson().fromJson("{}",TrackerConfig.class);config.validate();assertTrue(config.textBoxes.isEmpty());
        var editor=new TextBoxEditor(config);var box=editor.add();editor.fields("min: 100m\\nmax: 2b","#49E8D4","55C900");box.font=1;box.align=1;box.gradient=TextBox.DOWN;box.size=2.5f;box.x=.1f;box.y=.8f;
        var store=new LocalStore(dir);store.saveConfig(config);var loaded=store.loadConfig();var saved=loaded.textBoxes.getFirst();
        assertEquals(box.id,saved.id);assertEquals("min: 100m\nmax: 2b",saved.text);assertEquals(2.5f,saved.size);assertEquals(.8f,saved.y);assertEquals(1,saved.font);assertEquals(1,saved.align);assertEquals(0x49E8D4,saved.startColor);assertEquals(1,saved.backgroundStyle);
    }
    @Test void invalidColorsTextAndPositionsAreRejectedWithoutPartialEdits(){
        var c=new TrackerConfig();var e=new TextBoxEditor(c);var b=e.add();String old=b.text;int color=b.startColor;
        assertThrows(IllegalArgumentException.class,()->e.fields("Changed","NOPE","FFFFFF"));assertEquals(old,b.text);assertEquals(color,b.startColor);
        assertThrows(IllegalArgumentException.class,()->e.fields("A".repeat(161),"FFFFFF","000000"));
        assertThrows(IllegalArgumentException.class,()->e.fields("a\\n".repeat(25),"FFFFFF","000000"));
        b.x=Float.NaN;assertThrows(IllegalArgumentException.class,c::validate);b.x=.5f;b.size=5;assertThrows(IllegalArgumentException.class,c::validate);
    }
    @Test void newDeleteSelectAndCancelKeepIndependentCopies(){
        var c=new TrackerConfig();var e=new TextBoxEditor(c);var first=e.add();first.text="First";var second=e.add();second.text="Second";assertNotEquals(first.id,second.id);
        e.select(first.id);assertSame(first,e.selected());e.delete();assertEquals("Second",e.selected().text);
        e.cancel();assertTrue(c.textBoxes.isEmpty());
        c.textBoxes.add(first);var existing=new TextBoxEditor(c);first.text="Changed";existing.cancel();assertEquals("First",c.textBoxes.getFirst().text);assertNotSame(first,c.textBoxes.getFirst());
    }
    @Test void boxLimitsAndDuplicateIdsAreEnforced(){
        var c=new TrackerConfig();var e=new TextBoxEditor(c);for(int i=0;i<32;i++)e.add();assertThrows(IllegalArgumentException.class,e::add);
        c.textBoxes.getLast().id=c.textBoxes.getFirst().id;assertThrows(IllegalArgumentException.class,c::validate);
    }
    @Test void measurementKeepsUnicodeCodepointsAndAccuratePrefixPositions(){
        var b=new TextBox();b.text="W🙂i\nshort";
        var measure=TextLayout.measure(b,s->s.codePoints().map(cp->cp=='W'?10:cp>65535?8:5).sum());
        assertEquals(25,measure.width());assertEquals(3,measure.lines().getFirst().glyphs().size());
        assertEquals(10,measure.lines().getFirst().glyphs().get(1).x());assertEquals(18,measure.lines().getFirst().glyphs().get(2).x());
        b.align=1;assertEquals(TextLayout.PADDING+1,TextLayout.lineX(b,measure,measure.lines().getFirst()));
        b.align=2;assertEquals(TextLayout.PADDING+2,TextLayout.lineX(b,measure,measure.lines().getFirst()));
    }
    @Test void draggingUsesSameBoundsAndClampsToEveryViewportEdge(){
        var b=new TextBox();b.text="Move me";var measured=TextLayout.measure(b,s->s.length()*6);var bounds=TextLayout.bounds(b,measured,640,480,true);
        assertTrue(bounds.contains(bounds.x()+1,bounds.y()+1));
        TextLayout.drag(b,bounds,-100,-100,2,2,640,480,true);assertEquals(0,b.x);assertEquals(0,b.y);
        var top=TextLayout.bounds(b,measured,640,480,true);assertEquals(TextLayout.EDITOR_TOP,top.y());
        TextLayout.drag(b,bounds,9999,9999,0,0,640,480,true);assertEquals(1,b.x);assertEquals(1,b.y);
        var corner=TextLayout.bounds(b,measured,640,480,false);assertEquals(640,corner.x()+corner.width());assertEquals(480,corner.y()+corner.height());
    }
    @Test void longTextScalesToFitWithoutChangingTheSavedSize(){
        var b=new TextBox();b.text="wide ".repeat(20);b.size=4;var m=TextLayout.measure(b,s->s.length()*6);var bounds=TextLayout.bounds(b,m,320,240,true);
        assertTrue(bounds.width()<=320);assertTrue(bounds.height()<=80);assertEquals(4,b.size);assertTrue(bounds.scale()<4);
    }
    @Test void gradientMathPreservesEndpointsAndInterpolatesAllChannels(){
        assertEquals(0xFF49E8D4,TextBox.blend(0x49E8D4,0x55C900,-1));assertEquals(0xFF55C900,TextBox.blend(0x49E8D4,0x55C900,2));
        assertEquals(0xFF808080,TextBox.blend(0,0xFFFFFF,.5));assertEquals(0x112233,TextBox.rgb("#112233"));assertThrows(IllegalArgumentException.class,()->TextBox.rgb("FF"));
    }
    @Test void textStylesAndSizeChangesArePerBoxAndBounded(){
        var c=new TrackerConfig();var e=new TextBoxEditor(c);var a=e.add();a.size=3;var b=e.add();e.preset();
        assertEquals(TextBox.DOWN,b.gradient);assertEquals(0x55C900,b.endColor);assertEquals(TextBox.SOLID,a.gradient);assertEquals(3,a.size);
        for(int i=0;i<100;i++)e.size(.1f);assertEquals(4,b.size);for(int i=0;i<100;i++)e.size(-.1f);assertEquals(.4f,b.size);
    }
    @Test void glassHasPixelEdgesTransparentFillAndTintableColor(){
        var fill=GlassLook.fill(0,0,224,302,0xBE0F1722);var border=GlassLook.border(0,0,224,302,0xB0000000);
        assertTrue(fill.size()>1);assertTrue((fill.getFirst().color()>>>24)<190);assertTrue(border.stream().anyMatch(r->(r.color()&0xFFFFFF)==0xFFFFFF));
        assertNotEquals(fill.getFirst().color(),GlassLook.fill(0,0,224,302,0xBEFF0000).getFirst().color());
        assertTrue(GlassLook.fill(-1,2,226,303,0x30000000).isEmpty());
    }
    @Test void smallGlassBoxesNeverPaintOutsideTheirBounds(){
        for(int w:new int[]{1,3,8,16,224})for(int h:new int[]{1,5,16,70}){
            var rects=new ArrayList<>(GlassLook.fill(0,0,w,h,0x80000000));rects.addAll(GlassLook.border(0,0,w,h,0xB0000000));
            for(var r:rects){assertTrue(r.x1()>=0&&r.y1()>=0);assertTrue(r.x2()<=w&&r.y2()<=h);assertTrue(r.x2()>=r.x1()&&r.y2()>=r.y1());}
        }
    }
    @Test void oldGlassAndSoftGlassAreDistinctValidDesigns(){
        var appearance=new TabAppearance();assertTrue(RenderTheme.minecraftGlass(appearance,224,302));
        appearance.design=TabAppearance.SOFT_GLASS;appearance.validate();assertFalse(RenderTheme.minecraftGlass(appearance,224,302));
    }
}
