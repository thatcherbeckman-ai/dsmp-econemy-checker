package dev.dsmp.economy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dsmp.economy.core.Ledger;

/** Headless fixture with the production command/save method descriptors. */
public final class EconomyTrackerClient {
    public final dev.dsmp.economy.core.ViewCycle viewCycle = new dev.dsmp.economy.core.ViewCycle();
    public final dev.dsmp.economy.core.WalletTracker wallet = new dev.dsmp.economy.core.WalletTracker();
    public final Ledger ledger = new Ledger();
    public TrackerConfig config = new TrackerConfig();
    public final LocalStore store;
    private String scope;
    public EconomyTrackerClient() { this(java.nio.file.Path.of(System.getProperty("java.io.tmpdir"), "loan-command-fixture")); }
    public EconomyTrackerClient(java.nio.file.Path dir) { store = new LocalStore(dir); }
    public void openLedger(String account) {
        scope = null;
        if (account == null) return;
        scope = account; active = true;
    }
    public void openMenu() { net.minecraft.class_310.method_1551().method_1507(new dev.dsmp.economy.ui.TrackerScreen(this)); }
    public void openCommandMenu() { net.minecraft.class_310.method_1551().method_1507(new dev.dsmp.economy.ui.TrackerScreen(this)); }
    public void disconnect() { scope = null; active = false; }
    public void auctionTick() {}
    public boolean customFallback(dev.dsmp.economy.core.PaymentDetector.Detection payment) { if (!payment.betChanged()) return true; return false; }
    public String status = "Save failed";
    public boolean active, saveSucceeds = true;
    public int saves;
    public String message;
    public CommandDispatcher<Object> dispatcher() {
        var dispatcher = new CommandDispatcher<Object>();
        var root = LiteralArgumentBuilder.<Object>literal("economy");
        dispatcher.register(root);
        return dispatcher;
    }
    public String help() { return "Commands: /economy rakeback reset NAME | set NAME AMOUNT | show NAME."; }
    public boolean showHud(Object screen) { return !(screen instanceof dev.dsmp.economy.ui.TrackerScreen); }
    public boolean saveConfig() {
        if (!saveSucceeds) return false;
        try { store.saveConfig(config); return true; } catch (java.io.IOException ex) { status = "Save failed"; return false; }
    }
    private int runLocal(Runnable action) {
        if (!active) return 0;
        try { action.run(); return 1; }
        catch (IllegalArgumentException | IllegalStateException | ArithmeticException ex) { message = ex.getMessage(); return 0; }
    }
    private boolean saveLedger() { saves++; return saveSucceeds; }
    private void say(String text) { message = text; }
}
