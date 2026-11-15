import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.ui.*;
import dev.dsmp.economy.core.*;
import dev.dsmp.economy.loans.LoanService;
import net.minecraft.*;
import java.nio.file.Files;

public final class DashboardPatchSmoke {
    private static void click(class_437 screen,String label){screen.widgets.stream().filter(b->b.label.equals(label)).findFirst().orElseThrow(()->new AssertionError(label)).click();}
    public static void main(String[] args)throws Exception{
        var client=new EconomyTrackerClient(Files.createTempDirectory("dashboard-ui-"));client.openLedger("server|account");client.ledger.recordBalance(123400,1);var before=client.ledger.snapshot();
        client.openMenu();if(!(class_310.method_1551().screen instanceof DashboardScreen dashboard)||client.showHud(dashboard))throw new AssertionError("Dashboard entry/gate failed");
        var g=new class_332();dashboard.method_25394(g,0,0,0);if(g.text.stream().noneMatch(s->s.equals("Your economy"))||g.shapes.size()<100)throw new AssertionError("Dashboard/winter scene not rendered");
        click(dashboard,"HUD modules");dashboard.widgets.stream().filter(b->b.label.equals("OFF")).findFirst().orElseThrow().click();if(client.config.balancePanel.visible)throw new AssertionError("Module toggle failed");
        click(dashboard,"Appearance");var appearance=class_310.method_1551().screen;click(appearance,"Size +");click(appearance,"Done");if(class_310.method_1551().screen!=dashboard)throw new AssertionError("Child did not return");
        click(dashboard,"Settings");dashboard.widgets.stream().filter(b->b.label.equals("OFF")).skip(1).findFirst().orElseThrow().click();if(client.config.animateSnow)throw new AssertionError("Snow toggle failed");click(dashboard,"Cancel");
        if(class_310.method_1551().screen!=null||client.config.balancePanel.visible||client.config.appearance(2).size!=1.05f||!client.config.animateSnow)throw new AssertionError("Cancel/checkpoint semantics failed");
        client.openCommandMenu();if(!(class_310.method_1551().screen instanceof DashboardScreen root))throw new AssertionError("Command entry not updated");
        click(root,"Loans");root.fields.get(0).method_1852("Alex");root.fields.get(1).method_1852("10M");root.method_41843();if(!root.fields.get(0).method_1882().equals("Alex")||!root.fields.get(1).method_1882().equals("10M"))throw new AssertionError("Loan draft lost on resize");click(root,"Set");
        if(LoanService.readOnlySnapshot().loans().size()!=1)throw new AssertionError("Loan form failed");click(root,"No loan +");if(!LoanService.readOnlySnapshot().noLoans().contains("Alex"))throw new AssertionError("No Loan form failed");
        click(root,"Cancel");if(LoanService.readOnlySnapshot().loans().isEmpty())throw new AssertionError("Settings Cancel undid a saved loan");
        client.openMenu();root=(DashboardScreen)class_310.method_1551().screen;click(root,"HUD layout");if(!(class_310.method_1551().screen instanceof TrackerScreen layout))throw new AssertionError("Layout navigation failed");layout.method_25419();if(!(class_310.method_1551().screen instanceof DashboardScreen))throw new AssertionError("Layout did not return to dashboard");
        root=(DashboardScreen)class_310.method_1551().screen;client.saveSucceeds=false;click(root,"Save");if(class_310.method_1551().screen!=root)throw new AssertionError("Save failure closed dashboard");click(root,"Cancel");
        if(!before.equals(client.ledger.snapshot()))throw new AssertionError("Settings modified financial ledger");
        System.out.println("Dashboard UI smoke passed: O/command entry, winter render, HUD toggles, child save checkpoints, loan form, layout navigation, cancel and save failure");
    }
}
