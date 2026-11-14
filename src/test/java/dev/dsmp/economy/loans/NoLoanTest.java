package dev.dsmp.economy.loans;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.commands.LoanCommands;
import dev.dsmp.economy.core.Ledger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class NoLoanTest {
    @TempDir Path dir;
    @AfterEach void close() { LoanService.close(); }
    @Test void namesAreValidatedDeduplicatedAndCaseInsensitive() {
        var book = new LoanBook(); book.noLoanAdd("Alex"); book.noLoanAdd("alex"); book.noLoanAdd(".Bedrock");
        assertEquals(2, book.noLoanCount()); assertTrue(book.isNoLoan("ALEX"));
        assertThrows(IllegalArgumentException.class, () -> book.noLoanAdd("../bad"));
        book.noLoanRemove("ALex"); assertFalse(book.isNoLoan("Alex"));
        assertThrows(IllegalArgumentException.class, () -> book.noLoanRemove("Alex"));
        assertEquals(0, book.total());
    }
    @Test void existingDebtCanBeReducedOrRepaidButNotIncreased() {
        var book = new LoanBook(); book.set("Alex", 1000); book.noLoanAdd("Alex");
        assertEquals(1000, book.total());
        assertThrows(IllegalArgumentException.class, () -> book.set("Alex", 1001));
        assertThrows(IllegalArgumentException.class, () -> book.add("alex", 1));
        book.set("alex", 900); book.paid("Alex", 400); assertEquals(500, book.total());
        book.paid("Alex", 500); assertEquals(0, book.total()); assertTrue(book.isNoLoan("Alex"));
        assertThrows(IllegalArgumentException.class, () -> book.set("Alex", 1));
        book.noLoanRemove("Alex"); book.set("Alex", 1); assertEquals(1, book.total());
    }
    @Test void removingAnExistingLoanKeepsTheNoLoanReminder() {
        var book = new LoanBook(); book.set("Alex", 1000); book.noLoanAdd("Alex"); book.remove("alex");
        assertTrue(book.isNoLoan("Alex")); assertEquals(0, book.loanCount());
        var restored = LoanBook.restore(book.snapshot()); assertTrue(restored.isNoLoan("Alex"));
    }
    @Test void legacyFilesMigrateAndTheNewListPersistsPerAccount() throws Exception {
        var store = new LoanStore(dir, "legacy"); store.save(new LoanBook());
        Path file; try (var files = Files.list(dir)) { file = files.findFirst().orElseThrow(); }
        String legacy = "{\"version\":1,\"loans\":[{\"player\":\"Alex\",\"cents\":1000}]}";
        Files.writeString(file, legacy);
        LoanService.open("legacy", dir); assertEquals(1000, LoanService.rows(null).getFirst().cents());
        assertEquals(legacy, Files.readString(file)); // Loading alone leaves the old file intact.
        LoanService.execute("noloan_add", "Alex", null);
        LoanService.open("other", dir); assertFalse(LoanService.hasLoans());
        LoanService.open("legacy", dir);
        assertTrue(LoanService.execute("noloan_list", null, null).contains("Alex"));
        assertThrows(IllegalArgumentException.class, () -> LoanService.execute("add", "Alex", "1"));
        LoanService.execute("paid", "Alex", "10"); assertTrue(LoanService.hasLoans());
        assertTrue(Files.readString(file).contains("\"version\": 2"));
    }
    @Test void malformedListsAndUnknownSchemasAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> LoanBook.restore(new LoanBook.Snapshot(2, List.of(), null)));
        assertThrows(IllegalArgumentException.class, () -> LoanBook.restore(new LoanBook.Snapshot(2, List.of(), List.of("Alex", "alex"))));
        assertThrows(IllegalArgumentException.class, () -> LoanBook.restore(new LoanBook.Snapshot(2, List.of(), List.of("../bad"))));
        assertThrows(IllegalArgumentException.class, () -> LoanBook.restore(new LoanBook.Snapshot(3, List.of(), List.of())));
        var book = new LoanBook(); for (int i = 0; i < 1000; i++) book.noLoanAdd("Player" + i);
        assertThrows(IllegalArgumentException.class, () -> book.noLoanAdd("Overflow")); assertEquals(1000, book.noLoanCount());
    }
    @Test void noLoanOnlyDataKeepsTheTabVisibleWithLabelsInsteadOfZeroMoney() {
        LoanService.open("scope", dir); LoanService.execute("noloan_add", "Alex", null);
        assertTrue(LoanService.hasLoans()); assertTrue(new TrackerConfig().enabledViews()[3]);
        var rows = LoanService.rows(null); assertEquals("NO LOAN LIST", rows.getFirst().player());
        assertEquals("Alex", rows.getLast().player()); assertEquals("", LoanService.rowAmount(-1));
        assertEquals("NO LOAN", LoanService.rowAmount(0)); assertEquals("$10.00", LoanService.rowAmount(1000));
        assertNotEquals(LoanService.rowNameColor(-1), LoanService.rowNameColor(1000));
        assertNotEquals(LoanService.rowAmountColor(0), LoanService.rowAmountColor(1000));
        assertFalse(LoanService.execute("list", null, null).contains("$0.01"));
    }
    @Test void bothSectionsFitAndEveryNameIsAccessibleThroughPagination() {
        LoanService.open("scope", dir);
        for (int i = 0; i < 24; i++) LoanService.execute("set", "Loan" + i, Integer.toString(i + 1));
        for (int i = 0; i < 20; i++) LoanService.execute("noloan_add", "Blocked" + i, null);
        assertEquals(4, LoanService.pages());
        var loanNames = new HashSet<String>(); var blockedNames = new HashSet<String>();
        for (int p = 1; p <= LoanService.pages(); p++) {
            LoanService.execute("page", null, Integer.toString(p)); var rows = LoanService.rows(null);
            assertTrue(rows.size() <= 14);
            for (var row : rows) { if (row.cents() > 0) loanNames.add(row.player()); else if (row.cents() == 0) blockedNames.add(row.player()); }
        }
        assertEquals(24, loanNames.size()); assertEquals(20, blockedNames.size());
        String listPageTwo = LoanService.execute("noloan_list", null, "2"); assertTrue(listPageTwo.contains("Page 2/2"));
        assertThrows(IllegalArgumentException.class, () -> LoanService.execute("noloan_list", null, "3"));
    }
    @Test void realCommandsManageTheListWithoutChangingFinancialRecords() throws Exception {
        LoanService.open("scope", dir); var ledger = Ledger.demo(); var before = ledger.snapshot();
        var root = LiteralArgumentBuilder.<Object>literal("economy");
        LoanCommands.attach(root, (op, player, amount) -> { LoanService.execute(op, player, amount); return 1; });
        var dispatcher = new CommandDispatcher<>(); dispatcher.register(root);
        dispatcher.execute("economy loan noloan add Alex", new Object());
        assertThrows(IllegalArgumentException.class, () -> dispatcher.execute("economy loan set Alex 1M", new Object()));
        dispatcher.execute("economy loan noloan list", new Object()); dispatcher.execute("economy loan noloan list 1", new Object());
        dispatcher.execute("economy loan noloan remove Alex", new Object());
        dispatcher.execute("economy loan set Alex 1M", new Object());
        assertEquals(before, ledger.snapshot());
    }
    @Test void failedNoLoanSaveLeavesThePreviousRecordsIntact() throws Exception {
        Path unavailable = dir.resolve("blocked"); LoanService.open("scope", unavailable); Files.writeString(unavailable, "keep");
        assertThrows(IllegalStateException.class, () -> LoanService.execute("noloan_add", "Alex", null));
        assertFalse(LoanService.hasLoans()); assertTrue(LoanService.execute("noloan_list", null, null).contains("empty"));
        assertEquals("keep", Files.readString(unavailable));
    }
}
