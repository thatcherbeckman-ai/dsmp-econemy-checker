package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.appearance.AppearanceEditor;
import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.core.ViewCycle;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

/** Minecraft 1.21.11 intermediary API; built without packaging the API fixtures. */
public final class AppearanceScreen extends net.minecraft.client.gui.screens.Screen {
    private final EconomyTrackerClient mod;
    private final net.minecraft.client.gui.screens.Screen parent;
    private final AppearanceEditor editor;
    private final Ledger sample = Ledger.demo();
    private final ViewCycle cycle = new ViewCycle();
    private java.util.List<AppearanceEditor.Control> controls = java.util.List.of();
    public AppearanceScreen(EconomyTrackerClient mod, net.minecraft.client.gui.screens.Screen parent) {
        super(net.minecraft.network.chat.Component.literal("Economy Tracker Appearance"));
        this.mod = mod; this.parent = parent; editor = new AppearanceEditor(mod.config);
    }
    @Override protected void init() {
        controls = editor.controls(this::rebuildWidgets, this::onClose, this::cancel);
        for (var control : controls)
            button(control.x(), control.y(), control.width(), control.label(), control.action());
    }
    private void button(int x, int y, int width, String label, Runnable action) {
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.literal(label), button -> action.run())
                .bounds(x, y, width, 20).build());
    }
    @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        FestiveUi.background(graphics, width, height, mod.config);
        graphics.text(font, editor.heading(), 10, 10, 0xFFEAF2F8, true);
        graphics.text(font, editor.notice(), 10, 150, 0xFFB4C1CC, false);
        TrackerRenderer.drawAll(graphics, editor.previewConfig(), sample, cycle, width, height, true, true, false);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        for (var c : controls) FestiveUi.control(graphics,c.x(),c.y(),c.width(),20,c.label(),c.label().equals("Done"),mouseX>=c.x()&&mouseX<c.x()+c.width()&&mouseY>=c.y()&&mouseY<c.y()+20,mod.config.menuAccent);
    }
    @Override public void onClose() {
        if (mod.saveConfig()) { if (parent instanceof DashboardScreen dashboard) dashboard.checkpoint(); net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent); }
        else editor.failedSave();
    }
    private void cancel() {
        editor.cancel();
        net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);
    }
    @Override public boolean isPauseScreen() { return false; }
}
