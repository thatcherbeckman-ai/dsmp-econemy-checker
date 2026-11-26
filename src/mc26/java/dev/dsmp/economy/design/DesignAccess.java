package dev.dsmp.economy.design;

import net.minecraft.client.Minecraft;
import dev.dsmp.economy.menubackground.MenuBackgroundOptions;

/** Saved accessibility plus the current Tools draft; individual artwork settings are retained. */
public final class DesignAccess {
    private static AccessOptions current=new AccessOptions();private static String error;
    private DesignAccess(){}
    public static void initialize(java.nio.file.Path folder){try{current=new AccessStore(folder.resolve("accessibility.json")).load();error=null;}catch(java.io.IOException ex){error=ex.getMessage();}}
    public static AccessOptions current(){return current.copy();}public static String error(){return error;}
    public static void install(AccessOptions value){value.validate();current=value.copy();}
    private static AccessOptions visible(){var client=Minecraft.getInstance();return client!=null&&dev.dsmp.economy.ui.MenuNavigation.active() instanceof dev.dsmp.economy.ui.DesignToolsScreen tools?tools.accessOptions():current;}
    public static float scale(){return visible().textScale/100f;}public static boolean contrast(){return visible().highContrast;}public static boolean reduced(){return visible().reducedMotion;}
    public static MenuBackgroundOptions background(MenuBackgroundOptions original){if(!reduced())return original;var result=original.copy();result.animations=false;result.parallax=false;result.particles=dev.dsmp.economy.menubackground.MenuScene.Particles.NONE;return result;}
}
