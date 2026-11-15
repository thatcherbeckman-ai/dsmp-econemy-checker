package dev.dsmp.economy.ui;
import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.core.*;
import net.minecraft.class_332;
import net.minecraft.class_327;
/** Native-free geometry/color fixture with the production method descriptors. */
public final class TrackerRenderer {
    public static final int EDITOR_TOP = 128;
    private static final class Ink {
        float alpha; final class_332 graphics; final StringBuilder text = new StringBuilder();
        Ink(class_332 graphics, float alpha) { this.graphics = graphics; this.alpha = alpha; }
        private int fade(int color) { return color & 0xFFFFFF | Math.round((color >>> 24) * alpha) << 24; }
        private void fill(int x1, int y1, int x2, int y2, int color) { graphics.method_25294(x1,y1,x2,y2,fade(color)); }
        private void rounded(int x, int y, int width, int height, int color) { graphics.rounded(x,y,width,height,fade(color)); }
        private void border(int x, int y, int width, int height, int color) { graphics.border(x,y,width,height,fade(color)); }
        private void label(String value, int x, int y, int color) { text.append(value); graphics.method_51433(new class_327(),value,x,y,fade(color),false); }
    }
    public record Bounds(int x, int y, int width, int height, float scale) {}
    public static Bounds bounds(TrackerConfig config, int widget, int width, int height, boolean editing) {
        int top = editing ? 128 : 0, cw = widget == 2 ? 280 : 224, ch = widget == 1 ? 184 : widget == 2 ? 70 : 302;
        float scale = Math.min(config.scale, Math.max(.1f, (height - top - 12f) / ch));
        scale = Math.min(scale, Math.max(.1f, (width - 12f) / cw));
        int w = (int)Math.ceil(cw * scale), h = (int)Math.ceil(ch * scale);
        return new Bounds(Math.round(config.box(widget).x * (width - w)), top + Math.round(config.box(widget).y * (height - top - h)), w,h,scale);
    }
    public static void drawAll(class_332 graphics, TrackerConfig config, Ledger ledger, ViewCycle cycle, int width, int height, boolean editing, boolean demo, boolean estimated) {
        boolean[] enabled = config.enabledViews();
        var frame = cycle.frame(enabled,config.autoCycle,System.nanoTime()/1000000);
        if (frame.page() >= 0) {
            beginBox(graphics,config,0,width,height,editing);
            drawView(new Ink(graphics,1),ledger,frame.page(),60000);
        }
        if (config.rakebackPanel.visible) { beginBox(graphics,config,1,width,height,editing); new Ink(graphics,1).label("RAKEBACK",8,10,-2974791); }
        if (config.balancePanel.visible) { beginBox(graphics,config,2,width,height,editing); new Ink(graphics,1).label("BALANCE",8,10,-2974791); }
    }
    private static void beginBox(class_332 graphics, TrackerConfig config, int widget, int width, int height, boolean editing) {
        var b = bounds(config,widget,width,height,editing); graphics.scales.add(b.scale());
        var ink = new Ink(graphics,1); int w = widget == 2 ? 280 : 224, h = widget == 1 ? 184 : widget == 2 ? 70 : 302;
        ink.rounded(-1,2,w+2,h+1,0x30000000); ink.rounded(0,0,w,h,config.opacity << 24 | 0x0F1722); ink.border(0,0,w,h,-1337699736);
    }
    public static String show(int page, Ledger ledger) { var ink = new Ink(new class_332(),1); drawView(ink,ledger,page,60000); return ink.text.toString(); }
    private static void drawView(Ink g, Ledger ledger, int page, long waitMs) {
        if (g.alpha <= .02f) return;
        if (page == 0) nets(g, ledger); else g.label(page == 1 ? "BETS" : "PROFIT",8,10,-2974791);
    }
    private static void text(Ink g, String value, int x, int y, int color) { g.label(value,x,y,color); }
    private static void right(Ink g, String value, int x, int y, int color) { g.label(value,x,y,color); }
    private static int color(long cents) { return cents < 0 ? -1416577 : -10688352; }
    private static void nets(Ink g, Ledger ledger) {
        g.label("PLAYER NETS",8,10,-2974791);
        g.label("+ means they're up  •  − means they're down",8,25,-8154973);
        if (PlayerNetRows.forPanel(ledger).isEmpty()) { g.label("No players up or down yet",8,70,-1380105); return; }
        int y = 43;
        for (Ledger.PlayerNet row : PlayerNetRows.forPanel(ledger).stream().limit(14).toList()) {
            text(g, row.player(), 8, y, -1380105);
            right(g, Money.format(row.cents(), true), 216, y, color(row.cents())); y += 16;
        }
    }
}
