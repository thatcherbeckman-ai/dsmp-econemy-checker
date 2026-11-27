package dev.dsmp.economy.spotify;

/** Local media metadata only; implementations must never perform Spotify account/API requests. */
public interface DesktopMedia extends AutoCloseable {
    record Snapshot(boolean ready,SpotifyApi.Track track,String status){}
    default void start(){}
    Snapshot read(long now);
    @Override void close();
}
