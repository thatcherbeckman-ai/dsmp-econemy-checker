package dev.dsmp.economy.loans;

import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.core.Money;
import java.nio.file.Path;
import java.util.List;

/** The client opens one isolated file for its active server/account. */
public final class LoanService {
    private static LoanBook book = new LoanBook();
    private static LoanStore store;
    private static String error;
    private static int page;
    private LoanService() {}
    public static void close() { book = new LoanBook(); store = null; error = null; page = 0; }
    public static void open(String scope, Path directory) {
        close();
        if (scope == null) return;
        try { store = new LoanStore(directory, scope); book = store.load(); }
        catch (Exception ex) { error = "Loans unreadable; original file preserved. Fix the loan file and reconnect."; }
    }
    public static boolean hasLoans() { return error == null && !book.rows().isEmpty(); }
    public static int pages() { return Math.max(1, (book.rows().size() + 13) / 14); }
    public static List<Ledger.PlayerNet> rows(Ledger unused) {
        page = Math.min(page, pages() - 1);
        return book.rows().stream().skip(page * 14L).limit(14).map(l -> new Ledger.PlayerNet(l.player(), l.cents())).toList();
    }
    public static String caption() {
        return "Total " + Money.format(book.total(), false) + "  |  Page " + (Math.min(page, pages() - 1) + 1) + "/" + pages();
    }
    public static String toggleLabel(boolean visible) { return "Loans: " + (visible ? "on" : "off"); }
    public static String execute(String operation, String player, String amount) {
        if (error != null) throw new IllegalStateException(error);
        if (store == null) throw new IllegalStateException("Join the configured server first");
        if (operation.equals("page")) {
            int requested = Integer.parseInt(amount);
            if (requested < 1 || requested > pages()) throw new IllegalArgumentException("Loan page must be 1 to " + pages());
            page = requested - 1;
            return listing();
        }
        if (operation.equals("list")) return listing();
        var next = LoanBook.restore(book.snapshot());
        switch (operation) {
            case "set" -> next.set(player, Money.parse(amount));
            case "add" -> next.add(player, Money.parse(amount));
            case "paid" -> next.paid(player, Money.parse(amount));
            case "remove" -> next.remove(player);
            default -> throw new IllegalArgumentException("Unknown loan action");
        }
        try { store.save(next); }
        catch (Exception ex) { throw new IllegalStateException("Loans could not be saved; previous records retained", ex); }
        book = next;
        page = Math.min(page, pages() - 1);
        var remaining = book.rows().stream().filter(l -> l.player().equalsIgnoreCase(player)).findFirst();
        return remaining.map(l -> "Loan for " + l.player() + ": " + Money.full(l.cents(), false) + " outstanding.")
                .orElse("Loan cleared for " + player + ".") + " Saved locally; balance and bets unchanged.";
    }
    private static String listing() {
        if (book.rows().isEmpty()) return "No outstanding loans. Use /economy loan set NAME AMOUNT.";
        var text = new StringBuilder("LOANS | " + caption());
        for (var row : rows(null)) text.append('\n').append(row.player()).append(": ").append(Money.full(row.cents(), false));
        return text.toString();
    }
}
