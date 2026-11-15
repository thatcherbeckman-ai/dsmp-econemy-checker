package dev.dsmp.economy.dashboard;

import com.google.gson.Gson;
import dev.dsmp.economy.TrackerConfig;

/** Settings transactions are independent of immediately saved loan edits. */
public final class DashboardSettings {
    private static final Gson JSON=new Gson();
    private TrackerConfig config;
    private String checkpoint;
    public static final int[] ACCENTS={0x8B6CF2,0xDF6A80,0x4EC29C,0x6AD8E8,0xDFBA67};
    public DashboardSettings(TrackerConfig config){checkpoint(config);}
    public TrackerConfig config(){return config;}
    public void checkpoint(TrackerConfig config){this.config=config;checkpoint=JSON.toJson(config);}
    public TrackerConfig cancel(){var restored=JSON.fromJson(checkpoint,TrackerConfig.class);restored.validate();return restored;}
    public boolean visible(int id){return switch(id){case 0,1,2->config.panels[id].visible;case 3->config.loansVisible;case 4->config.rakebackPanel.visible;case 5->config.balancePanel.visible;default->throw new IllegalArgumentException("Unknown HUD module");};}
    public void visible(int id,boolean visible){switch(id){case 0,1,2->config.panels[id].visible=visible;case 3->config.loansVisible=visible;case 4->config.rakebackPanel.visible=visible;case 5->config.balancePanel.visible=visible;default->throw new IllegalArgumentException("Unknown HUD module");}}
    public void waitSeconds(int change){config.paymentSettlementSeconds=Math.clamp(config.paymentSettlementSeconds+change,5,600);}
    public void scale(float change){config.scale=Math.clamp(Math.round((config.scale+change)*100)/100f,.4f,1.5f);}
    public void opacity(int change){config.opacity=Math.clamp(config.opacity+change,40,240);}
    public void nextAccent(){int current=-1;for(int i=0;i<ACCENTS.length;i++)if(ACCENTS[i]==config.menuAccent)current=i;config.menuAccent=ACCENTS[(current+1)%ACCENTS.length];}
}
