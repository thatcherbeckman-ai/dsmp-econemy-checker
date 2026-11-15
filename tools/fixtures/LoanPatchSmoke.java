import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.ui.*;
import dev.dsmp.economy.loans.LoanService;
import java.nio.file.Files;

/** Executes the same injected command, editor and render bytecode on fixtures. */
public final class LoanPatchSmoke {
    public static void main(String[] args) throws Exception {
        var client = new EconomyTrackerClient(Files.createTempDirectory("loans-integration-"));
        var dispatcher = client.dispatcher();
        if (dispatcher.execute("economy loan set Alex 10M", new Object()) != 0 || LoanService.hasLoans()) throw new AssertionError("Scope guard bypassed");
        client.openLedger("server|account");
        var before = client.ledger.snapshot();
        if (dispatcher.execute("economy loan set Alex 10M", new Object()) != 1) throw new AssertionError(client.message);
        if (!client.message.contains("Alex") || !client.config.enabledViews()[3]) throw new AssertionError("Loan not visible");
        String view = TrackerRenderer.show(3, client.ledger);
        if (!view.contains("LOANS") || !view.contains("Alex") || !view.contains("Total") || view.contains("+$")) throw new AssertionError(view);
        var screen = new TrackerScreen(client, 1);
        if (screen.buttons().stream().filter(b -> b.y() == 54).count() != 4) throw new AssertionError("Missing editor toggle");
        for (var b : screen.buttons().stream().filter(b -> b.y() == 54).toList()) if (b.width() != 70 || b.x() + b.width() > 302) throw new AssertionError("Overlapping layout");
        screen.buttons().stream().filter(b -> b.label().startsWith("Loans:")).findFirst().orElseThrow().click().run();
        if (client.config.loansVisible || client.config.enabledViews()[3]) throw new AssertionError("Cannot hide loans");
        screen.buttons().stream().filter(b -> b.label().startsWith("Loans:")).findFirst().orElseThrow().click().run();
        if (!client.config.loansVisible) throw new AssertionError("Cannot show loans");
        for (var panel : client.config.panels) panel.visible = false;
        if (screen.warning()) throw new AssertionError("Incorrect all-views-off hint");
        client.disconnect(); if (LoanService.hasLoans()) throw new AssertionError("Loan data leaked after disconnect");
        client.openLedger("server|other"); if (LoanService.hasLoans()) throw new AssertionError("Account isolation failed");
        client.openLedger("server|account"); if (!LoanService.hasLoans()) throw new AssertionError("Loan not saved");
        dispatcher.execute("economy loan paid Alex 10M", new Object()); if (LoanService.hasLoans()) throw new AssertionError("Repayment not cleared");
        dispatcher.execute("economy loan noloan add Blocked", new Object());
        if (!LoanService.hasLoans()) throw new AssertionError("No Loan list hidden without debts");
        String noLoanView = TrackerRenderer.show(3, client.ledger);
        if (!noLoanView.contains("NO LOAN LIST") || !noLoanView.contains("BlockedNO LOAN") || noLoanView.contains("Blocked$0")) throw new AssertionError(noLoanView);
        if (dispatcher.execute("economy loan set Blocked 1M", new Object()) != 0) throw new AssertionError("No Loan restriction bypassed");
        client.disconnect(); client.openLedger("server|account");
        if (!TrackerRenderer.show(3, client.ledger).contains("BlockedNO LOAN")) throw new AssertionError("No Loan record not saved");
        dispatcher.execute("economy loan noloan remove Blocked", new Object());
        if (LoanService.hasLoans()) throw new AssertionError("No Loan entry not removed");
        if (!TrackerRenderer.show(0, client.ledger).contains("PLAYER NETS")) throw new AssertionError("Player Nets render changed");
        if (!before.equals(client.ledger.snapshot())) throw new AssertionError("Loan changed financial ledger");
        System.out.println("Loan adapter smoke check passed: scoped commands, persistence, rendering, unsigned amounts, editor layout/toggle and isolation");
    }
}
