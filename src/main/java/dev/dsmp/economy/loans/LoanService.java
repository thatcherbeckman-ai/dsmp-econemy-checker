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
    public static boolean hasLoans() { return error == null && (book.loanCount() > 0 || book.noLoanCount() > 0); }
    private static int loanPageSize() { return book.noLoanCount() == 0 ? 14 : 7; }
    private static int noLoanPageSize() { return book.loanCount() == 0 ? 13 : 6; }
    public static int pages() {
        return Math.max(1, Math.max((book.loanCount() + loanPageSize() - 1) / loanPageSize(),
                (book.noLoanCount() + noLoanPageSize() - 1) / noLoanPageSize()));
    }
    public static List<Ledger.PlayerNet> rows(Ledger unused) {
        page = Math.min(page, pages() - 1);
        var rows = new java.util.ArrayList<Ledger.PlayerNet>();
        book.rows().stream().skip(page * (long)loanPageSize()).limit(loanPageSize())
                .map(l -> new Ledger.PlayerNet(l.player(), l.cents())).forEach(rows::add);
        var names = book.noLoanNames().stream().skip(page * (long)noLoanPageSize()).limit(noLoanPageSize()).toList();
        if (!names.isEmpty()) {
            rows.add(new Ledger.PlayerNet("NO LOAN LIST", -1)); // Display-only section label, never a financial entry.
            names.forEach(name -> rows.add(new Ledger.PlayerNet(name, 0)));
        }
        return List.copyOf(rows);
    }
    public static String rowAmount(long cents) { return cents < 0 ? "" : cents == 0 ? "NO LOAN" : Money.format(cents, false); }
    public static int rowNameColor(long cents) { return cents < 0 ? -2974791 : -1380105; }
    public static int rowAmountColor(long cents) { return cents == 0 ? -1416577 : -10688352; }
    public static String caption() {
        return "Total " + Money.format(book.total(), false) + "  |  Page " + (Math.min(page, pages() - 1) + 1) + "/" + pages();
    }
    public static String toggleLabel(boolean visible) { return "Loans: " + (visible ? "on" : "off"); }
    public static String execute(String operation, String player, String amount) {
        if (error != null) throw new IllegalStateException(error);
        if (store == null) throw new IllegalStateException("Join the configured server first");
        if (operation.equals("noloan_list")) return noLoanListing(amount == null ? 1 : Integer.parseInt(amount));
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
            case "noloan_add" -> next.noLoanAdd(player);
            case "noloan_remove" -> next.noLoanRemove(player);
            default -> throw new IllegalArgumentException("Unknown loan action");
        }
        try { store.save(next); }
        catch (Exception ex) { throw new IllegalStateException("Loans could not be saved; previous records retained", ex); }
        book = next;
        page = Math.min(page, pages() - 1);
        if (operation.equals("noloan_add")) {
            page = 0;
            return player + " added to the No Loan list. Existing debt retained; new loans blocked in this tracker.";
        }
        if (operation.equals("noloan_remove")) return player + " removed from the No Loan list.";
        var remaining = book.rows().stream().filter(l -> l.player().equalsIgnoreCase(player)).findFirst();
        return remaining.map(l -> "Loan for " + l.player() + ": " + Money.full(l.cents(), false) + " outstanding.")
                .orElse("Loan cleared for " + player + ".") + " Saved locally; balance and bets unchanged.";
    }
    private static String noLoanListing(int requested) {
        var names = book.noLoanNames();
        int pages = Math.max(1, (names.size() + 13) / 14);
        if (requested < 1 || requested > pages) throw new IllegalArgumentException("No Loan list page must be 1 to " + pages);
        if (names.isEmpty()) return "No Loan list is empty. Use /economy loan noloan add NAME.";
        return "NO LOAN LIST | Page " + requested + "/" + pages + "\n"
                + String.join("\n", names.stream().skip((requested - 1) * 14L).limit(14).toList());
    }
    private static String listing() {
        if (!hasLoans()) return "No outstanding loans. Use /economy loan set NAME AMOUNT.";
        var text = new StringBuilder("LOANS | " + caption());
        for (var row : rows(null)) {
            text.append('\n').append(row.player());
            if (row.cents() >= 0) text.append(": ").append(row.cents() == 0 ? "NO LOAN" : Money.full(row.cents(), false));
        }
        return text.toString();
    }
}
