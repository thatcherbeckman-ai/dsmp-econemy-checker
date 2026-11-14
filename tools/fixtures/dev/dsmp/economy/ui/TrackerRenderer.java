package dev.dsmp.economy.ui;
import dev.dsmp.economy.core.*;
/** Native-free fixture for verifying the generated render adapter. */
public final class TrackerRenderer {
    private static final class Ink { float alpha = 1; final StringBuilder text = new StringBuilder(); }
    public static String show(int page, Ledger ledger) { var ink = new Ink(); drawView(ink, ledger, page, 60000); return ink.text.toString(); }
    private static void drawView(Ink g, Ledger ledger, int page, long waitMs) {
        if (g.alpha <= .02f) return;
        if (page == 0) nets(g, ledger); else g.text.append(page == 1 ? "BETS" : "PROFIT");
    }
    private static void nets(Ink g, Ledger ledger) {
        g.text.append("PLAYER NETS");
        g.text.append("+ means they're up  •  − means they're down");
        if (PlayerNetRows.forPanel(ledger).isEmpty()) { g.text.append("No players up or down yet"); return; }
        for (var row : PlayerNetRows.forPanel(ledger).stream().limit(14).toList())
            g.text.append(row.player()).append(Money.format(row.cents(), true));
    }
}
