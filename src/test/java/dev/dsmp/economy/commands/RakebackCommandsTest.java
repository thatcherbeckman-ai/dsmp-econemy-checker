package dev.dsmp.economy.commands;

import dev.dsmp.economy.LocalStore;
import dev.dsmp.economy.core.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RakebackCommandsTest {
    @Test void realCommandTreeExecutesOnceAndPreservesPerPlayerCommands() throws Exception {
        var root = LiteralArgumentBuilder.<Object>literal("economy");
        var perPlayer = new AtomicInteger();
        root.then(LiteralArgumentBuilder.<Object>literal("rakeback").then(LiteralArgumentBuilder.<Object>literal("reset")
                .then(RequiredArgumentBuilder.<Object, String>argument("player", StringArgumentType.word())
                        .executes(ctx -> perPlayer.incrementAndGet()))));
        var bulk = new AtomicInteger();
        RakebackCommands.attach(root, () -> { bulk.incrementAndGet(); return 1; });
        var dispatcher = new CommandDispatcher<>(); dispatcher.register(root);
        assertEquals(0, bulk.get()); // Registering or typing a partial command cannot reset data.
        assertEquals(1, dispatcher.execute("economy rakeback resetall", new Object()));
        assertEquals(1, bulk.get()); assertEquals(0, perPlayer.get());
        assertEquals(1, dispatcher.execute("economy rakeback reset Alex", new Object()));
        assertEquals(1, bulk.get()); assertEquals(1, perPlayer.get());
        assertTrue(dispatcher.getCompletionSuggestions(dispatcher.parse("economy rakeback res", new Object())).get()
                .getList().stream().anyMatch(s -> s.getText().equals("resetall")));
    }
    @Test void resetsLossesWinsAndZeroRecordsWithoutChangingOtherData() {
        var ledger = new Ledger();
        ledger.receiveStake("a", "Loss", 1000, 1);
        ledger.recordBet(new Ledger.Bet("b", "Winner", 2000, 2));
        ledger.setRakebackLoss("Zero", 0);
        ledger.recordBalance(123400, 3); ledger.recordBalance(123500, 4);
        ledger.resetGraph(); ledger.resetProfit(); ledger.resetPlayerNet("Loss");
        var before = ledger.snapshot();
        assertEquals(3, RakebackReset.all(ledger));
        assertTrue(ledger.snapshot().rakebackNets().stream().allMatch(p -> p.cents() == 0));
        var after = ledger.snapshot();
        assertEquals(before.players(), after.players()); assertEquals(before.bets(), after.bets());
        assertEquals(before.profitPoints(), after.profitPoints()); assertEquals(before.balances(), after.balances());
        assertEquals(before.seenIds(), after.seenIds()); assertEquals(before.moneyIn(), after.moneyIn());
        assertEquals(before.moneyOut(), after.moneyOut()); assertEquals(before.balanceBaseline(), after.balanceBaseline());
        assertEquals(before.paymentBets(), after.paymentBets()); assertEquals(before.displayBaselines(), after.displayBaselines());
        assertEquals(before.graphProfitBaseline(), after.graphProfitBaseline());
        assertEquals(before.profitMoneyInBaseline(), after.profitMoneyInBaseline());
        assertEquals(before.profitMoneyOutBaseline(), after.profitMoneyOutBaseline());
        ledger.recordBet(new Ledger.Bet("c", "Winner", -300, 5));
        assertEquals(300, ledger.rakeback("Winner").orElseThrow().cents()); // Old win no longer offsets new losses.
        ledger.recordBet(new Ledger.Bet("d", "Loss", -400, 6));
        assertEquals(400, ledger.rakeback("Loss").orElseThrow().cents());
    }
    @Test void resetPersistsAndIsScopedToTheActiveAccount(@TempDir Path dir) throws Exception {
        var store = new LocalStore(dir); var active = new Ledger(); var other = new Ledger();
        active.setRakebackLoss("Alex", 1000); other.setRakebackLoss("Alex", 2000);
        store.saveLedger("other-account", other);
        RakebackReset.all(active); store.saveLedger("active-account", active);
        var loaded = store.loadLedger("active-account");
        assertEquals(0, loaded.rakeback("Alex").orElseThrow().cents());
        assertEquals(2000, store.loadLedger("other-account").rakeback("Alex").orElseThrow().cents());
        loaded.recordBet(new Ledger.Bet("new", "Alex", -100, 1));
        assertEquals(100, loaded.rakeback("Alex").orElseThrow().cents());
    }
    @Test void emptyLedgerAndRepeatedResetAreSafe() {
        var ledger = new Ledger(); assertEquals(0, RakebackReset.all(ledger));
        ledger.setRakebackLoss("Alex", 1000); assertEquals(1, RakebackReset.all(ledger));
        var once = ledger.snapshot(); assertEquals(1, RakebackReset.all(ledger)); assertEquals(once, ledger.snapshot());
        assertEquals("Save failed", RakebackCommands.resultMessage(false, "Save failed"));
        assertTrue(RakebackCommands.resultMessage(true, "old status").startsWith("All rakeback reset"));
    }
}
