package dev.dsmp.economy.core;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Pattern;

/** Reads server payment confirmations. It never sends commands or payments. */
public final class PaymentDetector {
    private static final Detection NONE = new Detection(false, null);
    private static final String NAME = "\\.?[A-Za-z0-9_]{1,16}";
    private static final String AMOUNT = "[0-9][0-9,]*(?:\\.[0-9]{1,12})?\\s*[KMBT]?";
    private static final Pattern IN = pattern("^(" + NAME + ")\\s+(?:has\\s+)?(?:paid|payed)\\s+you\\s+\\$\\s*(" + AMOUNT + ")[.!]?$");
    private static final Pattern RECEIVED = pattern("^You\\s+(?:have\\s+)?received\\s+\\$\\s*(" + AMOUNT + ")\\s+from\\s+(" + NAME + ")[.!]?$");
    private static final String TAX = "(?:\\s*\\(\\s*\\+\\s*\\$\\s*(" + AMOUNT + ")\\s+tax\\s*\\))?[.!]?$";
    private static final Pattern OUT = pattern("^You\\s+(?:have\\s+)?(?:paid|payed|sent)\\s+(" + NAME + ")\\s+\\$\\s*(" + AMOUNT + ")" + TAX);
    private static final Pattern OUT_TO = pattern("^You\\s+(?:have\\s+)?(?:paid|payed|sent)\\s+\\$\\s*(" + AMOUNT + ")\\s+to\\s+(" + NAME + ")" + TAX);
    private static final Pattern COMMAND = pattern("^(?:[a-z_]+:)?pay\\s+(" + NAME + ")\\s+(\\$?" + AMOUNT + ")$");
    private static final Pattern PREFIX = pattern("^\\[(?:DonutSMP|Economy|Payments?|Money)\\]\\s*");
    private final ArrayDeque<Intent> intents = new ArrayDeque<>();
    // Only receipts correlated with an observed command can be identified as retries.
    private final ArrayDeque<Confirmation> confirmations = new ArrayDeque<>();

    private static Pattern pattern(String text) { return Pattern.compile(text, Pattern.CASE_INSENSITIVE); }
    static String messageText(String line) {
        String clean = BalanceReader.readable(line).replaceAll("\\s+", " ");
        return BalanceReader.readable(PREFIX.matcher(clean).replaceFirst(""));
    }
    public void clear() { intents.clear(); confirmations.clear(); }
    public boolean observeCommand(String command, long now) {
        if (command == null || command.length() > 128) return false;
        var match = COMMAND.matcher(command.trim().replaceFirst("^/", ""));
        if (!match.matches()) return false;
        try {
            long cents = Money.parse(match.group(2).replaceAll("\\s+", ""));
            if (cents <= 0) return false;
            expire(now);
            if (intents.size() >= 100) intents.removeFirst();
            intents.addLast(new Intent(UUID.randomUUID().toString(), match.group(1), cents, now));
            return true;
        } catch (ArithmeticException | IllegalArgumentException ex) { return false; }
    }
    public boolean receive(String line, Ledger ledger, long now, long waitMs, String localName) {
        return receiveDetailed(line, ledger, now, waitMs, localName, true).betChanged();
    }
    public Detection receiveDetailed(String line, Ledger ledger, long now, long waitMs, String localName, boolean captureBets) {
        if (line == null || line.length() > 512 || waitMs <= 0) return NONE;
        String clean = messageText(line);
        try {
            var incoming = IN.matcher(clean);
            String player, value;
            String taxValue = null;
            boolean received = incoming.matches();
            if (received) {
                player = incoming.group(1); value = incoming.group(2);
            } else {
                var alternate = RECEIVED.matcher(clean);
                received = alternate.matches();
                if (received) { player = alternate.group(2); value = alternate.group(1); }
                else {
                    var outgoing = OUT.matcher(clean);
                    if (outgoing.matches()) { player = outgoing.group(1); value = outgoing.group(2); taxValue = outgoing.group(3); }
                    else {
                        outgoing = OUT_TO.matcher(clean);
                        if (!outgoing.matches()) return NONE;
                        player = outgoing.group(2); value = outgoing.group(1); taxValue = outgoing.group(3);
                    }
                }
            }
            long cents = Money.parse(value.replaceAll("\\s+", ""));
            if (cents <= 0) return NONE;
            if (received) {
                if (player.equalsIgnoreCase(localName)) return NONE;
                boolean changed = captureBets && ledger.receiveStake(UUID.randomUUID().toString(), player, cents, now);
                return new Detection(changed, cents);
            }
            long tax = taxValue == null ? 0 : Money.parse(taxValue.replaceAll("\\s+", ""));
            long debit = Math.addExact(cents, tax);
            expire(now);
            String id = null;
            for (Iterator<Intent> iterator = intents.iterator(); iterator.hasNext();) {
                Intent intent = iterator.next();
                if (!intent.player().equalsIgnoreCase(player) || intent.cents() != cents) continue;
                id = intent.id(); iterator.remove(); break;
            }
            if (id != null) {
                if (confirmations.size() >= 100) confirmations.removeFirst();
                confirmations.addLast(new Confirmation(player, cents, now));
            } else {
                // Protect repeated command confirmations, but allow every independent
                // receipt when another client mod bypassed the command event entirely.
                final String recipient = player;
                if (confirmations.stream().anyMatch(c -> c.player().equalsIgnoreCase(recipient) && c.cents() == cents)) return NONE;
                id = UUID.randomUUID().toString();
            }
            boolean changed = false;
            if (captureBets && !player.equalsIgnoreCase(localName)) {
                changed = ledger.receivePayout(id, player, cents, now, waitMs);
                if (!changed) changed = ledger.recordBet(new Ledger.Bet(id, player, cents, now));
            }
            return new Detection(changed, -debit);
        } catch (ArithmeticException | IllegalArgumentException ex) { return NONE; }
    }
    private void expire(long now) {
        intents.removeIf(i -> now < i.time() || now - i.time() > 30_000);
        confirmations.removeIf(c -> now < c.time() || now - c.time() > 1_000);
    }
    private record Intent(String id, String player, long cents, long time) {}
    private record Confirmation(String player, long cents, long time) {}
    public record Detection(boolean betChanged, Long walletDelta) {}
}
