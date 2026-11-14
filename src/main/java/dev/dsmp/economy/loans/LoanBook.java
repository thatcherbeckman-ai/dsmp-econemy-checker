package dev.dsmp.economy.loans;

import dev.dsmp.economy.core.PlayerNames;
import java.util.*;

/** Outstanding loans recorded manually, independent of the payment/bet ledger. */
public final class LoanBook {
    public record Loan(String player, long cents) {}
    public record Snapshot(int version, List<Loan> loans, List<String> noLoans) {
        public Snapshot(int version, List<Loan> loans) { this(version, loans, null); }
    }
    private final Map<String, Loan> loans = new HashMap<>();
    private final Map<String, String> noLoans = new HashMap<>();
    public int loanCount() { return loans.size(); }
    public int noLoanCount() { return noLoans.size(); }
    public boolean isNoLoan(String name) { return noLoans.containsKey(key(name)); }
    public List<String> noLoanNames() { return noLoans.values().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList(); }
    public void noLoanAdd(String name) {
        String key = key(name);
        if (!noLoans.containsKey(key) && noLoans.size() >= 1000) throw new IllegalArgumentException("No Loan list is full (1000 names)");
        noLoans.put(key, name);
    }
    public void noLoanRemove(String name) {
        if (noLoans.remove(key(name)) == null) throw new IllegalArgumentException("No entry for " + name + " on the No Loan list");
    }
    private static String key(String name) {
        if (!PlayerNames.valid(name)) throw new IllegalArgumentException("Invalid player name");
        return name.toLowerCase(Locale.ROOT);
    }
    public List<Loan> rows() {
        return loans.values().stream().sorted(Comparator.comparingLong(Loan::cents).reversed()
                .thenComparing(Loan::player, String.CASE_INSENSITIVE_ORDER)).toList();
    }
    public long total() {
        long sum = 0;
        for (var loan : loans.values()) sum = Math.addExact(sum, loan.cents());
        return sum;
    }
    public void set(String name, long cents) {
        String key = key(name);
        if (cents <= 0) throw new IllegalArgumentException("Loan amount must be positive; use remove for a cleared loan");
        if (!loans.containsKey(key) && loans.size() >= 1000) throw new IllegalArgumentException("Loan chart is full (1000 players)");
        long old = loans.containsKey(key) ? loans.get(key).cents() : 0;
        if (noLoans.containsKey(key) && cents > old)
            throw new IllegalArgumentException(name + " is on the No Loan list; remove them before adding or increasing a loan");
        Math.addExact(Math.subtractExact(total(), old), cents); // Validate before mutation.
        loans.put(key, new Loan(name, cents));
    }
    public void add(String name, long cents) {
        String key = key(name);
        if (cents <= 0) throw new IllegalArgumentException("Amount must be positive");
        set(name, Math.addExact(loans.containsKey(key) ? loans.get(key).cents() : 0, cents));
    }
    public void paid(String name, long cents) {
        var loan = loans.get(key(name));
        if (loan == null) throw new IllegalArgumentException("No loan recorded for " + name);
        if (cents <= 0 || cents > loan.cents()) throw new IllegalArgumentException("Repayment must be positive and no larger than the outstanding loan");
        if (cents == loan.cents()) loans.remove(key(name));
        else set(loan.player(), loan.cents() - cents);
    }
    public void remove(String name) {
        if (loans.remove(key(name)) == null) throw new IllegalArgumentException("No loan recorded for " + name);
    }
    public Snapshot snapshot() { return new Snapshot(2, rows(), noLoanNames()); }
    public static LoanBook restore(Snapshot state) {
        if (state == null || (state.version() != 1 && state.version() != 2) || state.loans() == null || state.loans().size() > 1000)
            throw new IllegalArgumentException("Invalid loan file");
        var book = new LoanBook();
        for (var loan : state.loans()) {
            if (loan == null || book.loans.containsKey(key(loan.player()))) throw new IllegalArgumentException("Duplicate or invalid loan");
            book.set(loan.player(), loan.cents());
        }
        if (state.version() == 2) {
            if (state.noLoans() == null || state.noLoans().size() > 1000) throw new IllegalArgumentException("Invalid No Loan list");
            for (String name : state.noLoans()) {
                if (book.noLoans.containsKey(key(name))) throw new IllegalArgumentException("Duplicate No Loan name");
                book.noLoanAdd(name);
            }
        }
        return book;
    }
}
