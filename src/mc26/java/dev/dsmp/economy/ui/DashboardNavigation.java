package dev.dsmp.economy.ui;
import dev.dsmp.economy.EconomyTrackerClient;
import net.minecraft.client.Minecraft;
public final class DashboardNavigation {
    private DashboardNavigation(){}
    public static void open(EconomyTrackerClient mod){MenuNavigation.open(new DashboardScreen(mod));}
}
