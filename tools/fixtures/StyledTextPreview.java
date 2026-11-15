import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.text.*;
import dev.dsmp.economy.ui.TextHud;
import net.minecraft.*;
import com.google.gson.Gson;
import java.nio.file.*;
import java.util.Map;

/** Sample-data layout capture, not a Minecraft screenshot. */
public final class StyledTextPreview {
    public static void main(String[] args)throws Exception{
        System.setProperty("economy.fixture.bubbleFont",Path.of("src/main/resources/assets/economy_tracker/font/bubble-lilita-one.ttf").toAbsolutePath().toString());
        var mod=new EconomyTrackerClient(Files.createTempDirectory("styled-preview-"));var editor=new TextBoxEditor(mod.config);editor.reference("YourName");var box=editor.selected();box.x=.05f;box.y=.04f;box.size=1.65f;
        var g=new class_332();TextHud.draw(g,mod.config,480,600,false);Files.writeString(Path.of("/tmp/styled-text-preview.json"),new Gson().toJson(Map.of("width",480,"height",600,"sampleData",true,"draws",g.draws)));
        System.out.println("Exported sample-data styled text layout");
    }
}
