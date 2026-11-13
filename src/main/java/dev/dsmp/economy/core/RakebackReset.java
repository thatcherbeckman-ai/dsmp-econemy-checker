package dev.dsmp.economy.core;

/** Uses the same zero baseline as each existing per-player rakeback reset. */
public final class RakebackReset {
    private RakebackReset() {}
    public static int all(Ledger ledger) {
        var players = ledger.rakebackPlayers();
        for (var player : players) ledger.resetRakeback(player.player());
        return players.size();
    }
}
