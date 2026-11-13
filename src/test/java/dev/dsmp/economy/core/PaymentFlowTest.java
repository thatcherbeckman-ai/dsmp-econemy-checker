package dev.dsmp.economy.core;

import dev.dsmp.economy.LocalStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class PaymentFlowTest {
    private final Ledger ledger = new Ledger();
    private final WalletTracker wallet = new WalletTracker();
    private final PaymentDetector payments = new PaymentDetector();
    private long now = 10_000;
    private PaymentDetector.Detection receive(String message) { return receive(message, true); }
    private PaymentDetector.Detection receive(String message, boolean capture) {
        var result = payments.receiveDetailed(message, ledger, ++now, 60_000, "Host", capture);
        if (result.walletDelta() != null) wallet.payment(result.walletDelta(), ledger, now);
        return result;
    }
    private void seed() { assertTrue(wallet.receiveReply("You have $ 3,078", "Host", ledger, now)); }
    @Test void screenshotTaxedCommandConfirmationAdjustsWalletAndRakeback() {
        seed(); receive("unprayin paid you $ 1");
        payments.observeCommand("pay unprayin 1", now);
        var result = receive("You paid unprayin $ 1 (+$ 1 tax)");
        assertEquals(-200L, result.walletDelta());
        assertEquals(307700L, ledger.balance().orElseThrow());
        assertEquals(0L, ledger.rakeback("unprayin").orElseThrow().cents());
        assertEquals(0L, ledger.playerNets().getFirst().cents());
    }
    @Test void taxedDoublePayoutMatchesStakeAndKeepsFeeOutOfPlayerWinnings() {
        seed(); receive("Alex paid you $10");
        payments.observeCommand("pay Alex 20", now);
        assertTrue(receive("You paid Alex $20 (+$1 tax)").betChanged());
        assertEquals(306700L, ledger.balance().orElseThrow());
        assertEquals(1000L, ledger.playerNets().getFirst().cents());
        assertEquals(1, ledger.recentBets().size());
        assertEquals(0L, ledger.rakeback("Alex").orElseThrow().cents());
    }
    @Test void confirmedTaxedPaymentsWithoutCommandEventAreRead() {
        seed(); assertTrue(receive("You paid unprayin $ 1 (+$ 1 tax)").betChanged());
        assertEquals(307600L, ledger.balance().orElseThrow());
        assertEquals(100L, ledger.playerNets().getFirst().cents());
    }
    @Test void repeatedIndependentShortcutReceiptsAreSeparatePayments() {
        seed(); receive("You paid Alex $ 1 (+$ 1 tax)");
        receive("You paid Alex $ 1 (+$ 1 tax)");
        assertEquals(307400L, ledger.balance().orElseThrow());
        assertEquals(200L, ledger.playerNets().getFirst().cents());
    }
    @Test void observedCommandDuplicateConfirmationIsNotCountedTwice() {
        seed(); payments.observeCommand("pay Alex 1", now);
        receive("You paid Alex $1 (+$1 tax)");
        assertNull(receive("You paid Alex $1 (+$1 tax)").walletDelta());
        payments.observeCommand("pay Alex 1", now);
        assertNotNull(receive("You paid Alex $1 (+$1 tax)").walletDelta());
        assertEquals(307400L, ledger.balance().orElseThrow());
    }
    @Test void incomingScreenshotMessagesIncreaseWalletAndLossProgress() {
        seed(); receive("unprayin paid you $ 1"); receive("2amsnowy paid you $ 1"); receive("unprayin paid you $ 1");
        assertEquals(308100L, ledger.balance().orElseThrow());
        assertEquals(200L, ledger.rakeback("unprayin").orElseThrow().cents());
        assertEquals(100L, ledger.rakeback("2amsnowy").orElseThrow().cents());
    }
    @Test void winningAndLosingAdjustNetLossSinceReset() {
        seed(); receive("Alex paid you $10"); assertEquals(1000, ledger.rakeback("Alex").orElseThrow().cents());
        receive("You paid Alex $20 (+$1 tax)"); assertEquals(0, ledger.rakeback("Alex").orElseThrow().cents());
        ledger.resetRakeback("Alex"); receive("Alex paid you $5");
        assertEquals(500, ledger.rakeback("Alex").orElseThrow().cents());
    }
    @Test void originalUntaxedMessagesStillWork() {
        seed(); receive("Alex payed you $1"); receive("You payed Alex $2");
        assertEquals(307700L, ledger.balance().orElseThrow());
        assertEquals(100, ledger.playerNets().getFirst().cents());
    }
    @Test void alternativeMoneyFirstReceiptAndDecimalTaxWork() {
        seed(); receive("You paid $2 to Alex (+$0.25 tax)");
        assertEquals(307575L, ledger.balance().orElseThrow());
        assertEquals(200, ledger.playerNets().getFirst().cents());
    }
    @Test void knownPrefixColorsAndWhitespaceAreAccepted() {
        seed(); receive("§a[Payments] » You  paid Alex $ 1 (+ $ 1 tax)");
        assertEquals(307600L, ledger.balance().orElseThrow());
        assertTrue(wallet.receiveReply("[Economy] Your balance is $3,076", "Host", ledger, ++now) || !wallet.estimated());
    }
    @Test void nonReceiptsNeverChangeMoney() {
        seed();
        for (String text : new String[]{"<Alex> You paid Alex $1 (+$1 tax)", "Payment failed", "You cannot pay Alex $1", "You paid Alex $1 (tax unknown)", "You paid Alex $0 (+$1 tax)", "You paid Alex $1 (+$-1 tax)", "You paid Alex $1 (+$1 tax) please"})
            assertNull(receive(text).walletDelta(), text);
        assertEquals(307800, ledger.balance().orElseThrow());
        assertTrue(ledger.playerNets().isEmpty());
    }
    @Test void autoBetsPausedStillUpdatesWalletIncludingTax() {
        seed(); assertFalse(receive("You paid Alex $1 (+$1 tax)", false).betChanged());
        receive("Alex paid you $1", false);
        assertEquals(307700, ledger.balance().orElseThrow());
        assertTrue(ledger.rakebackPlayers().isEmpty());
    }
    @Test void unknownBalanceIsNotInventedAndManualBalStillSeedsIt() {
        receive("Alex paid you $1"); assertTrue(ledger.balance().isEmpty());
        wallet.observeCommand("bal", "Host", now);
        assertTrue(wallet.receiveReply("Balance: $100", "Host", ledger, ++now));
        receive("You paid Alex $1 (+$1 tax)");
        assertEquals(9800, ledger.balance().orElseThrow());
    }
    @Test void absoluteUpdateBeforeTaxedReceiptDoesNotDoubleDeduct() {
        seed(); wallet.receiveReply("You have $3076", "Host", ledger, ++now);
        receive("You paid Alex $1 (+$1 tax)"); assertEquals(307600, ledger.balance().orElseThrow());
    }
    @Test void roundedSidebarCannotUndoTaxedPayment() {
        wallet.sidebar(BalanceReading.parse("$1.2B"), ledger, now);
        receive("You paid Alex $1 (+$1 tax)"); now += 5000;
        wallet.sidebar(BalanceReading.parse("$1.2B"), ledger, now);
        assertEquals(Money.parse("1200000000") - 200, ledger.balance().orElseThrow());
    }
    @Test void persistedWalletAndRakebackContinueAfterReload(@TempDir Path dir) throws Exception {
        seed(); receive("Alex paid you $10"); receive("You paid Alex $5 (+$1 tax)");
        var store = new LocalStore(dir); store.saveLedger("server|account", ledger);
        var restored = store.loadLedger("server|account");
        assertEquals(ledger.balance(), restored.balance());
        assertEquals(500, restored.rakeback("Alex").orElseThrow().cents());
        var result = payments.receiveDetailed("Alex paid you $1", restored, ++now, 60_000, "Host", true);
        wallet.payment(result.walletDelta(), restored, now);
        assertEquals(308300, restored.balance().orElseThrow());
        assertEquals(600, restored.rakeback("Alex").orElseThrow().cents());
    }
}
