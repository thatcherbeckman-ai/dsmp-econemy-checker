package dev.dsmp.economy.ui;
import dev.dsmp.economy.EconomyTrackerClient;
import net.minecraft.client.Minecraft;
public final class DashboardNavigation {
    private DashboardNavigation(){}
    public static void open(EconomyTrackerClient mod){net.minecraft.client.Minecraft.getInstance().gui.setScreen(new DashboardScreen(mod));}
}
