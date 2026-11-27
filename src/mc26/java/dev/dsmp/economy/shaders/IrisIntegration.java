package dev.dsmp.economy.shaders;

import net.fabricmc.loader.api.FabricLoader;

public final class IrisIntegration {
    private IrisIntegration(){}
    public static IrisBridge current(){
        var loader=FabricLoader.getInstance();boolean installed=loader.isModLoaded("iris");
        String version=loader.getModContainer("iris").map(mod->mod.getMetadata().getVersion().getFriendlyString()).orElse("");
        return IrisBridge.discover(installed,version,IrisIntegration.class.getClassLoader());
    }
}
