/*
 * Decompiled with CFR 0.152.
 */
package dev.dsmp.economy.core;

import dev.dsmp.economy.core.BalanceReader;
import dev.dsmp.economy.core.BalanceReading;
import dev.dsmp.economy.core.Ledger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WalletTracker {
    private static final Pattern SELF = Pattern.compile("(?i)^(?:You have|Your balance(?: is)?|Your money(?: is)?)\\s*[:=\u00bb]?\\s*(\\$.*?)[.!]?$");
    private BalanceReading lastSidebar;
    private long lastPayment = -1L;
    private long ownQueryUntil;
    private long lastReplyTime = -1L;
    private long unclaimedServerDelta;
    private long serverDeltaTime = -1L;
    private boolean estimated = true;
    private boolean initialCoarse;
    private String lastReply = "No balance reply observed yet";

    public void clear() {
        this.lastSidebar = null;
        this.lastReplyTime = -1L;
        this.lastPayment = -1L;
        this.ownQueryUntil = 0L;
        this.estimated = true;
        this.initialCoarse = false;
        this.lastReply = "No balance reply observed yet";
        this.unclaimedServerDelta = 0L;
        this.serverDeltaTime = -1L;
    }

    public boolean estimated() {
        return this.estimated;
    }

    public String lastReply() {
        return this.lastReply;
    }

    public void manualBalance(long cents, Ledger ledger, long now) {
        ledger.recordBalance(cents, now);
        this.estimated = false;
        this.initialCoarse = false;
        this.lastReplyTime = now;
    }

    public void observeCommand(String command, String localName, long now) {
        if (command == null || command.length() > 128) {
            return;
        }
        String[] parts = command.trim().replaceFirst("^/", "").split("\\s+");
        String name = parts[0].toLowerCase(Locale.ROOT).replaceFirst("^[a-z_]+:", "");
        if (!(name.equals("bal") || name.equals("balance") || name.equals("money"))) {
            return;
        }
        this.ownQueryUntil = parts.length == 1 || parts.length == 2 && parts[1].equalsIgnoreCase(localName) ? now + 10000L : 0L;
    }

    public boolean receiveReply(String line, String localName, Ledger ledger, long now) {
        Matcher named;
        String amount;
        if (line == null || line.length() > 512) {
            return false;
        }
        String clean = PaymentDetector.messageText(line);
        Matcher self = SELF.matcher(clean);
        String string = amount = self.matches() ? self.group(1) : null;
        if (amount == null && (named = Pattern.compile("(?i)^" + Pattern.quote(localName) + "'s balance(?: is)?\\s*[:=\u00bb]?\\s*(\\$.*?)[.!]?$").matcher(clean)).matches()) {
            amount = named.group(1);
        }
        if (amount == null && this.ownQueryUntil > now) {
            this.lastReply = clean;
            Matcher label = Pattern.compile("(?i)^(?:Balance|Money)(?: is)?\\s*[:=\u00bb]?\\s*(\\$.*?)[.!]?$").matcher(clean);
            if (label.matches()) {
                amount = label.group(1);
            } else if (clean.startsWith("$")) {
                amount = clean.replaceFirst("[.!]$", "");
            }
        }
        if (amount == null) {
            return false;
        }
        this.lastReply = clean;
        try {
            boolean changed;
            BalanceReading reading = BalanceReading.parse(amount);
            boolean bl = changed = ledger.balance().isEmpty() || ledger.balance().getAsLong() != reading.cents();
            if (!this.initialCoarse) {
                this.rememberServerMovement(reading, ledger, now);
            }
            if (this.initialCoarse && reading.exact()) {
                ledger.calibrateStartingBalance(reading.cents(), now);
            } else {
                ledger.recordBalance(reading.cents(), now);
            }
            this.estimated = !reading.exact();
            this.lastReplyTime = now;
            this.initialCoarse = false;
            this.ownQueryUntil = 0L;
            return changed;
        }
        catch (ArithmeticException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean payment(long delta, Ledger ledger, long now) {
        this.lastPayment = now;
        this.estimated = true;
        if (ledger.balance().isEmpty()) {
            return false;
        }
        if (this.unclaimedServerDelta == delta && this.serverDeltaTime >= 0L && now >= this.serverDeltaTime && now - this.serverDeltaTime <= 2000L) {
            this.unclaimedServerDelta = 0L;
            return false;
        }
        try {
            long balance = Math.addExact(ledger.balance().getAsLong(), delta);
            if (balance < 0L) {
                return false;
            }
            ledger.recordBalance(balance, now);
            return delta != 0L;
        }
        catch (ArithmeticException ex) {
            return false;
        }
    }

    public boolean sidebar(BalanceReading reading, Ledger ledger, long now) {
        if (ledger.balance().isEmpty()) {
            ledger.recordBalance(reading.cents(), now);
            this.lastSidebar = reading;
            this.estimated = !reading.exact();
            this.initialCoarse = !reading.exact();
            return true;
        }
        long current = ledger.balance().getAsLong();
        if (reading.exact() && reading.cents() == current) {
            this.lastSidebar = reading;
            this.estimated = false;
            return false;
        }
        if (this.lastSidebar == null && this.lastReplyTime >= 0L) {
            this.lastSidebar = reading;
            return false;
        }
        if (reading.equals(this.lastSidebar)) {
            return false;
        }
        if (!reading.exact() && Math.abs(reading.cents() - current) < reading.quantum()) {
            this.lastSidebar = reading;
            return false;
        }
        if (this.lastPayment >= 0L && now - this.lastPayment < 2000L) {
            return false;
        }
        if (this.lastReplyTime >= 0L && now - this.lastReplyTime < 2000L) {
            return false;
        }
        this.lastSidebar = reading;
        this.estimated = !reading.exact();
        this.rememberServerMovement(reading, ledger, now);
        ledger.recordBalance(reading.cents(), now);
        return reading.cents() != current;
    }

    private void rememberServerMovement(BalanceReading reading, Ledger ledger, long now) {
        if (reading.exact() && ledger.balance().isPresent() && reading.cents() != ledger.balance().getAsLong()) {
            this.unclaimedServerDelta = reading.cents() - ledger.balance().getAsLong();
            this.serverDeltaTime = now;
        }
    }
}

