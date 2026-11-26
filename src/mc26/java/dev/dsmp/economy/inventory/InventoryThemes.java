package dev.dsmp.economy.inventory;

import java.io.IOException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

/** Client-wide cosmetic state. Drafts apply only after their atomic save succeeds. */
public final class InventoryThemes {
    private static InventoryOptions current=InventoryOptions.defaults();
    private static InventoryThemeStore store;
    private static String loadError;
    private InventoryThemes(){}
    public static void initialize(){
        if(store!=null)return;
        store=new InventoryThemeStore(FabricLoader.getInstance().getConfigDir().resolve("economy-tracker/inventory-themes.json"));
        try{current=store.load();}catch(IOException ex){loadError=ex.getMessage();LoggerFactory.getLogger("InventoryThemes").warn("Inventory theme preferences could not be read; keeping the original file",ex);}
    }
    public static InventoryOptions current(){return current;}
    public static String loadError(){return loadError;}
    public static void install(InventoryOptions options){current=java.util.Objects.requireNonNull(options);}
    public static void save(InventoryOptions options)throws IOException {
        initialize();if(loadError!=null)throw new IOException(loadError+". Original file retained; see config/economy-tracker/inventory-themes.json.");
        dev.dsmp.economy.design.DesignManager.beforeSave("Before inventory change");store.save(options);current=options;
    }
}
