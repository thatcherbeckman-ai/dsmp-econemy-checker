package dev.dsmp.economy.ui;

import dev.dsmp.economy.core.Ledger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Display-only selection; ledger totals and reset baselines are never changed. */
public final class PlayerNetRows {
    private static final int PER_SIDE = 7;
    private static final Comparator<Ledger.PlayerNet> DESCENDING = Comparator
            .comparingLong(Ledger.PlayerNet::cents).reversed()
            .thenComparing(Ledger.PlayerNet::player, String.CASE_INSENSITIVE_ORDER);
    private PlayerNetRows() {}

    public static List<Ledger.PlayerNet> forPanel(Ledger ledger) {
        return select(ledger.playerNets());
    }

    public static List<Ledger.PlayerNet> select(List<Ledger.PlayerNet> players) {
        var up = players.stream().filter(p -> p.cents() > 0).sorted(DESCENDING).limit(PER_SIDE).toList();
        var down = players.stream().filter(p -> p.cents() < 0)
                .sorted(Comparator.comparingLong(Ledger.PlayerNet::cents)
                        .thenComparing(Ledger.PlayerNet::player, String.CASE_INSENSITIVE_ORDER))
                .limit(PER_SIDE).toList();
        // Match counts whenever both sides exist. A single-sided ledger still
        // shows its available players rather than appearing empty.
        int count = up.isEmpty() ? down.size() : down.isEmpty() ? up.size() : Math.min(up.size(), down.size());
        var rows = new ArrayList<Ledger.PlayerNet>(count * 2);
        rows.addAll(up.stream().limit(count).toList());
        rows.addAll(down.stream().limit(count).sorted(DESCENDING).toList());
        return List.copyOf(rows);
    }
}
