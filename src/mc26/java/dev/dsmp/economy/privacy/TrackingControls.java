package dev.dsmp.economy.privacy;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;

/** Persisted recording/privacy opt-outs; appearance profiles cannot override them. */
public final class TrackingControls {
    private static TrackingOptions current=new TrackingOptions();private static TrackingStore store;private static String error;
    private TrackingControls(){}
    public static void initialize(){if(store!=null)return;store=new TrackingStore(FabricLoader.getInstance().getConfigDir().resolve("economy-tracker/tracking-controls.json"));try{current=store.load();}catch(IOException ex){current.turnOffAll();error=ex.getMessage();org.slf4j.LoggerFactory.getLogger("TrackingControls").warn("Tracking controls unreadable; recording disabled and original retained",ex);}}
    public static TrackingOptions current(){return current.copy();}public static String error(){return error;}
    public static void save(TrackingOptions options)throws IOException{initialize();if(error!=null)throw new IOException(error);store.save(options);current=options.copy();}
}
