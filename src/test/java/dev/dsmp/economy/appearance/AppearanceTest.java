package dev.dsmp.economy.appearance;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.LocalStore;
import dev.dsmp.economy.core.ViewCycle;
import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AppearanceTest {
    @AfterEach void cleanup() { RenderTheme.end(); }
    private static void click(AppearanceEditor editor, String label) {
        editor.controls(() -> {}, () -> {}).stream().filter(c -> c.label().equals(label)).findFirst().orElseThrow().action().run();
    }
    private static void editNext(AppearanceEditor editor) { editor.controls(() -> {}, () -> {}).getFirst().action().run(); }
    private static void select(AppearanceEditor editor, int page) { for (int i=0; i<6 && editor.selected()!=page; i++) editNext(editor); assertEquals(page,editor.selected()); }
    @Test void oldSettingsLoadWithIndependentDefaultStylesAndUnchangedLayout() {
        var config = new Gson().fromJson("{\"scale\":0.8,\"opacity\":155,\"autoCycle\":false}",TrackerConfig.class); config.validate();
        assertEquals(.8f,config.scale); assertEquals(155,config.opacity); assertFalse(config.autoCycle);
        assertEquals(6,config.appearances.length);
        for (int i=0;i<6;i++) { assertEquals(1,config.appearance(i).size); if(i>0) assertNotSame(config.appearance(0),config.appearance(i)); }
        var style = config.appearance(2);
        for (int color : new int[]{-2974791,-1380105,-10688352,-1416577,-8154973,0xBE0F1722,-1337699736}) assertEquals(color,RenderTheme.color(style,color));
    }
    @Test void allSixStylesPersistAndPreviewStateIsNotSaved(@TempDir Path dir) throws Exception {
        var config = new TrackerConfig();
        for(int i=0;i<6;i++) { var style=config.appearance(i); style.size=.5f+i*.1f; style.accent=0x112200+i; style.background=0x030400+i; style.opacity=80+i*20; style.design=i%4; }
        config.renderPage=3; config.appearancePreview=4;
        var store=new LocalStore(dir); store.saveConfig(config); var loaded=store.loadConfig();
        for(int i=0;i<6;i++) { assertEquals(config.appearance(i).size,loaded.appearance(i).size); assertEquals(config.appearance(i).accent,loaded.appearance(i).accent); assertEquals(config.appearance(i).background,loaded.appearance(i).background); assertEquals(config.appearance(i).opacity,loaded.appearance(i).opacity); assertEquals(config.appearance(i).design,loaded.appearance(i).design); }
        assertEquals(2,loaded.renderPage); assertEquals(-1,loaded.appearancePreview);
    }
    @Test void cancelRestoresTheOpeningStylesWithoutAliasingOrChangingOtherSettings() {
        var c = new TrackerConfig(); c.appearance(0).accent = 0x123456;
        var editor = new AppearanceEditor(c); c.appearance(0).accent = 0xABCDEF; c.appearance(2).size = 2;
        editor.cancel(); assertEquals(0x123456, c.appearance(0).accent); assertEquals(1, c.appearance(2).size);
        c.appearance(0).accent = 0; editor.cancel(); assertEquals(0x123456, c.appearance(0).accent); assertEquals(.6f, c.scale);
    }
    @Test void corruptAppearanceValuesAreRejected() {
        var c=new TrackerConfig(); c.appearance(0).size=Float.NaN; assertThrows(IllegalArgumentException.class,c::validate);
        c=new TrackerConfig(); c.appearance(1).accent=0xFF123456; assertThrows(IllegalArgumentException.class,c::validate);
        c=new TrackerConfig(); c.appearance(2).design=5; assertThrows(IllegalArgumentException.class,c::validate);
        c=new TrackerConfig(); c.appearance(3).opacity=0; assertThrows(IllegalArgumentException.class,c::validate);
        c=new TrackerConfig(); c.appearances=new TabAppearance[2]; assertThrows(IllegalArgumentException.class,c::validate);
    }
    @Test void editorChangesOnlyTheChosenPanelAndResetPreservesOthers() {
        var c=new TrackerConfig(); var editor=new AppearanceEditor(c);
        click(editor,"Size +"); assertEquals(1.05f,c.appearance(2).size); assertEquals(1,c.appearance(0).size);
        click(editor,"Design: MC Glass"); assertEquals(TabAppearance.OUTLINE,c.appearance(2).design);
        select(editor,0); click(editor,"Size -"); click(editor,"Reset Player Nets");
        assertEquals(1,c.appearance(0).size); assertEquals(1.05f,c.appearance(2).size); assertEquals(TabAppearance.OUTLINE,c.appearance(2).design);
        var seen=new HashSet<Integer>(); for(int i=0;i<6;i++) { seen.add(editor.selected()); editNext(editor); } assertEquals(6,seen.size());
    }
    @Test void rgbPickerSupportsPresetsFineAdjustmentsAndClamping() {
        var c=new TrackerConfig(); var editor=new AppearanceEditor(c); click(editor,"Accent");
        int original=c.appearance(2).accent;
        editor.controls(() -> {},()->{}).stream().filter(b->b.label().startsWith("B -")).findFirst().orElseThrow().action().run();
        assertEquals(Math.max(0,(original&255)-16),c.appearance(2).accent&255);
        click(editor,"Step: 16");
        editor.controls(() -> {},()->{}).stream().filter(b->b.label().startsWith("B +")).findFirst().orElseThrow().action().run();
        assertEquals(Math.max(0,(original&255)-16)+1,c.appearance(2).accent&255);
        click(editor,"Preset color"); assertTrue(editor.heading().contains("#"));
        for(int i=0;i<300;i++) editor.controls(() -> {},()->{}).stream().filter(b->b.label().startsWith("R +")).findFirst().orElseThrow().action().run();
        assertEquals(255,c.appearance(2).accent>>16&255); assertEquals(new TabAppearance().text,c.appearance(2).text);
        click(editor,"Reset color"); assertEquals(original,c.appearance(2).accent); click(editor,"Back"); assertEquals(-1,editor.colorRole());
    }
    @Test void previewSelectsOnePanelWithoutChangingVisibilityOrLayout() {
        var c=new TrackerConfig(); var editor=new AppearanceEditor(c); boolean[] original=c.enabledViews(); float originalX=c.box(0).x;
        for(int i=0;i<6;i++) { select(editor,i); var preview=editor.previewConfig(); assertSame(c.appearances,preview.appearances); assertEquals(i,preview.appearancePreview); assertEquals(i==4,preview.rakebackPanel.visible); assertEquals(i==5,preview.balancePanel.visible); if(i>=4) assertFalse(preview.loansVisible); }
        assertArrayEquals(original,c.enabledViews()); assertEquals(originalX,c.box(0).x); assertEquals(-1,c.appearancePreview);
    }
    @Test void geometryUsesTheCurrentPageAndBothAuxiliaryStyles() {
        var c=new TrackerConfig(); c.appearance(0).size=.5f; c.appearance(2).size=1.5f; c.appearance(4).size=.8f; c.appearance(5).size=1.2f;
        c.renderPage=0; assertEquals(.3f,RenderTheme.scale(c,0),1e-6);
        c.renderPage=2; assertEquals(.9f,RenderTheme.scale(c,0),1e-6);
        assertEquals(.48f,RenderTheme.scale(c,1),1e-6); assertEquals(.72f,RenderTheme.scale(c,2),1e-6);
        RenderTheme.begin(c); RenderTheme.widget(c,1); assertSame(c.appearance(4),RenderTheme.currentStyle()); RenderTheme.end(); assertNotSame(c.appearance(4),RenderTheme.currentStyle());
    }
    @Test void frameAndFadeColorsStayBoundToTheirOwnPages() {
        var c=new TrackerConfig(); c.appearance(0).accent=0x123456; c.appearance(2).accent=0xABCDEF;
        RenderTheme.begin(c); var frame=new ViewCycle.Frame(0,2,.5); assertEquals(frame,RenderTheme.frame(frame));
        assertEquals(0xFF123456,RenderTheme.color(RenderTheme.page(0),-2974791)); assertEquals(0xFFABCDEF,RenderTheme.color(RenderTheme.page(2),-2974791));
        c.appearancePreview=3; assertEquals(new ViewCycle.Frame(3,-1,1),RenderTheme.frame(frame));
    }
    @Test void colorsPreserveAlphaAndDesignsAffectOnlyThePanelShape() {
        var style=new TabAppearance(); style.positive=0x123456; style.negative=0x654321; style.background=0x010203; style.opacity=100;
        assertEquals(0x40123456,RenderTheme.color(style,0x40000000|(-10688352&0xFFFFFF)));
        assertEquals(0xFF654321,RenderTheme.color(style,-1416577)); assertEquals(0x64010203,RenderTheme.color(style,0xBE0F1722));
        style.design=TabAppearance.OUTLINE; assertTrue(RenderTheme.suppressFill(style,224,302)); assertFalse(RenderTheme.suppressFill(style,100,60)); assertFalse(RenderTheme.suppressBorder(style,224,302));
        style.design=TabAppearance.MINIMAL; assertTrue(RenderTheme.suppressBorder(style,224,302)); style.design=TabAppearance.SQUARE; assertTrue(RenderTheme.square(style));
    }
    @Test void controlsFitTheEditorAndSizeChangesAreBounded() {
        var c=new TrackerConfig(); var editor=new AppearanceEditor(c);
        for(int i=0;i<40;i++) click(editor,"Size +"); assertEquals(2,c.appearance(2).size);
        for(int i=0;i<50;i++) click(editor,"Size -"); assertEquals(.5,c.appearance(2).size);
        for(var button:editor.controls(()->{},()->{})) { assertTrue(button.x()+button.width()<=302); assertTrue(button.y()+20<RenderTheme.EDITOR_TOP); }
        click(editor,"Background"); for(var button:editor.controls(()->{},()->{})) { assertTrue(button.x()+button.width()<=302); assertTrue(button.y()+20<RenderTheme.EDITOR_TOP); }
    }
}
