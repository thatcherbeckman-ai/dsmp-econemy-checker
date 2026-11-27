package dev.dsmp.economy.spotify;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.LongSupplier;

/** One noninteractive Windows PowerShell helper, reading the documented local WinRT media API. */
public final class WindowsSpotifyMedia implements DesktopMedia {
    private static final String RESOURCE="/assets/economy_tracker/spotify/windows-now-playing.ps1";
    @FunctionalInterface interface Starter {Process start()throws IOException;}
    private final boolean supported;private final Starter starter;private final LongSupplier nanoClock;
    private Process process;private long lastOutput,retryAt;private boolean active;
    private Snapshot latest=new Snapshot(false,null,"Desktop mode is stopped.");
    public WindowsSpotifyMedia(){this(System.getProperty("os.name","").startsWith("Windows"),WindowsSpotifyMedia::launch,System::nanoTime);}
    WindowsSpotifyMedia(boolean supported,Starter starter,LongSupplier nanoClock){this.supported=supported;this.starter=starter;this.nanoClock=nanoClock;}
    @Override public synchronized void start(){active=true;}
    @Override public synchronized Snapshot read(long now){
        if(!active)return latest;
        if(!supported)return new Snapshot(false,null,"Desktop mode needs Windows 10/11 and the Spotify desktop app.");
        long clock=nanoClock.getAsLong();
        if(process!=null&&(!process.isAlive()||clock-lastOutput>15_000_000_000L))fail(process,"Windows media helper stopped responding. Retrying shortly.");
        if(process==null&&(retryAt==0||clock-retryAt>=0)){
            try{process=starter.start();lastOutput=clock;latest=new Snapshot(false,null,"Starting Windows media reader...");Process running=process;Thread reader=new Thread(()->readOutput(running),"Spotify desktop media");reader.setDaemon(true);reader.start();}
            catch(IOException|RuntimeException ex){retryAt=clock+30_000_000_000L;latest=new Snapshot(false,null,"Windows media reader unavailable. Check Windows PowerShell and Spotify.");}
        }
        return latest;
    }
    private void readOutput(Process running){
        try(var input=new InputStreamReader(running.getInputStream(),StandardCharsets.UTF_8)){
            String line;while((line=readLine(input))!=null){Snapshot frame=parse(line,System.currentTimeMillis());synchronized(this){if(process!=running)return;latest=frame;lastOutput=nanoClock.getAsLong();}}
        }catch(IOException|RuntimeException ignored){/* Only safe, fixed diagnostics reach the UI. */}
        synchronized(this){if(process==running)fail(running,"Windows media reader closed. Retrying shortly.");}
    }
    private void fail(Process running,String status){running.destroyForcibly();process=null;retryAt=nanoClock.getAsLong()+30_000_000_000L;latest=new Snapshot(false,null,status);}
    @Override public synchronized void close(){active=false;if(process!=null){process.destroyForcibly();process=null;}retryAt=0;latest=new Snapshot(false,null,"Desktop mode is stopped.");}
    private static Process launch()throws IOException{
        String root=System.getenv("SystemRoot");if(root==null||root.isBlank())throw new IOException("Windows directory unavailable.");
        Path executable=Path.of(root,"System32","WindowsPowerShell","v1.0","powershell.exe");
        if(!executable.isAbsolute()||!Files.isRegularFile(executable))throw new IOException("Windows PowerShell unavailable.");
        var builder=new ProcessBuilder(command(executable,script())).redirectError(ProcessBuilder.Redirect.DISCARD);
        builder.environment().put("THATCHACAT_SPOTIFY_PARENT_PID",Long.toString(ProcessHandle.current().pid()));
        return builder.start();
    }
    static String script()throws IOException{
        try(var input=WindowsSpotifyMedia.class.getResourceAsStream(RESOURCE)){if(input==null)throw new IOException("Missing Windows media reader.");byte[] bytes=input.readNBytes(32769);if(bytes.length>32768)throw new IOException("Invalid Windows media reader.");return new String(bytes,StandardCharsets.UTF_8);}
    }
    static List<String> command(Path executable,String script){return List.of(executable.toString(),"-NoLogo","-NoProfile","-NonInteractive","-WindowStyle","Hidden","-MTA","-EncodedCommand",Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE)));}
    static String readLine(Reader input)throws IOException{
        var line=new StringBuilder();int ch;
        while((ch=input.read())!=-1){if(ch=='\n')return line.toString();if(ch!='\r')line.append((char)ch);if(line.length()>8192)throw new IOException("Oversized media metadata.");}
        return line.isEmpty()?null:line.toString();
    }
    static boolean spotifySource(String source){return source!=null&&source.matches("(?i)(spotify(?:\\.exe)?|SpotifyAB\\.SpotifyMusic_[a-z0-9]+!Spotify)");}
    static Snapshot parse(String line,long now)throws IOException{
        if(line==null||line.length()>8192)throw new IOException("Invalid media metadata.");
        try{
            JsonObject json=JsonParser.parseString(line).getAsJsonObject();if(json.get("v").getAsInt()!=1)throw new IllegalArgumentException();
            String state=json.get("state").getAsString();
            if(state.equals("idle"))return new Snapshot(true,null,"Desktop mode ready. Open Spotify and play a song on this PC.");
            if(state.equals("error"))return new Snapshot(false,null,"Windows cannot read Spotify media information. Check the desktop app and Windows media controls.");
            if(!state.equals("playing")&&!state.equals("paused"))throw new IllegalArgumentException();
            if(!spotifySource(json.get("source").getAsString()))throw new IllegalArgumentException();
            String title=SpotifyApi.clean(json.get("title").getAsString()),artist=SpotifyApi.clean(json.get("artist").getAsString());
            if(title.isBlank())return new Snapshot(true,null,"Spotify is open. Play a song to show its title here.");
            long duration=Math.clamp(json.get("durationMs").getAsLong(),0,86_400_000),position=Math.clamp(json.get("positionMs").getAsLong(),0,duration);
            var track=new SpotifyApi.Track(title,artist.isBlank()?"Spotify desktop app":artist,"",state.equals("playing"),position,duration,now);
            return new Snapshot(true,track,state.equals("playing")?"Desktop app / playing":"Desktop app / paused");
        }catch(RuntimeException ex){throw new IOException("Invalid Spotify desktop metadata.");}
    }
}
