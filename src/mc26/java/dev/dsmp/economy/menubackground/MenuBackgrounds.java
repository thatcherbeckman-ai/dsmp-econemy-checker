package dev.dsmp.economy.menubackground;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import dev.dsmp.economy.ui.MenuBackgroundScreen;
import java.io.IOException;

public final class MenuBackgrounds {
    private static MenuBackgroundOptions current=new MenuBackgroundOptions();
    private static MenuBackgroundStore store;private static String loadError;
    private MenuBackgrounds(){}
    public static void initialize(){if(store!=null)return;store=new MenuBackgroundStore(FabricLoader.getInstance().getConfigDir().resolve("economy-tracker"));try{current=store.load();}catch(IOException ex){loadError=ex.getMessage();org.slf4j.LoggerFactory.getLogger("MenuBackgrounds").warn("Original menu background preferences retained",ex);}}
    public static MenuBackgroundOptions current(){return current.copy();}
    public static MenuBackgroundOptions visible(){var screen=Minecraft.getInstance().gui.screen();return screen instanceof dev.dsmp.economy.ui.CharacterSceneScreen scene?scene.previewOptions():screen instanceof dev.dsmp.economy.ui.CharacterPreviewScreen preview?preview.previewOptions():screen instanceof MenuBackgroundScreen picker?picker.previewOptions():current;}
    public static MenuBackgroundStore store(){initialize();return store;}
    public static String loadError(){return loadError;}
    public static void install(MenuBackgroundOptions options){options.validate();current=options.copy();}
    public static void save(MenuBackgroundOptions options)throws IOException{initialize();if(loadError!=null)throw new IOException(loadError+". Original file retained.");if(options.scene==MenuScene.CUSTOM){if(options.customImage.isEmpty())throw new IOException("Import a PNG first");store.image(options.customImage);}dev.dsmp.economy.design.DesignManager.beforeSave("Before scene save");store.save(options);current=options.copy();}
}
