package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.appearance.AppearanceEditor;
import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.core.ViewCycle;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** Minecraft 1.21.11 intermediary API; built without packaging the API fixtures. */
public final class AppearanceScreen extends class_437 {
    private final EconomyTrackerClient mod;
    private final class_437 parent;
    private final AppearanceEditor editor;
    private final Ledger sample = Ledger.demo();
    private final ViewCycle cycle = new ViewCycle();
    private java.util.List<AppearanceEditor.Control> controls = java.util.List.of();
    public AppearanceScreen(EconomyTrackerClient mod, class_437 parent) {
        super(class_2561.method_43470("Economy Tracker Appearance"));
        this.mod = mod; this.parent = parent; editor = new AppearanceEditor(mod.config);
    }
    @Override protected void method_25426() {
        controls = editor.controls(this::method_41843, this::method_25419, this::cancel);
        for (var control : controls)
            button(control.x(), control.y(), control.width(), control.label(), control.action());
    }
    private void button(int x, int y, int width, String label, Runnable action) {
        method_37063((class_364)class_4185.method_46430(class_2561.method_43470(label), button -> action.run())
                .method_46434(x, y, width, 20).method_46431());
    }
    @Override public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        FestiveUi.background(graphics, field_22789, field_22790, mod.config);
        graphics.method_51433(field_22793, editor.heading(), 10, 10, 0xFFEAF2F8, true);
        graphics.method_51433(field_22793, editor.notice(), 10, 150, 0xFFB4C1CC, false);
        TrackerRenderer.drawAll(graphics, editor.previewConfig(), sample, cycle, field_22789, field_22790, true, true, false);
        super.method_25394(graphics, mouseX, mouseY, delta);
        for (var c : controls) FestiveUi.control(graphics,c.x(),c.y(),c.width(),20,c.label(),c.label().equals("Done"),mouseX>=c.x()&&mouseX<c.x()+c.width()&&mouseY>=c.y()&&mouseY<c.y()+20,mod.config.menuAccent);
    }
    @Override public void method_25419() {
        if (mod.saveConfig()) { if (parent instanceof DashboardScreen dashboard) dashboard.checkpoint(); class_310.method_1551().method_1507(parent); }
        else editor.failedSave();
    }
    private void cancel() {
        editor.cancel();
        class_310.method_1551().method_1507(parent);
    }
    @Override public boolean method_25421() { return false; }
}
