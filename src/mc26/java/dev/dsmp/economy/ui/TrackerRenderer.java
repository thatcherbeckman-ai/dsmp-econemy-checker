package dev.dsmp.economy.ui;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.appearance.RenderTheme;
import dev.dsmp.economy.appearance.TabAppearance;
import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.core.Money;
import dev.dsmp.economy.core.ViewCycle;
import dev.dsmp.economy.loans.LoanService;
import dev.dsmp.economy.ui.ChartPath;
import dev.dsmp.economy.ui.GlassPane;
import dev.dsmp.economy.ui.HudFont;
import dev.dsmp.economy.ui.PlayerNetRows;
import dev.dsmp.economy.ui.TextHud;
import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public final class TrackerRenderer {
    public static final int WIDTH = 224;
    public static final int HEIGHT = 302;
    public static final int EDITOR_TOP = 160;
    public static final int PINK = -2974791;
    public static final int GREEN = -10688352;
    public static final int RED = -1416577;
    private static final int TEXT = -1380105;
    private static final int MUTED = -8154973;
    private static final Identifier PANEL = Identifier.fromNamespaceAndPath((String)"economy_tracker", (String)"panel_fill");
    private static final Identifier BORDER = Identifier.fromNamespaceAndPath((String)"economy_tracker", (String)"panel_border");

    private TrackerRenderer() {
    }

    public static int boxWidth(int box) {
        return box == 2 ? 280 : 224;
    }

    public static int boxHeight(int box) {
        return box == 1 ? 184 : (box == 2 ? 70 : 302);
    }

    public static Bounds bounds(TrackerConfig config, int width, int height, boolean editing) {
        return TrackerRenderer.bounds(config, 0, width, height, editing);
    }

    public static Bounds bounds(TrackerConfig config, int widget, int width, int height, boolean editing) {
        int top = editing ? 160 : 0;
        int cw = TrackerRenderer.boxWidth(widget);
        int ch = TrackerRenderer.boxHeight(widget);
        float scale = Math.min(RenderTheme.scale(config, widget), Math.max(0.1f, ((float)(height - top) - 12.0f) / (float)ch));
        scale = Math.min(scale, Math.max(0.1f, ((float)width - 12.0f) / (float)cw));
        int w = (int)Math.ceil((float)cw * scale);
        int h = (int)Math.ceil((float)ch * scale);
        TrackerConfig.Panel anchor = config.box(widget);
        return new Bounds(Math.round(anchor.x * (float)Math.max(0, width - w)), top + Math.round(anchor.y * (float)Math.max(0, height - top - h)), w, h, scale);
    }

public static void drawAll(GuiGraphicsExtractor graphics, TrackerConfig config, Ledger ledger2, ViewCycle cycle, int width, int height, boolean editing, boolean demo, boolean estimatedBalance) {
        RenderTheme.begin(config);
        long now = System.nanoTime() / 1000000L;
        boolean[] enabled = config.enabledViews();
        ViewCycle.Frame frame = RenderTheme.frame(cycle.frame(enabled, config.autoCycle&&!dev.dsmp.economy.design.DesignAccess.reduced(), now));
        if(dev.dsmp.economy.design.DesignAccess.reduced())frame=new ViewCycle.Frame(frame.page(),-1,1);
        if (frame.page() >= 0) {
            TrackerRenderer.beginBox(graphics, (TrackerConfig)config, 0, (int)width, (int)height, (boolean)editing);
            if (frame.previous() >= 0) {
                TrackerRenderer.drawView(new Ink(graphics, (float)(1.0 - frame.blend())), (Ledger)ledger2, frame.previous(), (long)config.paymentSettlementSeconds * 1000L);
            }
            TrackerRenderer.drawView(new Ink(graphics, (float)frame.blend()), (Ledger)ledger2, frame.page(), (long)config.paymentSettlementSeconds * 1000L);
            Ink solid = new Ink(graphics, 1.0f);
            long pending = ledger2.pendingCount(System.currentTimeMillis(), (long)config.paymentSettlementSeconds * 1000L);
            if (demo != false || pending > 0L) {
                TrackerRenderer.small(solid, (String)(demo != false ? "SAMPLE PREVIEW" : pending + " PENDING \u2014 TOTALS PROVISIONAL"), 8.0f, 282.0f, 0.65f, -2974791);
            }
            int dotX = 9;
            for (int view : ViewCycle.ORDER) {
                solid.dot(dotX, 293.0f, 1.6f, !enabled[view] ? -13352879 : (view == frame.page() ? -2974791 : -8154973));
                dotX += 7;
            }
            solid.fill(34, 292, 214, 293, -13287344);
            int progress = (int)(180.0 * cycle.progress(enabled, config.autoCycle&&!dev.dsmp.economy.design.DesignAccess.reduced(), now));
            solid.fill(34, 292, 34 + progress, 293, -2974791);
            graphics.pose().popMatrix();
        }
        if (config.rakebackPanel.visible) {
            TrackerRenderer.beginBox(graphics, (TrackerConfig)config, 1, (int)width, (int)height, (boolean)editing);
            TrackerRenderer.rakeback(new Ink(graphics, 1.0f), (Ledger)ledger2, (boolean)demo, (long)config.paymentSettlementSeconds * 1000L);
            graphics.pose().popMatrix();
        }
        if (config.balancePanel.visible) {
            TrackerRenderer.beginBox(graphics, (TrackerConfig)config, 2, (int)width, (int)height, (boolean)editing);
            TrackerRenderer.balance(new Ink(graphics, 1.0f), (Ledger)ledger2, (boolean)demo, estimatedBalance != false && demo == false);
            graphics.pose().popMatrix();
        }
        RenderTheme.end();
        TextHud.draw(graphics, (TrackerConfig)config, (int)width, (int)height, (boolean)editing);
    }

private static void beginBox(GuiGraphicsExtractor graphics,TrackerConfig config,int widget,int width,int height,boolean editing){
        RenderTheme.widget(config,widget);Bounds box=bounds(config,widget,width,height,editing);
        graphics.pose().pushMatrix();graphics.pose().translate(box.x(),box.y());graphics.pose().scale(box.scale());
        Ink ink=new Ink(graphics,1);int w=boxWidth(widget),h=boxHeight(widget);ink.rounded(-1,2,w+2,h+1,0x30000000);ink.rounded(0,0,w,h,config.opacity<<24|0x0F1722);ink.border(0,0,w,h,-1337699736);
    }
    private static void drawView(Ink g,Ledger ledger,int page,long waitMs){
        g.appearance=RenderTheme.page(page);if(g.alpha<=.02f)return;
        if(page==3)loans(g,ledger);else if(page==0)nets(g,ledger);else if(page==1)bets(g,ledger,waitMs);else profit(g,ledger);
    }
    private static void nets(Ink g, Ledger ledger) {
        TrackerRenderer.caps(g, "PLAYER NETS", 8, 10, 0.9f, 1.3f, -2974791);
        TrackerRenderer.small(g, "+ means they're up  \u2022  \u2212 means they're down", 8.0f, 25.0f, 0.7f, -8154973);
        if (PlayerNetRows.forPanel(ledger).isEmpty()) {
            TrackerRenderer.empty(g, "No players up or down yet");
            return;
        }
        int y = 43;
        for (Ledger.PlayerNet net : PlayerNetRows.forPanel(ledger).stream().limit(14L).toList()) {
            TrackerRenderer.text(g, HudFont.fit(net.player(), 112), 8, y, -1380105);
            TrackerRenderer.right(g, Money.format(net.cents(), true), 216, y, TrackerRenderer.color(net.cents()));
            y += 16;
        }
    }

    private static void bets(Ink g, Ledger ledger, long waitMs) {
        TrackerRenderer.caps(g, "RECENT BETS", 8, 10, 0.9f, 1.3f, -2974791);
        TrackerRenderer.small(g, "Results from the player's perspective", 8.0f, 25.0f, 0.7f, -8154973);
        if (ledger.recentBets().isEmpty()) {
            TrackerRenderer.empty(g, "No recorded results yet");
            return;
        }
        int y = 43;
        int i = 0;
        for (Ledger.Bet bet : ledger.recentBets().stream().limit(13L).toList()) {
            boolean won = bet.playerDelta() > 0L;
            boolean waiting = ledger.pending(bet.id(), System.currentTimeMillis(), waitMs);
            int tint = waiting ? -8154973 : (won ? -10688352 : -1416577);
            g.rounded(5, y - 3, 214, 16, i++ % 2 == 0 ? 1612853313 : 807086137);
            g.rounded(8, y - 1, 29, 12, waiting ? 1347969912 : (won ? 1079030661 : 1348152385));
            TrackerRenderer.centered(g, waiting ? "WAIT" : (won ? "WON" : "LOST"), 22.5f, y + 1, 0.7f, tint);
            TrackerRenderer.small(g, HudFont.fit(bet.player(), 77), 42.0f, y, 0.86f, -1380105);
            TrackerRenderer.smallRight(g, Money.format(bet.playerDelta(), false).replace("-", ""), 187.0f, y, 0.88f, tint);
            TrackerRenderer.smallRight(g, TrackerRenderer.age(bet.time()), 217.0f, y + 1, 0.65f, -8154973);
            y += 18;
        }
    }

    private static void profit(Ink g, Ledger ledger) {
        TrackerRenderer.caps(g, "PROFIT", 8, 10, 0.88f, 1.3f, -2974791);
        TrackerRenderer.smallRight(g, (String)(ledger.balance().isPresent() ? "Bal " + Money.format(ledger.balance().getAsLong(), false) : "Bal \u2014"), 216.0f, 11.0f, 0.65f, -8154973);
        TrackerRenderer.hero(g, Money.format(ledger.displayedProfit(), true), 8, 26, 208, 2.25f, TrackerRenderer.color(ledger.displayedProfit()));
        TrackerRenderer.small(g, "In " + Money.format(ledger.displayedMoneyIn(), false) + "   Out " + Money.format(ledger.displayedMoneyOut(), false), 8.0f, 54.0f, 0.88f, -1380105);
        TrackerRenderer.graph(g, ledger.profitPoints());
        List<Ledger.PlayerNet> players = ledger.playerNets();
        List<Ledger.PlayerNet> winners = players.stream().filter(p -> p.cents() > 0L).limit(5L).toList();
        List<Ledger.PlayerNet> losers = players.reversed().stream().filter(p -> p.cents() < 0L).limit(5L).toList();
        TrackerRenderer.leaderboard(g, "TOP WINNERS", winners, 6, -10688352);
        TrackerRenderer.leaderboard(g, "TOP LOSERS", losers, 115, -1416577);
        List<Ledger.Bet> recent = ledger.recentBets();
        if (!recent.isEmpty()) {
            Ledger.Bet bet = recent.getFirst();
            long hostDelta = -bet.playerDelta();
            TrackerRenderer.text(g, hostDelta >= 0L ? "+" : "\u2212", 8, 265, TrackerRenderer.color(hostDelta));
            TrackerRenderer.small(g, HudFont.fit(bet.player(), 118), 20.0f, 266.0f, 0.83f, -1380105);
            TrackerRenderer.smallRight(g, Money.format(hostDelta, true), 216.0f, 266.0f, 0.83f, TrackerRenderer.color(hostDelta));
        }
        if (ledger.hasProfitReset()) {
            TrackerRenderer.small(g, "Graph history has its own reset", 8.0f, 277.0f, 0.6f, -8154973);
        } else if (ledger.graphBaseline() != 0L) {
            TrackerRenderer.small(g, "Chart shows activity since your graph reset", 8.0f, 277.0f, 0.6f, -8154973);
        }
    }

    private static void leaderboard(Ink g, String title, List<Ledger.PlayerNet> players, int x, int accent) {
        int w = 103;
        g.rounded(x, 168, w, 88, -1189468876);
        g.border(x, 168, w, 88, 892288336);
        TrackerRenderer.caps(g, title, x + 6, 176, 0.68f, 0.8f, accent);
        int y = 191;
        for (Ledger.PlayerNet player : players) {
            String amount = Money.format(player.cents(), false).replace("-", "");
            int amountWidth = Math.round((float)HudFont.width(amount) * 0.78f);
            int nameWidth = Math.max(10, Math.round((float)(w - 16 - amountWidth) / 0.82f));
            TrackerRenderer.small(g, HudFont.fit(player.player(), nameWidth), x + 6, y, 0.82f, -1380105);
            TrackerRenderer.smallRight(g, amount, x + w - 6, y, 0.78f, accent);
            y += 12;
        }
        if (players.isEmpty()) {
            TrackerRenderer.small(g, "No records yet", x + 6, y, 0.75f, -8154973);
        }
    }

    private static void graph(Ink g, List<Ledger.Point> points) {
        int left = 8;
        int top = 83;
        int width = 208;
        int height = 60;
        ChartPath.Plot plot = ChartPath.of(points);
        for (int i = 0; i <= 2; ++i) {
            int y = top + i * height / 2;
            for (int x = left; x < left + width; x += 5) {
                g.fill(x, y, Math.min(x + 2, left + width), y + 1, 675236701);
            }
        }
        if (points.isEmpty()) {
            TrackerRenderer.small(g, "Chart appears after recorded bets", left, top + 25, 0.8f, -8154973);
            return;
        }
        double range = Math.max(1.0, plot.max() - plot.min());
        TrackerRenderer.small(g, Money.format((long)plot.max(), true), left, 71.0f, 0.61f, -8154973);
        TrackerRenderer.small(g, Money.format((long)plot.min(), true), left, 149.0f, 0.61f, -8154973);
        double zeroY = (double)(top + height) - (0.0 - plot.min()) / range * (double)height;
        List<ChartPath.Sample> samples = plot.samples();
        int column = -1;
        for (ChartPath.Sample sample : samples) {
            int x = left + (int)(sample.x() * (double)width);
            double y = (double)(top + height) - (sample.cents() - plot.min()) / range * (double)height;
            if (x <= column || x >= left + width) continue;
            int tint = sample.cents() < 0.0 ? -1416577 : -10688352;
            int strong = tint & 0xFFFFFF | 0x26000000;
            int weak = tint & 0xFFFFFF | 0x3000000;
            int y1 = (int)Math.min(y, zeroY);
            int y2 = (int)Math.max(y, zeroY);
            if (y2 > y1) {
                g.gradient(x, y1, x + 1, y2, sample.cents() >= 0.0 ? strong : weak, sample.cents() >= 0.0 ? weak : strong);
            }
            column = x;
        }
        for (int layer = 0; layer < 3; ++layer) {
            float thickness=layer==0?3.8f:layer==1?1.8f:.9f;
            int opacity = layer == 0 ? 0x14000000 : (layer == 1 ? 0x50000000 : -268435456);
            for (int i = 1; i < samples.size(); ++i) {
                ChartPath.Sample a = samples.get(i - 1);
                ChartPath.Sample b = samples.get(i);
                double ay = (double)(top + height) - (a.cents() - plot.min()) / range * (double)height;
                double by = (double)(top + height) - (b.cents() - plot.min()) / range * (double)height;
                int tint = (b.cents() < 0.0 ? -1416577 : -10688352) & 0xFFFFFF;
                g.line((double)left + a.x() * (double)width, ay, (double)left + b.x() * (double)width, by, thickness, tint | opacity);
            }
        }
        ChartPath.Sample last = samples.getLast();
        int tint = last.cents() < 0.0 ? -1416577 : -10688352;
        float lastY = (float)((double)(top + height) - (last.cents() - plot.min()) / range * (double)height);
        g.dot(left + width, lastY, 1.8f, tint);
        String value = Money.format(points.getLast().cents(), true);
        int pillWidth = Math.round((float)HudFont.width(value) * 0.76f) + 12;
        int pillX = 216 - pillWidth;
        int pillY = Math.clamp((long)(Math.round(lastY) - 19), top, top + height - 17);
        g.rounded(pillX, pillY, pillWidth, 14, tint);
        TrackerRenderer.small(g, value, pillX + 6, pillY + 3, 0.76f, -15719139);
        g.dot(183.0f, 152.0f, 1.0f, -2974791);
        TrackerRenderer.smallRight(g, "TRACKED", 216.0f, 149.0f, 0.57f, -8154973);
    }

    private static void rakeback(Ink g, Ledger ledger, boolean demo, long waitMs) {
        boolean pending;
        TrackerRenderer.caps(g, "RAKEBACK", 8, 10, 0.86f, 1.1f, -2974791);
        TrackerRenderer.smallRight(g, "NET LOSS / $1B", 216.0f, 11.0f, 0.65f, -8154973);
        TrackerRenderer.small(g, "Since each player's last reset", 8.0f, 24.0f, 0.7f, -8154973);
        List<Ledger.PlayerLoss> players = ledger.rakebackPlayers();
        if (players.isEmpty()) {
            TrackerRenderer.small(g, "No rakeback progress recorded", 8.0f, 54.0f, 0.85f, -1380105);
            TrackerRenderer.small(g, "/economy rakeback set NAME 500M", 8.0f, 69.0f, 0.7f, -8154973);
        }
        int y = 40;
        int i = 0;
        for (Ledger.PlayerLoss player : players.stream().limit(6L).toList()) {
            g.rounded(5, y - 3, 214, 20, i++ % 2 == 0 ? 1159802174 : 622536246);
            TrackerRenderer.small(g, HudFont.fit(player.player(), 110), 9.0f, y, 0.9f, player.eligible() ? -10688352 : -1380105);
            String amount = Money.format(player.cents(), false).replace(".00", "");
            TrackerRenderer.smallRight(g, amount + " / $1B", 215.0f, y, 0.86f, player.eligible() ? -10688352 : -1380105);
            g.fill(9, y + 12, 215, y + 13, -12892842);
            g.fill(9, y + 12, 9 + (int)(206.0 * player.progress()), y + 13, player.eligible() ? -10688352 : -2974791);
            y += 22;
        }
        boolean bl = pending = ledger.pendingCount(System.currentTimeMillis(), waitMs) > 0L;
        TrackerRenderer.small(g, demo ? "SAMPLE PREVIEW" : (pending ? "Pending stakes may change totals" : "Green = $1B goal reached"), 8.0f, 174.0f, 0.66f, demo || pending ? -2974791 : -8154973);
    }

    private static void balance(Ink g, Ledger ledger, boolean demo, boolean estimated) {
        TrackerRenderer.caps(g, Minecraft.getInstance().getUser().getName().toUpperCase(Locale.ROOT), 9, 9, 0.82f, 1.05f, -2974791);
        if (demo) {
            TrackerRenderer.smallRight(g, "SAMPLE", 271.0f, 10.0f, 0.6f, -8154973);
        }
        String amount = ledger.balance().isPresent() ? (estimated ? "~" : "") + Money.full(ledger.balance().getAsLong(), false) : "Balance unknown";
        TrackerRenderer.hero(g, amount, 8, 24, 264, 1.95f, ledger.balance().isPresent() ? -10688352 : -8154973);
        OptionalLong change = ledger.balanceChange();
        TrackerRenderer.small(g, (String)(change.isPresent() ? "Change " + Money.full(change.getAsLong(), true) : "Waiting for a balance reading"), 9.0f, 52.0f, 0.83f, change.isPresent() ? TrackerRenderer.color(change.getAsLong()) : -8154973);
    }

    private static int color(long cents) {
        return cents < 0L ? -1416577 : (cents > 0L ? -10688352 : -8154973);
    }

    private static void text(Ink g, String value, int x, int y, int tint) {
        g.label(value, x, y, tint, false);
    }

    private static void right(Ink g, String value, int x, int y, int tint) {
        TrackerRenderer.text(g, value, x - HudFont.width(value), y, tint);
    }

    private static void small(Ink g, String value, float x, float y, float scale, int tint) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale);
        TrackerRenderer.text(g, value, 0, 0, tint);
        g.pose().popMatrix();
    }

    private static void smallRight(Ink g, String value, float x, float y, float scale, int tint) {
        TrackerRenderer.small(g, value, x - (float)HudFont.width(value) * scale, y, scale, tint);
    }

    private static void centered(Ink g, String value, float x, float y, float scale, int tint) {
        TrackerRenderer.small(g, value, x - (float)HudFont.width(value) * scale / 2.0f, y, scale, tint);
    }

    private static void hero(Ink g, String value, int x, int y, int width, float maxScale, int tint) {
        float scale = Math.min(maxScale, (float)width / (float)Math.max(1, HudFont.width(value, true)));
        g.pose().pushMatrix();
        g.pose().translate((float)x, (float)y);
        g.pose().scale(scale);
        g.label(value, 0, 0, tint, true);
        g.pose().popMatrix();
    }

    private static void caps(Ink g, String value, int x, int y, float scale, float tracking, int tint) {
        g.pose().pushMatrix();
        g.pose().translate((float)x, (float)y);
        g.pose().scale(scale);
        float offset = 0.0f;
        for (int code : value.codePoints().toArray()) {
            String glyph = new String(Character.toChars(code));
            g.pose().pushMatrix();
            g.pose().translate(offset, 0.0f);
            TrackerRenderer.text(g, glyph, 0, 0, tint);
            g.pose().popMatrix();
            offset += (float)HudFont.width(glyph) + tracking;
        }
        g.pose().popMatrix();
    }

    private static void empty(Ink g, String value) {
        TrackerRenderer.small(g, value, 8.0f, 70.0f, 0.9f, -1380105);
        TrackerRenderer.small(g, "Use /economy to configure", 8.0f, 87.0f, 0.75f, -8154973);
    }

    private static String age(long time) {
        long seconds = Math.max(0L, (System.currentTimeMillis() - time) / 1000L);
        return seconds < 60L ? "now" : (seconds < 3600L ? seconds / 60L + "m" : (seconds < 86400L ? seconds / 3600L + "h" : seconds / 86400L + "d"));
    }

    private static void loans(Ink g, Ledger ledger) {
        TrackerRenderer.caps(g, "LOANS", 8, 10, 0.9f, 1.3f, -2974791);
        TrackerRenderer.small(g, LoanService.caption(), 8.0f, 25.0f, 0.7f, -8154973);
        if (LoanService.rows(ledger).isEmpty()) {
            TrackerRenderer.empty(g, "Add loans with /economy loan set");
            return;
        }
        int y = 43;
        for (Ledger.PlayerNet net : LoanService.rows(ledger).stream().limit(14L).toList()) {
            TrackerRenderer.text(g, HudFont.fit(net.player(), 112), 8, y, LoanService.rowNameColor(net.cents()));
            TrackerRenderer.right(g, LoanService.rowAmount(net.cents()), 216, y, LoanService.rowAmountColor(net.cents()));
            y += 16;
        }
    }

    public record Bounds(int x, int y, int width, int height, float scale) {
        public boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx <= (double)(this.x + this.width) && my >= (double)this.y && my <= (double)(this.y + this.height);
        }
    }

    private static final class Ink {
        private final GuiGraphicsExtractor graphics;
        private final float alpha;
        private TabAppearance appearance;

        private Ink(GuiGraphicsExtractor graphics, float alpha) {
            this.graphics = graphics;
            this.alpha = alpha;
            this.appearance = RenderTheme.currentStyle();
        }

        private int fade(int n) {
            int color = RenderTheme.color(this.appearance, n);
            return color & 0xFFFFFF | Math.round((float)(color >>> 24) * this.alpha) << 24;
        }

        private Matrix3x2fStack pose() {
            return this.graphics.pose();
        }

        private void fill(int x1, int y1, int x2, int y2, int color) {
            if (this.alpha > 0.02f) {
                this.graphics.fill(x1, y1, x2, y2, this.fade(color));
            }
        }

        private void gradient(int x1, int y1, int x2, int y2, int top, int bottom) {
            if (this.alpha > 0.02f) {
                this.graphics.fillGradient(x1, y1, x2, y2, this.fade(top), this.fade(bottom));
            }
        }

    private void rounded(int n, int n2, int n3, int n4, int n5) {
            if (RenderTheme.minecraftGlass(this.appearance, n3, n4)) {
                GlassPane.fill(this.graphics, n, n2, n3, n4, this.fade(n5));
                return;
            }
            if (RenderTheme.suppressFill(this.appearance, n3, n4)) {
                return;
            }
            if (RenderTheme.square(this.appearance)) {
                this.fill(n, n2, n + n3, n2 + n4, n5);
                return;
            }
            if (this.alpha > 0.02f) {
                this.graphics.blitSprite(RenderPipelines.GUI_TEXTURED,PANEL,n,n2,n3,n4,this.fade(n5));
            }
        }

    private void border(int n, int n2, int n3, int n4, int n5) {
            if (RenderTheme.minecraftGlass(this.appearance, n3, n4)) {
                GlassPane.border(this.graphics, n, n2, n3, n4, this.fade(n5));
                return;
            }
            if (RenderTheme.suppressBorder(this.appearance, n3, n4)) {
                return;
            }
            if (RenderTheme.square(this.appearance)) {
                this.rectBorder(n, n2, n3, n4, n5);
                return;
            }
            if (this.alpha > 0.02f) {
                this.graphics.blitSprite(RenderPipelines.GUI_TEXTURED,BORDER,n,n2,n3,n4,this.fade(n5));
            }
        }

        private void dot(float x, float y, float radius, int color) {
            this.pose().pushMatrix();
            this.pose().translate(x - radius, y - radius);
            this.pose().scale(radius * 2.0f / 16.0f);
            this.rounded(0, 0, 16, 16, color);
            this.pose().popMatrix();
        }

        private void line(double x1, double y1, double x2, double y2, float width, int color) {
            double length = Math.hypot(x2 - x1, y2 - y1);
            if (length <= 1.0E-5 || this.alpha <= 0.02f) {
                return;
            }
            this.pose().pushMatrix();
            this.pose().translate((float)x1, (float)y1);
            this.pose().rotate((float)Math.atan2(y2 - y1, x2 - x1));
            this.pose().translate(0.0f, -width / 2.0f);
            this.pose().scale((float)length, width);
            this.fill(0, 0, 1, 1, color);
            this.pose().popMatrix();
        }

        private void label(String text, int x, int y, int color, boolean display) {
            if (this.alpha > 0.02f) {
                this.graphics.text(Minecraft.getInstance().font, HudFont.text(text, display), x, y, this.fade(color), false);
            }
        }

        private void rectBorder(int n, int n2, int n3, int n4, int n5) {
            this.fill(n, n2, n + n3, n2 + 1, n5);
            this.fill(n, n2 + n4 - 1, n + n3, n2 + n4, n5);
            this.fill(n, n2, n + 1, n2 + n4, n5);
            this.fill(n + n3 - 1, n2, n + n3, n2 + n4, n5);
        }
    }
}
