package dev.dsmp.economy.ui;

import dev.dsmp.economy.core.Ledger;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerNetRowsTest {
    private static List<Ledger.PlayerNet> players(int up, int down) {
        var rows = new ArrayList<Ledger.PlayerNet>();
        for (int i = 1; i <= up; i++) rows.add(new Ledger.PlayerNet("Up" + i, i * 100L));
        for (int i = 1; i <= down; i++) rows.add(new Ledger.PlayerNet("Down" + i, -i * 100L));
        return rows;
    }
    @Test void fullPanelHasSevenFromEachSideAndSelectsLargestTotals() {
        var rows = PlayerNetRows.select(players(20, 20));
        assertEquals(14, rows.size());
        assertEquals(7, rows.stream().filter(p -> p.cents() > 0).count());
        assertEquals(7, rows.stream().filter(p -> p.cents() < 0).count());
        assertEquals(2000, rows.getFirst().cents());
        assertEquals(-2000, rows.getLast().cents());
        assertTrue(rows.stream().allMatch(p -> Math.abs(p.cents()) >= 1400));
    }
    @Test void unequalPoolsShowEqualCounts() {
        var rows = PlayerNetRows.select(players(12, 3));
        assertEquals(6, rows.size());
        assertEquals(3, rows.stream().filter(p -> p.cents() > 0).count());
        assertEquals(3, rows.stream().filter(p -> p.cents() < 0).count());
        assertEquals(1200, rows.getFirst().cents());
        assertEquals(-300, rows.getLast().cents());
        assertEquals(6, PlayerNetRows.select(players(3, 12)).size());
    }
    @Test void hidesZeroNetsIncludingPlayersWhoseBetsCancelOut() {
        var ledger = new Ledger();
        ledger.recordBet(new Ledger.Bet("a", "Even", 100, 1));
        ledger.recordBet(new Ledger.Bet("b", "Even", -100, 2));
        assertFalse(ledger.playerNets().isEmpty());
        assertTrue(PlayerNetRows.forPanel(ledger).isEmpty());
        assertTrue(PlayerNetRows.select(List.of(new Ledger.PlayerNet("Zero", 0))).isEmpty());
        assertTrue(PlayerNetRows.select(List.of()).isEmpty());
    }
    @Test void onlyOneSideShowsItsAvailablePlayersUpToSeven() {
        assertEquals(7, PlayerNetRows.select(players(12, 0)).size());
        assertEquals(4, PlayerNetRows.select(players(0, 4)).size());
        assertTrue(PlayerNetRows.select(players(0, 9)).stream().allMatch(p -> p.cents() < 0));
    }
    @Test void usesDisplayedResetTotalsWithoutChangingFinancialRecords() {
        var ledger = new Ledger();
        ledger.recordBet(new Ledger.Bet("a", "Reset", 1000, 1));
        ledger.recordBet(new Ledger.Bet("b", "Gain", 200, 2));
        ledger.recordBet(new Ledger.Bet("c", "Loss", -300, 3));
        ledger.resetPlayerNet("Reset");
        var before = ledger.snapshot();
        var rows = PlayerNetRows.forPanel(ledger);
        assertEquals(List.of("Gain", "Loss"), rows.stream().map(Ledger.PlayerNet::player).toList());
        assertEquals(before, ledger.snapshot());
        ledger.recordBet(new Ledger.Bet("d", "Reset", -400, 4));
        assertEquals(-400, PlayerNetRows.forPanel(ledger).getLast().cents());
    }
    @Test void sortingIsDeterministicAndInputRemainsIntact() {
        var input = new ArrayList<>(List.of(new Ledger.PlayerNet("Beta", 100), new Ledger.PlayerNet("alpha", 100),
                new Ledger.PlayerNet("Delta", -100), new Ledger.PlayerNet("charlie", -100), new Ledger.PlayerNet("Zero", 0)));
        var before = List.copyOf(input);
        assertEquals(List.of("alpha", "Beta", "charlie", "Delta"), PlayerNetRows.select(input).stream().map(Ledger.PlayerNet::player).toList());
        assertEquals(before, input);
        assertThrows(UnsupportedOperationException.class, () -> PlayerNetRows.select(input).clear());
    }
}
