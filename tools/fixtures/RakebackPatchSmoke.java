import dev.dsmp.economy.EconomyTrackerClient;

/** Loads and runs the actual injected bytecode, including its runnable lambda. */
public final class RakebackPatchSmoke {
    public static void main(String[] args) throws Exception {
        var client = new EconomyTrackerClient();
        client.ledger.setRakebackLoss("Alex", 12300);
        var dispatcher = client.dispatcher();
        if (dispatcher.execute("economy rakeback resetall", new Object()) != 0 || client.saves != 0
                || client.ledger.rakeback("Alex").orElseThrow().cents() != 12300) throw new AssertionError("Scope guard bypassed");
        client.active = true;
        if (dispatcher.execute("economy rakeback resetall", new Object()) != 1 || client.saves != 1
                || client.ledger.rakeback("Alex").orElseThrow().cents() != 0
                || !client.message.startsWith("All rakeback reset")) throw new AssertionError("Command did not reset/save/report");
        client.saveSucceeds = false;
        dispatcher.execute("economy rakeback resetall", new Object());
        if (!client.message.equals("Save failed") || client.saves != 2) throw new AssertionError("Save failure hidden");
        if (!client.help().contains("resetall")) throw new AssertionError("Help not updated");
        System.out.println("Command bytecode smoke check passed: scope guard, reset, save, feedback and save-failure reporting");
    }
}
