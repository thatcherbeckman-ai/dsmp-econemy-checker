package dev.dsmp.economy.ui;
import dev.dsmp.economy.EconomyTrackerClient;
import java.util.*;
import java.util.stream.IntStream;
/** Native-free fixture for verifying button placement and click callbacks. */
public final class TrackerScreen extends net.minecraft.class_437 {
    private final EconomyTrackerClient mod;
    private final int tab;
    public record Button(int x, int y, int width, String label, Runnable click) {}
    private final List<Button> buttons = new ArrayList<>();
    public TrackerScreen(EconomyTrackerClient client) { this(client, 0); }
    public TrackerScreen(EconomyTrackerClient client, int tab) { super(net.minecraft.class_2561.method_43470("Editor")); mod = client; this.tab = tab; method_25426(); }
    public List<Button> buttons() { return List.copyOf(buttons); }
    protected void method_25426() {
        button(10, 28, 64, "Layout", () -> {});
        button(78, 28, 64, "Trackers", () -> {});
        button(146, 28, 64, "Widgets", () -> {});
        if (tab == 1) { toggle(10, 54, 94, "Player Nets", 0); toggle(108, 54, 94, "Recent Bets", 1); toggle(206, 54, 94, "Profit", 2); }
    }
    private void toggle(int x, int y, int width, String label, int panel) {
        button(x, y, width, label, () -> { mod.config.panels[panel].visible = !mod.config.panels[panel].visible; method_41843(); });
    }
    private void button(int x, int y, int width, String label, Runnable click) { buttons.add(new Button(x, y, width, label, click)); }
    public void method_41843() { buttons.clear(); method_25426(); }
    public void method_25394(net.minecraft.class_332 g,int mx,int my,float delta) { g.method_25294(0,0,field_22789,field_22790,-301396199); super.method_25394(g,mx,my,delta); }
    public void method_25419() { if(mod.saveConfig())super.method_25419();else super.method_25419(); }
    public boolean warning() { return IntStream.range(0, 3).noneMatch(i -> mod.config.panels[i].visible); }
}
