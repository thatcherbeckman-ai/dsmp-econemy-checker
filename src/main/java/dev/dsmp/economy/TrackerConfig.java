/*
 * Decompiled with CFR 0.152.
 */
package dev.dsmp.economy;

import dev.dsmp.economy.core.MessageParser;

public final class TrackerConfig {
    public Panel[] panels = new Panel[]{new Panel(0.02f, 0.1f), new Panel(0.35f, 0.1f), new Panel(0.68f, 0.1f)};
    public Panel rakebackPanel = new Panel(0.98f, 0.1f);
    public Panel balancePanel = new Panel(0.02f, 0.98f);
    public float scale = 0.6f;
    public int opacity = 190;
    public boolean autoCycle = true;
    public boolean loansVisible = true;
    public boolean trackingEnabled = false;
    public boolean scoreboardBalance = true;
    public boolean autoDetectPayments = true;
    public int paymentSettlementSeconds = 60;
    public String serverAddress = "donutsmp.net";
    public String betPattern = "";
    public String balancePattern = "";

    public boolean[] enabledViews() {
        return new boolean[]{this.panels[0].visible, this.panels[1].visible, this.panels[2].visible, this.loansVisible && dev.dsmp.economy.loans.LoanService.hasLoans()};
    }

    public Panel box(int index) {
        return switch (index) {
            case 0 -> this.panels[0];
            case 1 -> this.rakebackPanel;
            case 2 -> this.balancePanel;
            default -> throw new IllegalArgumentException("Unknown HUD box");
        };
    }

    public boolean boxVisible(int index) {
        return index == 0 ? this.panels[0].visible || this.panels[1].visible || this.panels[2].visible || this.loansVisible && dev.dsmp.economy.loans.LoanService.hasLoans() : this.box((int)index).visible;
    }

    public void validate() {
        if (this.panels == null || this.panels.length != 3 || !Float.isFinite(this.scale) || this.scale < 0.4f || this.scale > 1.5f || this.opacity < 40 || this.opacity > 240 || this.serverAddress == null || this.serverAddress.isBlank() || this.paymentSettlementSeconds < 5 || this.paymentSettlementSeconds > 600) {
            throw new IllegalArgumentException("Invalid tracker settings");
        }
        for (Panel panel : this.panels) {
            if (!(panel == null || !Float.isFinite(panel.x) || !Float.isFinite(panel.y) || panel.x < 0.0f || panel.x > 1.0f || panel.y < 0.0f) && !(panel.y > 1.0f)) continue;
            throw new IllegalArgumentException("Invalid panel position");
        }
        for (Panel panel : new Panel[]{this.rakebackPanel, this.balancePanel}) {
            if (!(panel == null || !Float.isFinite(panel.x) || !Float.isFinite(panel.y) || panel.x < 0.0f || panel.x > 1.0f || panel.y < 0.0f) && !(panel.y > 1.0f)) continue;
            throw new IllegalArgumentException("Invalid auxiliary panel position");
        }
        new MessageParser(this.betPattern, this.balancePattern);
        if (this.trackingEnabled && (this.betPattern == null || this.betPattern.isBlank()) && (this.balancePattern == null || this.balancePattern.isBlank())) {
            throw new IllegalArgumentException("Configure a verified message pattern before enabling tracking");
        }
    }

    public static final class Panel {
        public boolean visible = true;
        public float x;
        public float y;

        public Panel(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}

