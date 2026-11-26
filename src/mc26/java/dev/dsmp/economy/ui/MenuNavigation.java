package dev.dsmp.economy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Mod settings stay in their dashboard host; explicit presentation/canvas screens can still fill the display. */
public final class MenuNavigation {
    private MenuNavigation(){}
    public static Screen active(){var screen=Minecraft.getInstance().gui.screen();return screen instanceof DashboardScreen host&&host.activePanel()!=null?host.activePanel():screen;}
    public static boolean isOpen(Screen screen){return active()==screen;}
    public static void open(Screen target){
        var mc=Minecraft.getInstance();var actual=mc.gui.screen();
        if(actual instanceof DashboardScreen host){
            if(target==host){host.closePanel();return;}
            if(target instanceof WindowedScreen child&&!(target instanceof DashboardScreen)){host.openPanel(child);return;}
        }
        if(target instanceof WindowedScreen child&&child.dashboardHost()!=null){var host=child.dashboardHost();host.openPanel(child);mc.gui.setScreen(host);return;}
        if(target instanceof DashboardScreen host&&actual instanceof WindowedScreen child&&child.dashboardHost()==host){host.openPanel(child);mc.gui.setScreen(host);return;}
        mc.gui.setScreen(target);
    }
}
