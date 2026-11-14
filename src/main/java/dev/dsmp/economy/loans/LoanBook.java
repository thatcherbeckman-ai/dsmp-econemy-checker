package dev.dsmp.economy.loans;

import dev.dsmp.economy.core.PlayerNames;
import java.util.*;

/** Outstanding loans recorded manually, independent of the payment/bet ledger. */
public final class LoanBook {
    public record Loan(String player, long cents) {}
    public record Snapshot(int version, List<Loan> loans) {}
    private final Map<String, Loan> loans = new HashMap<>();
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
    public Snapshot snapshot() { return new Snapshot(1, rows()); }
    public static LoanBook restore(Snapshot state) {
        if (state == null || state.version() != 1 || state.loans() == null || state.loans().size() > 1000)
            throw new IllegalArgumentException("Invalid loan file");
        var book = new LoanBook();
        for (var loan : state.loans()) {
            if (loan == null || book.loans.containsKey(key(loan.player()))) throw new IllegalArgumentException("Duplicate or invalid loan");
            book.set(loan.player(), loan.cents());
        }
        return book;
    }
}
