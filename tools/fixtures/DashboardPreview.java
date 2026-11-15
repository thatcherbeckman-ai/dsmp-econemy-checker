import com.google.gson.Gson;
import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.core.*;
import net.minecraft.*;
import java.nio.file.*;
import java.util.Map;

/** Exports the UI layout with explicitly synthetic sample data, not a game screenshot. */
public final class DashboardPreview {
    public static void main(String[] args)throws Exception{
        var mod=new EconomyTrackerClient(Files.createTempDirectory("dashboard-preview-"));mod.config.animateSnow=false;
        for(int i=0;i<80;i++)mod.ledger.recordBalance(Money.parse("124B")+(long)(Math.sin(i*.16)*Money.parse("3B"))+i*Money.parse("12M"),i+1);
        String[] names={"Alex","Blair","Casey","Drew","Ember","Finley"};
        for(int i=0;i<18;i++)mod.ledger.recordBet(new Ledger.Bet("demo"+i,names[i%names.length],(i%3==0?1:-1)*Money.parse((i+1)+"M"),100+i));
        mod.openMenu();var screen=class_310.method_1551().screen;screen.field_22789=960;screen.field_22790=540;screen.method_41843();
        for(String page:new String[]{"overview","modules"}){
            if(page.equals("modules"))screen.widgets.stream().filter(b->b.label.equals("HUD modules")).findFirst().orElseThrow().click();
            var graphics=new class_332();screen.method_25394(graphics,0,0,0);
            Files.writeString(Path.of("/tmp/economy-"+page+"-preview.json"),new Gson().toJson(Map.of("width",960,"height",540,"sampleData",true,"draws",graphics.draws)));
        }
        System.out.println("Exported sample-data dashboard layout previews");
    }
}
