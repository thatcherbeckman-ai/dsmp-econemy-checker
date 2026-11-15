package dev.dsmp.economy.appearance;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.core.ViewCycle;

/** Render-local colors; effective size is also used by the editor's hit bounds. */
public final class RenderTheme {
    public static final int EDITOR_TOP = 160;
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();
    private static final class Context {
        final TrackerConfig config; TabAppearance style;
        Context(TrackerConfig config) { this.config = config; style = forWidget(config, 0); }
    }
    private RenderTheme() {}
    public static void begin(TrackerConfig config) { CURRENT.set(new Context(config)); }
    public static void end() { CURRENT.remove(); }
    public static ViewCycle.Frame frame(ViewCycle.Frame actual) {
        Context context = CURRENT.get();
        if (context == null) return actual;
        int preview = context.config.appearancePreview;
        ViewCycle.Frame chosen = preview >= 0 && preview < 4 ? new ViewCycle.Frame(preview, -1, 1) : actual;
        if (chosen.page() >= 0) context.config.renderPage = chosen.page();
        context.style = forWidget(context.config, 0);
        return chosen;
    }
    public static TabAppearance forWidget(TrackerConfig config, int widget) {
        int page = config.appearancePreview >= 0 && config.appearancePreview < 4 ? config.appearancePreview : config.renderPage;
        return config.appearance(widget == 0 ? page : widget == 1 ? 4 : 5);
    }
    public static float scale(TrackerConfig config, int widget) { return config.scale * forWidget(config, widget).size; }
    public static void widget(TrackerConfig config, int widget) {
        Context context = CURRENT.get();
        if (context != null) context.style = forWidget(config, widget);
    }
    public static TabAppearance currentStyle() {
        Context context = CURRENT.get(); return context == null ? new TabAppearance() : context.style;
    }
    public static TabAppearance page(int page) {
        Context context = CURRENT.get(); return context == null ? new TabAppearance() : context.config.appearance(page);
    }
    public static boolean isEditor(Object screen) {
        if (screen == null) return false;
        String name = screen.getClass().getName();
        return name.equals("dev.dsmp.economy.ui.TrackerScreen") || name.equals("dev.dsmp.economy.ui.AppearanceScreen") || name.equals("dev.dsmp.economy.ui.TextBoxesScreen");
    }
    public static int color(TabAppearance style, int original) {
        int rgb = original & 0xFFFFFF, alpha = original >>> 24;
        if (rgb == (-2974791 & 0xFFFFFF)) rgb = style.accent;
        else if (rgb == (-1380105 & 0xFFFFFF)) rgb = style.text;
        else if (rgb == (-10688352 & 0xFFFFFF)) rgb = style.positive;
        else if (rgb == (-1416577 & 0xFFFFFF)) rgb = style.negative;
        else if (rgb == 0x0F1722) { rgb = style.background; if (style.opacity >= 0) alpha = style.opacity; }
        else if (rgb == (-8154973 & 0xFFFFFF) && style.text != (-1380105 & 0xFFFFFF)) rgb = mix(style.text, style.background);
        else if (rgb == (-1337699736 & 0xFFFFFF) && style.accent != (-2974791 & 0xFFFFFF)) rgb = mix(style.accent, style.background);
        return alpha << 24 | rgb;
    }
    private static int mix(int a, int b) {
        return (((a >> 16 & 255) + (b >> 16 & 255)) / 2) << 16
                | (((a >> 8 & 255) + (b >> 8 & 255)) / 2) << 8 | ((a & 255) + (b & 255)) / 2;
    }
    public static boolean suppressFill(TabAppearance style, int width, int height) {
        return width >= 224 && height >= 70 && (style.design == TabAppearance.OUTLINE || style.design == TabAppearance.MINIMAL);
    }
    public static boolean suppressBorder(TabAppearance style, int width, int height) {
        return width >= 224 && height >= 70 && style.design == TabAppearance.MINIMAL;
    }
    public static boolean minecraftGlass(TabAppearance style, int width, int height) {
        return style.design == TabAppearance.GLASS && width >= 224 && height >= 70;
    }
    public static boolean square(TabAppearance style) { return style.design == TabAppearance.SQUARE; }
}
