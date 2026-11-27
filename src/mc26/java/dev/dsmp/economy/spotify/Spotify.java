package dev.dsmp.economy.spotify;

import net.fabricmc.loader.api.FabricLoader;

public final class Spotify {
    private static SpotifyService service;
    private Spotify(){}
    public static void initialize(){if(service==null)service=new SpotifyService(FabricLoader.getInstance().getConfigDir().resolve("economy-tracker/spotify.json"));}
    public static SpotifyService service(){if(service==null)initialize();return service;}
    public static void close(){if(service!=null)service.close();}
}
