package dev.dsmp.economy.spotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;

class DesktopMediaTest {
    @TempDir Path folder;
    static final String FRAME="{\"v\":1,\"state\":\"playing\",\"source\":\"Spotify.exe\",\"title\":\"Song 🎵\",\"artist\":\"An artist\",\"positionMs\":42000,\"durationMs\":180000}";
    private static void until(BooleanSupplier condition)throws Exception{long end=System.nanoTime()+2_000_000_000L;while(!condition.getAsBoolean()&&System.nanoTime()<end)Thread.sleep(5);assertTrue(condition.getAsBoolean(),"Timed out waiting for local media reader");}
    private static class Media implements DesktopMedia {
        final AtomicInteger reads=new AtomicInteger(),starts=new AtomicInteger(),stops=new AtomicInteger();
        volatile Snapshot snapshot=new Snapshot(true,new SpotifyApi.Track("Local song","Local artist","",true,0,0,0),"Desktop app / playing");
        public void start(){starts.incrementAndGet();}public Snapshot read(long now){reads.incrementAndGet();return snapshot;}public void close(){stops.incrementAndGet();}
    }
    private static class FakeProcess extends Process {
        private final PipedInputStream input=new PipedInputStream(32768);private final PipedOutputStream output;
        volatile boolean alive=true;
        FakeProcess()throws IOException{output=new PipedOutputStream(input);}
        void frame(String value)throws IOException{output.write((value+"\n").getBytes(StandardCharsets.UTF_8));output.flush();}
        public InputStream getInputStream(){return input;}public InputStream getErrorStream(){return InputStream.nullInputStream();}public OutputStream getOutputStream(){return OutputStream.nullOutputStream();}
        public int waitFor(){return 0;}public int exitValue(){if(alive)throw new IllegalThreadStateException();return 0;}public boolean isAlive(){return alive;}
        public void destroy(){alive=false;try{output.close();}catch(IOException ignored){}}public Process destroyForcibly(){destroy();return this;}
    }
    private static SpotifyApi noApi(AtomicInteger calls){return new SpotifyApi((u,b,a)->{calls.incrementAndGet();throw new IOException("Web API must not be used in desktop mode");});}
    @Test void newDefaultsRecommendDesktopAndOldSettingsKeepWebApi()throws Exception{
        assertEquals(SpotifySource.DESKTOP,SpotifyOptions.defaults().source());
        Path file=folder.resolve("spotify.json");Files.writeString(file,"{\"clientId\":\"\",\"enabled\":true,\"hideIdle\":false,\"position\":8,\"scale\":120,\"palette\":3}");
        var old=SpotifyOptions.load(file);assertEquals(SpotifySource.WEB_API,old.source());assertEquals(8,old.position());
        var local=new SpotifyOptions("",true,false,8,120,3,SpotifySource.DESKTOP);local.save(file);assertEquals(local,SpotifyOptions.load(file));
        var json=new Gson().toJson(local);assertFalse(json.contains("token"));assertFalse(json.contains("password"));
    }
    @Test void metadataHandlesPlayingPausedUnicodeIdleAndUnknownProgress()throws Exception{
        var playing=WindowsSpotifyMedia.parse(FRAME,1000);assertTrue(playing.ready());assertEquals("Song 🎵",playing.track().title());assertEquals(43000,playing.track().progressAt(2000));assertTrue(playing.track().url().isEmpty());
        var paused=WindowsSpotifyMedia.parse(FRAME.replace("playing","paused"),1000);assertFalse(paused.track().playing());assertEquals(42000,paused.track().progressAt(2000));
        var unknown=WindowsSpotifyMedia.parse(FRAME.replace("180000","0"),1000);assertEquals(0,unknown.track().duration());assertEquals(0,unknown.track().progressAt(2000));
        assertNull(WindowsSpotifyMedia.parse("{\"v\":1,\"state\":\"idle\"}",1).track());assertFalse(WindowsSpotifyMedia.parse("{\"v\":1,\"state\":\"error\"}",1).ready());
    }
    @Test void onlySpotifyDesktopSourcesAreAcceptedAndUnsafeTextIsCleaned()throws Exception{
        for(String id:List.of("Spotify.exe","spotify","SpotifyAB.SpotifyMusic_zpdnekdrzrea0!Spotify"))assertTrue(WindowsSpotifyMedia.spotifySource(id));
        for(String id:List.of("chrome.exe","notspotify.exe","Spotify.exe;evil","SpotifyAB.Other_app!Spotify"))assertThrows(IOException.class,()->WindowsSpotifyMedia.parse(FRAME.replace("Spotify.exe",id),0));
        var clean=WindowsSpotifyMedia.parse(FRAME.replace("Song 🎵","§aSong\\n\\u202e"+"x".repeat(500)),1);assertTrue(clean.track().title().codePointCount(0,clean.track().title().length())<=256);assertFalse(clean.track().title().contains("§"));assertFalse(clean.track().title().contains("\n"));assertFalse(clean.track().title().contains("\u202e"));
        assertThrows(IOException.class,()->WindowsSpotifyMedia.parse(FRAME.replace("\"v\":1","\"v\":2"),1));assertThrows(IOException.class,()->WindowsSpotifyMedia.parse("x".repeat(8193),1));assertThrows(IOException.class,()->WindowsSpotifyMedia.parse("{",1));
    }
    @Test void stdoutReaderIsBoundedAndHandlesPartialEofAndCrLf()throws Exception{
        assertEquals("test",WindowsSpotifyMedia.readLine(new StringReader("test\r\n")));assertEquals("partial",WindowsSpotifyMedia.readLine(new StringReader("partial")));assertNull(WindowsSpotifyMedia.readLine(new StringReader("")));assertThrows(IOException.class,()->WindowsSpotifyMedia.readLine(new StringReader("x".repeat(8193))));
    }
    @Test void packagedHelperUsesFixedEncodedScriptWithoutWritingItToDisk()throws Exception{
        String script=WindowsSpotifyMedia.script();assertTrue(script.contains("GlobalSystemMediaTransportControlsSessionManager"));assertTrue(script.contains("THATCHACAT_SPOTIFY_PARENT_PID"));assertFalse(script.contains("Invoke-WebRequest"));assertFalse(script.contains("TryPlayAsync"));assertFalse(script.contains("TryPauseAsync"));
        var command=WindowsSpotifyMedia.command(Path.of("/trusted/windows/powershell.exe"),script);assertEquals("/trusted/windows/powershell.exe",command.getFirst());assertEquals(script,new String(Base64.getDecoder().decode(command.getLast()),StandardCharsets.UTF_16LE));assertTrue(command.contains("-NoProfile"));assertTrue(command.contains("-NonInteractive"));
    }
    @Test void unsupportedPlatformsNeverStartAHelper()throws Exception{
        AtomicInteger launches=new AtomicInteger();try(var media=new WindowsSpotifyMedia(false,()->{launches.incrementAndGet();throw new IOException();},System::nanoTime)){media.start();assertTrue(media.read(1).status().contains("Windows 10/11"));assertEquals(0,launches.get());}
    }
    @Test void oneHelperStreamsUpdatesAndStopPreventsLateOrAutomaticRestart()throws Exception{
        var launches=new AtomicInteger();var process=new FakeProcess();try(var media=new WindowsSpotifyMedia(true,()->{launches.incrementAndGet();return process;},System::nanoTime)){
            media.start();media.read(1);process.frame(FRAME);until(()->media.read(2).track()!=null);assertEquals(1,launches.get());assertEquals("Song 🎵",media.read(3).track().title());
            process.frame(FRAME.replace("playing","paused"));until(()->!media.read(4).track().playing());media.close();assertFalse(process.isAlive());assertNull(media.read(5).track());assertEquals(1,launches.get());
        }
    }
    @Test void unresponsiveHelperIsKilledAndRetriesRespectCooldown()throws Exception{
        var clock=new AtomicLong(1_000_000_000L);var process=new FakeProcess();var launches=new AtomicInteger();try(var media=new WindowsSpotifyMedia(true,()->{launches.incrementAndGet();return process;},clock::get)){
            media.start();media.read(1);clock.addAndGet(16_000_000_000L);assertFalse(media.read(2).ready());assertFalse(process.isAlive());media.read(3);assertEquals(1,launches.get());
        }
        launches.set(0);try(var media=new WindowsSpotifyMedia(true,()->{launches.incrementAndGet();throw new IOException("local failure");},clock::get)){media.start();media.read(1);media.read(2);assertEquals(1,launches.get());clock.addAndGet(31_000_000_000L);media.read(3);assertEquals(2,launches.get());}
    }
    @Test void desktopServiceNeverRequestsAccountApisAndStopDisableAndReloadWork()throws Exception{
        var calls=new AtomicInteger();var media=new Media();Path file=folder.resolve("spotify.json");
        try(var service=new SpotifyService(file,noApi(calls),media)){
            service.startDesktop();service.poll(10_000);assertTrue(service.connected());assertEquals("Local song",service.track().title());assertEquals(0,calls.get());assertEquals(SpotifySource.DESKTOP,service.options().source());
            media.snapshot=new DesktopMedia.Snapshot(true,new SpotifyApi.Track("Next song","Artist","",false,20,100,0),"Desktop app / paused");service.poll(12_000);assertFalse(service.track().playing());assertEquals("Next song",service.track().title());
            service.disconnect();int reads=media.reads.get();service.poll(14_000);assertEquals(reads,media.reads.get());assertNull(service.track());assertFalse(service.connected());
            service.startDesktop();service.poll(16_000);assertNotNull(service.track());var options=service.options();service.save(new SpotifyOptions("",false,true,6,100,0,SpotifySource.DESKTOP));service.poll(18_000);assertNull(service.track());assertFalse(service.connected());service.save(options);
        }
        var resumed=new Media();try(var service=new SpotifyService(file,noApi(calls),resumed)){service.poll(20_000);assertNotNull(service.track());assertTrue(resumed.starts.get()>0);assertEquals(0,calls.get());}
    }
    @Test void switchingFromWebApiClearsCredentialsAndIgnoresLateApiData()throws Exception{
        var started=new CountDownLatch(1);var release=new CountDownLatch(1);var local=new Media();var api=new SpotifyApi((u,b,a)->{started.countDown();if(!release.await(3,TimeUnit.SECONDS))throw new IOException();return new SpotifyApi.Reply(204,"","");});
        try(var service=new SpotifyService(folder.resolve("spotify.json"),api,local)){
            service.save(new SpotifyOptions(SpotifyTest.ID,true,true,6,100,0));service.session(new SpotifyApi.Tokens("fake","refresh",Long.MAX_VALUE));var request=CompletableFuture.runAsync(()->service.poll(10_000));assertTrue(started.await(2,TimeUnit.SECONDS));service.startDesktop();service.poll(12_000);assertEquals("Local song",service.track().title());release.countDown();request.get(3,TimeUnit.SECONDS);assertEquals("Local song",service.track().title());assertTrue(service.connected());assertThrows(IOException.class,service::connect);
        }
    }
    @Test void inFlightDesktopReadCannotRestoreStateAfterStop()throws Exception{
        var started=new CountDownLatch(1);var release=new CountDownLatch(1);var media=new Media(){@Override public Snapshot read(long now){started.countDown();try{release.await(3,TimeUnit.SECONDS);}catch(InterruptedException ex){Thread.currentThread().interrupt();}return snapshot;}};
        try(var service=new SpotifyService(folder.resolve("spotify.json"),noApi(new AtomicInteger()),media)){service.startDesktop();var request=CompletableFuture.runAsync(()->service.poll(10_000));assertTrue(started.await(2,TimeUnit.SECONDS));service.disconnect();release.countDown();request.get(3,TimeUnit.SECONDS);assertNull(service.track());assertFalse(service.connected());}
    }
    @Test void stoppingDesktopPersistsAndCannotAutoStartAfterMinecraftRestarts()throws Exception{
        Path file=folder.resolve("spotify.json");var calls=new AtomicInteger();try(var service=new SpotifyService(file,noApi(calls),new Media())){service.startDesktop();service.poll(10_000);service.stopDesktop();assertFalse(service.options().enabled());assertFalse(SpotifyOptions.load(file).enabled());assertNull(service.track());}
        var next=new Media();try(var service=new SpotifyService(file,noApi(calls),next)){service.poll(20_000);assertEquals(0,next.starts.get());assertEquals(0,next.reads.get());assertEquals(0,calls.get());}
    }
}
