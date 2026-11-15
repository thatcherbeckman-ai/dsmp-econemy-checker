package dev.dsmp.economy.appearance;

import dev.dsmp.economy.TrackerConfig;
import java.util.*;

/** Pure settings controller shared by the Minecraft screen and headless tests. */
public final class AppearanceEditor {
    public record Control(int x, int y, int width, String label, Runnable action) {}
    public static final String[] NAMES = {"Player Nets", "Recent Bets", "Profit", "Loans", "Rakeback", "Balance"};
    private static final int[] ORDER = {2, 0, 1, 3, 4, 5};
    private static final String[] ROLES = {"Accent", "Background", "Text", "Up / money", "Down / status"};
    private static final int[] PRESETS = {0xD29B39, 0xFF6B9D, 0x5CE4A0, 0x66CCFF, 0xA78BFA, 0xFF5252, 0xFFFFFF, 0x0F1722, 0x000000, 0xFFD166};
    private final TrackerConfig config;
    private final TabAppearance[] original;
    private int selected = 2, colorRole = -1, step = 16;
    private String notice = "Changes preview live. Done or Esc saves.";
    public AppearanceEditor(TrackerConfig config) {
        this.config = config;
        original = Arrays.stream(config.appearances).map(TabAppearance::copy).toArray(TabAppearance[]::new);
    }
    public void cancel() { config.appearances = Arrays.stream(original).map(TabAppearance::copy).toArray(TabAppearance[]::new); }
    public int selected() { return selected; }
    public int colorRole() { return colorRole; }
    public TabAppearance style() { return config.appearance(selected); }
    public String heading() {
        return colorRole < 0 ? "APPEARANCE / " + NAMES[selected]
                : ROLES[colorRole] + " #" + String.format(Locale.ROOT, "%06X", style().color(colorRole));
    }
    public String notice() { return notice; }
    public void failedSave() { notice = "Save failed. Retry Done or use Cancel."; }
    private Control control(int col, int row, String label, Runnable action, Runnable refresh) {
        return new Control(10 + col * 98, 28 + row * 24, 94, label, () -> { action.run(); refresh.run(); });
    }
    public List<Control> controls(Runnable refresh, Runnable done) { return controls(refresh, done, () -> {}); }
    public List<Control> controls(Runnable refresh, Runnable done, Runnable cancel) {
        var controls = new ArrayList<Control>();
        if (colorRole >= 0) {
            int color = style().color(colorRole);
            for (int channel = 0; channel < 3; channel++) {
                int index = channel, value = color >> (16 - channel * 8) & 255;
                String name = new String[]{"R", "G", "B"}[channel];
                controls.add(control(channel, 0, name + " - (" + value + ")", () -> channel(index, -step), refresh));
                controls.add(control(channel, 1, name + " + (" + value + ")", () -> channel(index, step), refresh));
            }
            controls.add(control(0, 2, "Preset color", this::preset, refresh));
            controls.add(control(1, 2, "Step: " + step, () -> step = step == 16 ? 1 : 16, refresh));
            controls.add(control(2, 2, "Reset color", () -> style().color(colorRole, new TabAppearance().color(colorRole)), refresh));
            controls.add(control(0, 3, "Back", () -> colorRole = -1, refresh));
            controls.add(new Control(178, 100, 60, "Done", done));
            controls.add(new Control(242, 100, 60, "Cancel", cancel));
            return List.copyOf(controls);
        }
        controls.add(new Control(10, 28, 164, "Edit: " + NAMES[selected], () -> { next(); refresh.run(); }));
        controls.add(new Control(178, 28, 60, "Done", done));
        controls.add(new Control(242, 28, 60, "Cancel", cancel));
        controls.add(control(0, 1, "Size -", () -> size(-.05f), refresh));
        controls.add(control(1, 1, "Size: " + Math.round(style().size * 100) + "%", () -> style().size = 1, refresh));
        controls.add(control(2, 1, "Size +", () -> size(.05f), refresh));
        for (int role = 0; role < 3; role++) { int index = role; controls.add(control(role, 2, ROLES[role], () -> colorRole = index, refresh)); }
        controls.add(control(0, 3, "Up / money", () -> colorRole = 3, refresh));
        controls.add(control(1, 3, "Down / status", () -> colorRole = 4, refresh));
        controls.add(control(2, 3, "Opacity: " + (style().opacity < 0 ? config.opacity : style().opacity), this::opacity, refresh));
        controls.add(new Control(10, 124, 144, "Design: " + TabAppearance.DESIGNS[style().design], () -> { style().design = (style().design + 1) % TabAppearance.DESIGNS.length; refresh.run(); }));
        controls.add(new Control(158, 124, 142, "Reset " + NAMES[selected], () -> { config.appearances[selected] = new TabAppearance(); refresh.run(); }));
        return List.copyOf(controls);
    }
    private void next() {
        for (int i = 0; i < ORDER.length; i++) if (ORDER[i] == selected) { selected = ORDER[(i + 1) % ORDER.length]; return; }
    }
    private void size(float change) { style().size = Math.clamp(Math.round((style().size + change) * 100) / 100f, .5f, 2f); }
    private void opacity() {
        int current = style().opacity < 0 ? config.opacity : style().opacity;
        style().opacity = current >= 255 ? 40 : Math.min(255, current + 20);
    }
    private void channel(int channel, int change) {
        int shift = 16 - channel * 8, color = style().color(colorRole);
        int value = Math.clamp((color >> shift & 255) + change, 0, 255);
        style().color(colorRole, color & ~(255 << shift) | value << shift);
    }
    private void preset() {
        int color = style().color(colorRole), index = -1;
        for (int i = 0; i < PRESETS.length; i++) if (PRESETS[i] == color) index = i;
        style().color(colorRole, PRESETS[(index + 1) % PRESETS.length]);
    }
    public TrackerConfig previewConfig() {
        var preview = new TrackerConfig();
        preview.scale = config.scale; preview.opacity = config.opacity; preview.appearances = config.appearances;
        preview.loansVisible = selected == 3;
        preview.autoCycle = false; preview.appearancePreview = selected; preview.renderPage = selected < 4 ? selected : 2;
        for (var panel : preview.panels) panel.visible = false;
        preview.panels[0].visible = selected < 4;
        preview.panels[0].x = .5f; preview.panels[0].y = .5f;
        preview.rakebackPanel.visible = selected == 4; preview.balancePanel.visible = selected == 5;
        preview.rakebackPanel.x = preview.balancePanel.x = .5f; preview.rakebackPanel.y = preview.balancePanel.y = .5f;
        return preview;
    }
}
