import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.appearance.*;
import dev.dsmp.economy.core.*;
import dev.dsmp.economy.ui.*;
import net.minecraft.*;
import java.nio.file.Files;

/** Executes the real appearance screen and styled renderer adapters without native graphics. */
public final class AppearancePatchSmoke {
    private static void click(class_437 screen, String label) { screen.widgets.stream().filter(b->b.label.equals(label)).findFirst().orElseThrow(()->new AssertionError(label)).click(); }
    public static void main(String[] args) throws Exception {
        var client=new EconomyTrackerClient(Files.createTempDirectory("appearance-ui-"));
        var parent=new TrackerScreen(client,1);
        parent.buttons().stream().filter(b->b.label().equals("Style")).findFirst().orElseThrow().click().run();
        if (!(class_310.method_1551().screen instanceof AppearanceScreen screen)) throw new AssertionError("Style screen not opened");
        if (client.showHud(screen)||client.showHud(parent)||!client.showHud(null)) throw new AssertionError("HUD editor gate incorrect");
        click(screen,"Size +"); if(client.config.appearance(2).size!=1.05f||client.config.appearance(0).size!=1) throw new AssertionError("Size not independent");
        click(screen,"Accent"); click(screen,"Preset color");
        if(client.config.appearance(2).accent==new TabAppearance().accent) throw new AssertionError("Color picker not applied");
        click(screen,"Back"); click(screen,"Design: MC Glass");
        var graphics=new class_332(); screen.method_25394(graphics,0,0,0);
        if(graphics.text.stream().noneMatch(s->s.contains("PROFIT"))||graphics.shapes.stream().noneMatch(s->s.kind().equals("border"))) throw new AssertionError("Preview not rendered");
        click(screen,"Done"); if(class_310.method_1551().screen!=parent) throw new AssertionError("Done did not return");
        var restored=client.store.loadConfig(); if(restored.appearance(2).size!=1.05f||restored.appearance(2).design!=TabAppearance.OUTLINE) throw new AssertionError("Styles not saved");
        var config=client.config; config.rakebackPanel.visible=false; config.balancePanel.visible=false;
        config.appearancePreview=2; config.appearance(2).design=TabAppearance.GLASS; config.appearance(2).size=1.5f; config.appearance(2).accent=0x123456;
        graphics=new class_332(); TrackerRenderer.drawAll(graphics,config,Ledger.demo(),new ViewCycle(),1000,1000,false,true,false);
        if(Math.abs(graphics.scales.getFirst()-.9f)>.00001||graphics.shapes.stream().noneMatch(s->s.color()==0xFF123456)) throw new AssertionError("Geometry/color hooks ineffective");
        config.appearance(2).design=TabAppearance.MINIMAL; graphics=new class_332(); TrackerRenderer.drawAll(graphics,config,Ledger.demo(),new ViewCycle(),1000,1000,false,true,false);
        if(graphics.shapes.stream().anyMatch(s->s.kind().equals("rounded")||s.kind().equals("border"))) throw new AssertionError("Minimal shape not applied");
        config.appearance(2).design=TabAppearance.SQUARE; graphics=new class_332(); TrackerRenderer.drawAll(graphics,config,Ledger.demo(),new ViewCycle(),1000,1000,false,true,false);
        if(graphics.shapes.stream().anyMatch(s->s.kind().equals("rounded")||s.kind().equals("border"))||graphics.shapes.stream().filter(s->s.kind().equals("fill")).count()<5) throw new AssertionError("Square shape not applied");
        if(Math.abs(TrackerRenderer.bounds(config,0,1000,1000,false).scale()-.9f)>.00001) throw new AssertionError("Hit bounds size differs");
        parent.buttons().stream().filter(b->b.label().equals("Style")).findFirst().orElseThrow().click().run();
        client.saveSucceeds=false; var current=class_310.method_1551().screen; click(current,"Done");
        if(class_310.method_1551().screen!=current) throw new AssertionError("Failed save silently closed editor");
        click(current,"Cancel"); if(class_310.method_1551().screen!=parent) throw new AssertionError("Cancel did not return after save failure");
        System.out.println("Appearance UI/renderer smoke passed: navigation, RGB picker, isolated size, preview, saved settings, themed shapes, hit bounds and failed-save handling");
    }
}
