package dev.dsmp.economy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dsmp.economy.core.Ledger;

/** Headless fixture with the production command/save method descriptors. */
public final class EconomyTrackerClient {
    public final Ledger ledger = new Ledger();
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
    private int runLocal(Runnable action) { if (!active) return 0; action.run(); return 1; }
    private boolean saveLedger() { saves++; return saveSucceeds; }
    private void say(String text) { message = text; }
}
