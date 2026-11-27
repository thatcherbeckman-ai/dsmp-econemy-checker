package dev.dsmp.economy.spotify;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.*;

/** Session-only credentials; HTTP and callback work never run on Minecraft's render thread. */
public final class SpotifyService implements AutoCloseable {
    private final Path file;private final SpotifyApi api;
    private final ScheduledExecutorService workers=Executors.newScheduledThreadPool(2,r->{var t=new Thread(r,"Spotify overlay");t.setDaemon(true);return t;});
    private SpotifyOptions options=SpotifyOptions.defaults();private SpotifyApi.Tokens tokens;private SpotifyOAuth flow;
    private SpotifyApi.Track track;private long epoch,nextPoll;private boolean closed;
    private String status="Not connected. Add your Spotify Client ID to begin.";
    public SpotifyService(Path file){this(file,new SpotifyApi());}
    public SpotifyService(Path file,SpotifyApi api){
        this.file=file;this.api=api;
        try{options=SpotifyOptions.load(file);}catch(IOException ex){status=ex.getMessage();}
        workers.scheduleWithFixedDelay(this::poll,1,1,TimeUnit.SECONDS);
    }
    public synchronized SpotifyOptions options(){return options;}
    public synchronized String status(){return status;}
    public synchronized boolean connected(){return tokens!=null;}
    public synchronized boolean connecting(){return flow!=null;}
    public synchronized SpotifyApi.Track track(){return track;}
    public synchronized void save(SpotifyOptions value)throws IOException{
        value.save(file);if(!value.clientId().equals(options.clientId()))disconnect();options=value;
        if(!value.enabled())track=null;
    }
    /** The caller opens this URI only in response to the user's Connect click. */
    public synchronized URI connect()throws IOException{
        if(closed)throw new IOException("Spotify is shutting down.");
        if(options.clientId().isEmpty())throw new IOException("Enter a Spotify developer Client ID first.");
        disconnect();
        try{flow=new SpotifyOAuth();}catch(IOException ex){status="Sign-in port 43821 is busy. Close the other sign-in and retry.";throw new IOException(status);}
        long generation=epoch;SpotifyOAuth request=flow;String clientId=options.clientId();URI uri=request.authorize(clientId);
        status="Waiting for Spotify sign-in in your browser (3 minutes).";
        workers.execute(()->{
            try{
                String code=request.awaitCode();
                synchronized(this){if(epoch!=generation||closed)return;}
                var result=api.exchange(clientId,code,request.verifier(),SpotifyOAuth.REDIRECT,System.currentTimeMillis());
                synchronized(this){if(epoch!=generation||closed)return;tokens=result;track=null;nextPoll=0;status="Connected. Play music on Spotify to see it here.";}
            }catch(Exception ex){synchronized(this){if(epoch==generation&&!closed){tokens=null;track=null;status=ex instanceof SpotifyOAuth.Denied?"Spotify sign-in cancelled.":"Sign-in failed or expired. Check the Client ID, redirect URI and app access, then retry.";}}}
            finally{request.close();synchronized(this){if(flow==request)flow=null;}}
        });return uri;
    }
    public synchronized void disconnect(){epoch++;if(flow!=null)flow.close();flow=null;tokens=null;track=null;nextPoll=0;status="Disconnected. Login tokens cleared from this session.";}
    // Package-visible for deterministic fake-API tests; no credential injection is exposed in the menu.
    synchronized void session(SpotifyApi.Tokens value){tokens=value;nextPoll=0;}
    void poll(){
        SpotifyApi.Tokens current;String clientId;long generation,now=System.currentTimeMillis();
        synchronized(this){if(closed||tokens==null||!options.enabled()||now<nextPoll)return;current=tokens;clientId=options.clientId();generation=epoch;nextPoll=now+5000;}
        try{
            if(current.expiresAt()-now<30_000){current=api.refresh(clientId,current,now);synchronized(this){if(epoch!=generation||closed)return;tokens=current;}}
            SpotifyApi.Track result;
            try{result=api.current(current,now);}catch(SpotifyApi.Failure ex){
                if(ex.status!=401)throw ex;
                current=api.refresh(clientId,current,now);synchronized(this){if(epoch!=generation||closed)return;tokens=current;}
                result=api.current(current,now);
            }
            synchronized(this){if(epoch!=generation||closed||!options.enabled())return;track=result;status=result==null?"Connected. No active music. Open Spotify and play a song.":result.playing()?"Connected / playing":"Connected / paused";}
        }catch(Exception ex){synchronized(this){
            if(epoch!=generation||closed)return;track=null;
            if(ex instanceof SpotifyApi.Failure failure){
                nextPoll=failure.retryMillis>Long.MAX_VALUE-now?Long.MAX_VALUE:now+failure.retryMillis;
                if(failure.status==400||failure.status==401){tokens=null;status="Spotify login expired. Connect again.";}
                else if(failure.status==403){nextPoll=now+60_000;status="Spotify denied access. Check this account's access in your developer app.";}
                else if(failure.status==429)status="Spotify rate limit. Retrying after its cooldown.";
                else status="Spotify is unavailable. Retrying shortly.";
            }else{nextPoll=now+15_000;status="Cannot reach Spotify. Check your connection; retrying shortly.";}
        }}
    }
    @Override public synchronized void close(){if(closed)return;disconnect();closed=true;workers.shutdownNow();}
}
