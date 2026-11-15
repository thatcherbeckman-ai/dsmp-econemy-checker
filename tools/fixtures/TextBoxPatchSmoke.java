import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.ui.*;
import dev.dsmp.economy.text.*;
import net.minecraft.*;
import java.nio.file.Files;

public final class TextBoxPatchSmoke {
    private static void click(class_437 screen,String label){screen.widgets.stream().filter(b->b.label.equals(label)).findFirst().orElseThrow(()->new AssertionError(label)).click();}
    public static void main(String[] args)throws Exception{
        var client=new EconomyTrackerClient(Files.createTempDirectory("text-box-ui-"));var before=client.ledger.snapshot();var parent=new TrackerScreen(client,2);
        parent.buttons().stream().filter(b->b.label().equals("Text boxes")).findFirst().orElseThrow().click().run();
        if(!(class_310.method_1551().screen instanceof TextBoxesScreen screen)||client.showHud(screen))throw new AssertionError("Text editor not opened or gated");
        click(screen,"New");if(client.config.textBoxes.size()!=1)throw new AssertionError("No text box");
        screen.fields.getFirst().method_1852("min: 100m\\nmax: 2b");click(screen,"Mint/Lime");click(screen,"Left");click(screen,"Size +");
        var box=client.config.textBoxes.getFirst();if(box.gradient!=TextBox.DOWN||box.align!=1||box.size!=1.1f)throw new AssertionError("Text controls failed");
        var graphics=new class_332();screen.method_25394(graphics,0,0,0);
        if(graphics.text.stream().noneMatch(t->t.equals("min: 100m"))||graphics.text.stream().noneMatch(t->t.equals("max: 2b")))throw new AssertionError("Multiline text not rendered");
        if(graphics.scissorDepth!=0||graphics.method_51448().m00()!=1||graphics.method_51448().m20()!=0)throw new AssertionError("Rendering state leaked");
        var b=TextHud.bounds(box,640,480,true);
        if(!screen.method_25402(new class_11909(b.x()+2,b.y()+2,0),false))throw new AssertionError("Cannot pick text");
        screen.method_25403(new class_11909(12,180,0),0,0);screen.method_25406(new class_11909(12,180,0));
        if(box.x>.1f||box.y>.1f)throw new AssertionError("Text drag failed");
        click(screen,"Visible");graphics=new class_332();TextHud.draw(graphics,client.config,640,480,false);if(!graphics.text.isEmpty())throw new AssertionError("Hidden text drawn on HUD");
        click(screen,"New");screen.fields.getFirst().method_1852("Second");click(screen,"Inter");click(screen,"Inter bold");click(screen,"Grad: Solid");
        var second=client.config.textBoxes.getLast();if(second.font!=2||second.gradient!=TextBox.ACROSS)throw new AssertionError("Font/gradient failed");
        graphics=new class_332();TextHud.draw(graphics,client.config,640,480,false);
        if(graphics.shapes.stream().filter(s->s.kind().equals("text")).map(s->s.color()).distinct().count()<2)throw new AssertionError("Horizontal gradient not drawn");
        click(screen,"Done");if(class_310.method_1551().screen!=parent)throw new AssertionError("Did not save/return");
        var loaded=client.store.loadConfig();if(loaded.textBoxes.size()!=2||!loaded.textBoxes.getFirst().text.contains("\n")||loaded.textBoxes.getLast().font!=2)throw new AssertionError("Text settings not saved");
        parent.buttons().stream().filter(v->v.label().equals("Text boxes")).findFirst().orElseThrow().click().run();var current=class_310.method_1551().screen;
        current.fields.get(1).method_1852("BAD");click(current,"Done");if(class_310.method_1551().screen!=current)throw new AssertionError("Invalid hex accepted");
        click(current,"Cancel");if(client.config.textBoxes.size()!=2||!before.equals(client.ledger.snapshot()))throw new AssertionError("Cancel or financial isolation failed");
        System.out.println("Text box UI smoke passed: text entry, multiline gradients, fonts, glass, drag, visibility, persistence, cancel and rendering-state restoration");
    }
}
