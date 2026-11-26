package dev.dsmp.economy.design;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.inventory.*;
import dev.dsmp.economy.menubackground.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Coordinates appearance-only profile application and backups; never opens tracked records. */
public final class DesignManager {
    private static EconomyTrackerClient client;private static Path folder;private static DesignArchive archive;private static boolean applying;
    private DesignManager(){}
    public static void initialize(EconomyTrackerClient mod){client=mod;if(archive!=null)return;folder=FabricLoader.getInstance().getConfigDir().resolve("economy-tracker");archive=new DesignArchive(folder.resolve("designs"));DesignAccess.initialize(folder);}
    public static DesignArchive archive(){if(archive==null)throw new IllegalStateException("Design tools are not initialized");return archive;}
    public static DesignLook capture(EconomyTrackerClient mod){return DesignLook.capture(mod.config,MenuBackgrounds.current(),InventoryThemes.current(),DesignAccess.current());}
    public static void beforeSave(String reason)throws IOException{if(client!=null)beforeSave(client,reason);}
    public static void beforeSave(EconomyTrackerClient mod,String reason)throws IOException{
        if(applying||archive==null)return;
        // Read committed settings so a live slider draft cannot replace the last saved look.
        var config=mod.store.loadConfig();var background=new MenuBackgroundStore(folder).load();var inventory=new InventoryThemeStore(folder.resolve("inventory-themes.json")).load();var access=new AccessStore(folder.resolve("accessibility.json")).load();
        archive.backup(reason,DesignLook.capture(config,background,inventory,access));
    }
    public static void saveAccess(EconomyTrackerClient mod,AccessOptions access)throws IOException{
        access.validate();if(DesignAccess.error()!=null)throw new IOException(DesignAccess.error());if(DesignLook.JSON.toJson(access).equals(DesignLook.JSON.toJson(DesignAccess.current())))return;beforeSave(mod,"Before accessibility change");new AccessStore(folder.resolve("accessibility.json")).save(access);DesignAccess.install(access);
    }
    public static void apply(EconomyTrackerClient mod,DesignLook look)throws IOException{
        look.validate();mod.store.loadConfig();if(MenuBackgrounds.loadError()!=null||InventoryThemes.loadError()!=null||DesignAccess.error()!=null)throw new IOException("Existing appearance settings are unreadable; original files retained");
        if(look.background.scene==MenuScene.CUSTOM)MenuBackgrounds.store().image(look.background.customImage);
        var next=look.applyTo(mod.config);archive.backup("Before applying a look",capture(mod));
        var files=new LinkedHashMap<Path,String>();files.put(folder.resolve("settings.json"),DesignLook.JSON.toJson(next));files.put(folder.resolve("menu-background.json"),DesignLook.JSON.toJson(look.background));files.put(folder.resolve("inventory-themes.json"),DesignLook.JSON.toJson(look.inventory));files.put(folder.resolve("accessibility.json"),DesignLook.JSON.toJson(look.access));
        applying=true;try{DesignFiles.commit(files);mod.config=next;MenuBackgrounds.install(look.background);InventoryThemes.install(look.inventory);DesignAccess.install(look.access);}finally{applying=false;}
    }
}
