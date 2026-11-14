package dev.dsmp.economy.loans;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.LocalStore;
import dev.dsmp.economy.commands.LoanCommands;
import dev.dsmp.economy.core.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LoanTest {
    @TempDir Path dir;
    @AfterEach void close() { LoanService.close(); }
    @Test void setAndAddUseCaseInsensitiveNamesAndExactMoney() {
        var book = new LoanBook(); book.set("Alex", Money.parse("1.25M")); book.add("alex", Money.parse("250K"));
        assertEquals(1, book.rows().size()); assertEquals(Money.parse("1.5M"), book.total());
        book.set("ALEX", 500); assertEquals(500, book.total());
        book.set(".Bedrock", 100); assertEquals("ALEX", book.rows().getFirst().player());
    }
    @Test void repaymentsAndRemovalClearOnlyTheNamedLoan() {
        var book = new LoanBook(); book.set("Alex", 1000); book.set("Sam", 100);
        book.paid("alex", 400); assertEquals(700, book.total());
        var before = book.snapshot(); assertThrows(IllegalArgumentException.class, () -> book.paid("Alex", 601)); assertEquals(before, book.snapshot());
        book.paid("Alex", 600); assertEquals(1, book.rows().size());
        book.remove("SAM"); assertTrue(book.rows().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> book.remove("Unknown"));
    }
    @Test void invalidNamesAmountsAndOverflowDoNotMutateData() {
        var book = new LoanBook(); book.set("Alex", 100);
        var before = book.snapshot();
        for (String name : new String[]{"", "Name With Space", "../outside"}) assertThrows(IllegalArgumentException.class, () -> book.set(name, 1));
        assertThrows(IllegalArgumentException.class, () -> book.set("Sam", 0));
        assertThrows(IllegalArgumentException.class, () -> book.add("Alex", -1));
        assertThrows(ArithmeticException.class, () -> book.set("Sam", Long.MAX_VALUE));
        assertEquals(before, book.snapshot());
    }
    @Test void snapshotsRejectMalformedOrDuplicateRecords() {
        assertThrows(IllegalArgumentException.class, () -> LoanBook.restore(new LoanBook.Snapshot(2, List.of())));
        assertThrows(IllegalArgumentException.class, () -> LoanBook.restore(new LoanBook.Snapshot(1, List.of(new LoanBook.Loan("Alex", 10), new LoanBook.Loan("alex", 20)))));
        assertThrows(IllegalArgumentException.class, () -> LoanBook.restore(new LoanBook.Snapshot(1, List.of(new LoanBook.Loan("Alex", -10)))));
    }
    @Test void loansPersistPerServerAndAccountAndClearOnDisconnect() throws Exception {
        LoanService.open("server|accountA", dir); LoanService.execute("set", "Alex", "10M");
        LoanService.open("server|accountB", dir); assertFalse(LoanService.hasLoans());
        LoanService.execute("set", "Sam", "1M"); LoanService.open("server|accountA", dir);
        assertEquals("Alex", LoanService.rows(null).getFirst().player());
        LoanService.close(); assertFalse(LoanService.hasLoans());
        assertThrows(IllegalStateException.class, () -> LoanService.execute("set", "NoScope", "10"));
        LoanService.open("server|accountA", dir); LoanService.execute("paid", "Alex", "10M");
        LoanService.open("server|accountA", dir); assertFalse(LoanService.hasLoans());
    }
    @Test void corruptFilesArePreservedAndEditingIsPaused() throws Exception {
        var store = new LoanStore(dir, "scope"); var book = new LoanBook(); book.set("Alex", 100); store.save(book);
        Path file; try (var paths = Files.list(dir)) { file = paths.findFirst().orElseThrow(); }
        Files.writeString(file, "{broken");
        LoanService.open("scope", dir); assertFalse(LoanService.hasLoans());
        assertThrows(IllegalStateException.class, () -> LoanService.execute("set", "Sam", "1M"));
        assertEquals("{broken", Files.readString(file));
    }
    @Test void failedSaveDoesNotChangeDisplayedLoans() throws Exception {
        Path unavailable = dir.resolve("blocked"); LoanService.open("scope", unavailable); Files.writeString(unavailable, "keep");
        assertThrows(IllegalStateException.class, () -> LoanService.execute("set", "Alex", "1M"));
        assertFalse(LoanService.hasLoans()); assertEquals("keep", Files.readString(unavailable));
    }
    @Test void paginationKeepsEveryLoanAccessibleAndDisplaysUnsignedAmounts() {
        LoanService.open("scope", dir);
        for (int i = 1; i <= 30; i++) LoanService.execute("set", "Player" + i, Integer.toString(i));
        assertEquals(3, LoanService.pages()); assertEquals(14, LoanService.rows(null).size());
        assertEquals("Player30", LoanService.rows(null).getFirst().player());
        LoanService.execute("page", null, "3"); assertEquals(2, LoanService.rows(null).size());
        assertTrue(LoanService.caption().contains("3/3"));
        assertThrows(IllegalArgumentException.class, () -> LoanService.execute("page", null, "4"));
        LoanService.execute("remove", "Player30", null);
        assertTrue(LoanService.execute("list", null, null).contains("Player1"));
    }
    @Test void realBrigadierCommandsUpdateLoansAndPreserveTheBetLedger() throws Exception {
        LoanService.open("scope", dir); var ledger = Ledger.demo(); var before = ledger.snapshot();
        var root = LiteralArgumentBuilder.<Object>literal("economy");
        LoanCommands.attach(root, (op, name, amount) -> { LoanService.execute(op, name, amount); return 1; });
        var dispatcher = new CommandDispatcher<>(); dispatcher.register(root);
        dispatcher.execute("economy loan set Alex 10M", new Object());
        dispatcher.execute("economy loan add Alex 2M", new Object());
        dispatcher.execute("economy loan paid Alex 5M", new Object());
        assertEquals(Money.parse("7M"), LoanService.rows(null).getFirst().cents());
        dispatcher.execute("economy loan list", new Object()); dispatcher.execute("economy loan page 1", new Object());
        dispatcher.execute("economy loan remove Alex", new Object()); assertFalse(LoanService.hasLoans());
        assertEquals(before, ledger.snapshot());
    }
    @Test void loansViewAppearsWithEntriesAndConfigMigratesOldSettings() throws Exception {
        var config = new Gson().fromJson("{}", TrackerConfig.class); config.validate();
        assertEquals(4, config.enabledViews().length); assertFalse(config.enabledViews()[3]);
        LoanService.open("scope", dir); LoanService.execute("set", "Alex", "1M"); assertTrue(config.enabledViews()[3]);
        for (var panel : config.panels) panel.visible = false;
        assertTrue(config.boxVisible(0)); assertFalse(LoanUi.viewsOff(true, config));
        config.loansVisible = false; assertFalse(config.boxVisible(0)); assertTrue(LoanUi.viewsOff(true, config));
        var store = new LocalStore(dir.resolve("config")); store.saveConfig(config);
        assertFalse(store.loadConfig().loansVisible);
    }
    @Test void fourViewsKeepEightSecondRotationAndFadeAndSkipHiddenLoans() {
        var cycle = new ViewCycle(); boolean[] enabled = {true, true, true, true};
        assertEquals(2, cycle.current(enabled, true, 0)); assertEquals(0, cycle.current(enabled, true, 8000));
        assertEquals(1, cycle.current(enabled, true, 16000)); assertEquals(3, cycle.current(enabled, true, 24000));
        var fading = cycle.frame(enabled, true, 24200); assertEquals(1, fading.previous()); assertEquals(.5, fading.blend(), 1e-9);
        assertEquals(2, cycle.current(enabled, true, 32000));
        enabled[3] = false; assertEquals(0, cycle.next(enabled, 32001)); assertEquals(1, cycle.next(enabled, 32002));
        assertEquals(2, cycle.next(enabled, 32003));
        Arrays.fill(enabled, false); assertEquals(-1, cycle.current(enabled, true, 40000));
        enabled[3] = true; assertEquals(3, cycle.current(enabled, true, 40001));
    }
}
