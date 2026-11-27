package dev.dsmp.economy.spotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class SpotifyTest {
    @TempDir Path folder;
    static final String ID="0123456789abcdef0123456789abcdef";
    static final String TOKEN="{\"access_token\":\"test-access\",\"refresh_token\":\"test-refresh\",\"token_type\":\"Bearer\",\"expires_in\":3600,\"scope\":\"user-read-currently-playing\"}";
    static final String SONG="{\"is_playing\":true,\"progress_ms\":42000,\"item\":{\"type\":\"track\",\"name\":\"A song\",\"duration_ms\":180000,\"artists\":[{\"name\":\"An artist\"}],\"external_urls\":{\"spotify\":\"https://open.spotify.com/track/test\"}}}";
    private static SpotifyApi.Reply ok(String body){return new SpotifyApi.Reply(200,body,"5");}
    private SpotifyService service(SpotifyApi.Transport transport)throws Exception{var service=new SpotifyService(folder.resolve("spotify.json"),new SpotifyApi(transport));service.save(new SpotifyOptions(ID,true,true,6,100,0));service.session(new SpotifyApi.Tokens("access","refresh",System.currentTimeMillis()+3600_000));return service;}
    @Test void publicPreferencesRoundTripAndValidateWithoutCredentials()throws Exception{
        Path file=folder.resolve("spotify.json");assertEquals(SpotifyOptions.defaults(),SpotifyOptions.load(file));var options=new SpotifyOptions(ID,true,false,99,200,99);options.save(file);assertEquals(options,SpotifyOptions.load(file));assertEquals(8,options.position());assertEquals(150,options.scale());assertFalse(Files.readString(file).contains("token"));assertThrows(IllegalArgumentException.class,()->new SpotifyOptions("client secret",true,true,0,100,0));
        Files.writeString(file,"{");assertThrows(IOException.class,()->SpotifyOptions.load(file));assertEquals("{",Files.readString(file));Files.writeString(file," ".repeat(8193));assertThrows(IOException.class,()->SpotifyOptions.load(file));
    }
    @Test void settingsRejectSymlinksAndPreserveUnrelatedFiles()throws Exception{
        Path other=folder.resolve("other");Files.writeString(other,"keep");Path file=folder.resolve("spotify.json");Files.createSymbolicLink(file,other);assertThrows(IOException.class,()->SpotifyOptions.load(file));assertThrows(IOException.class,()->SpotifyOptions.defaults().save(file));assertEquals("keep",Files.readString(other));
    }
    @Test void pkceMatchesRfcVectorAndAuthorizationHasOnlyReadScope()throws Exception{
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",SpotifyOAuth.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
        try(var flow=new SpotifyOAuth(0)){URI uri=flow.authorize(ID);var q=SpotifyOAuth.query(uri.getRawQuery());assertEquals("accounts.spotify.com",uri.getHost());assertEquals("S256",q.get("code_challenge_method"));assertEquals(SpotifyOAuth.challenge(flow.verifier()),q.get("code_challenge"));assertEquals("user-read-currently-playing",q.get("scope"));assertEquals(flow.redirect(),q.get("redirect_uri"));assertTrue(flow.verifier().length()>=43&&flow.verifier().length()<=128);assertFalse(uri.toString().contains(flow.verifier()));assertThrows(IllegalArgumentException.class,()->flow.authorize("bad"));}
    }
    private static String request(URI redirect,String target,String host)throws Exception{
        try(Socket socket=new Socket("127.0.0.1",redirect.getPort())){socket.setSoTimeout(3000);socket.getOutputStream().write(("GET "+target+" HTTP/1.1\r\nHost: "+host+"\r\n\r\n").getBytes(StandardCharsets.US_ASCII));return new String(socket.getInputStream().readAllBytes(),StandardCharsets.UTF_8);}
    }
    @Test void callbackRejectsForgedStateDuplicateParametersAndHostThenAcceptsOnce()throws Exception{
        try(var flow=new SpotifyOAuth(0)){var params=SpotifyOAuth.query(flow.authorize(ID).getRawQuery());URI redirect=URI.create(flow.redirect());var result=CompletableFuture.supplyAsync(()->{try{return flow.awaitCode();}catch(IOException ex){throw new CompletionException(ex);}});
            String host="127.0.0.1:"+redirect.getPort();
            assertTrue(request(redirect,"/callback?state=wrong&code=forged",host).startsWith("HTTP/1.1 400"));
            assertTrue(request(redirect,"/callback?state="+params.get("state")+"&state=duplicate&code=forged",host).startsWith("HTTP/1.1 400"));
            assertTrue(request(redirect,"/callback?state="+params.get("state")+"&code=forged","example.com").startsWith("HTTP/1.1 400"));
            assertFalse(result.isDone());String response=request(redirect,"/callback?state="+params.get("state")+"&code=accepted-code",host);assertTrue(response.contains("Cache-Control: no-store"));assertFalse(response.contains("accepted-code"));assertEquals("accepted-code",result.get(3,TimeUnit.SECONDS));
        }
    }
    @Test void deniedCallbackAndClosedListenerDoNotProduceCredentials()throws Exception{
        try(var flow=new SpotifyOAuth(0)){var q=SpotifyOAuth.query(flow.authorize(ID).getRawQuery());URI redirect=URI.create(flow.redirect());var result=CompletableFuture.supplyAsync(()->{try{flow.awaitCode();return false;}catch(SpotifyOAuth.Denied ex){return true;}catch(IOException ex){throw new CompletionException(ex);}});request(redirect,"/callback?state="+q.get("state")+"&error=access_denied","127.0.0.1:"+redirect.getPort());assertTrue(result.get(3,TimeUnit.SECONDS));}
        var closed=new SpotifyOAuth(0);closed.close();assertThrows(IOException.class,closed::awaitCode);
    }
    @Test void exchangeAndRefreshUsePkcePublicClientAndKeepRefreshWhenOmitted()throws Exception{
        var api=new SpotifyApi((uri,body,bearer)->{assertEquals("https://accounts.spotify.com/api/token",uri.toString());assertNull(bearer);var q=SpotifyOAuth.query(body);assertEquals(ID,q.get("client_id"));assertFalse(q.containsKey("client_secret"));if(q.get("grant_type").equals("authorization_code")){assertEquals("verifier",q.get("code_verifier"));assertEquals(SpotifyOAuth.REDIRECT,q.get("redirect_uri"));return ok(TOKEN);}return ok(TOKEN.replace("\"refresh_token\":\"test-refresh\",",""));});
        var tokens=api.exchange(ID,"code","verifier",SpotifyOAuth.REDIRECT,1000);assertEquals(3601000,tokens.expiresAt());assertFalse(tokens.toString().contains("test-access"));assertEquals("test-refresh",api.refresh(ID,tokens,2000).refresh());
    }
    @Test void trackProgressAndArtistAndSafeSongLink()throws Exception{
        var api=new SpotifyApi((uri,body,bearer)->{assertEquals("https://api.spotify.com/v1/me/player/currently-playing",uri.toString());assertNull(body);assertEquals("access",bearer);return ok(SONG);});var track=api.current(new SpotifyApi.Tokens("access","refresh",999999),1000);assertEquals("A song",track.title());assertEquals("An artist",track.artist());assertEquals(43000,track.progressAt(2000));assertEquals(57000,track.progressAt(100000));assertEquals(42000,track.progressAt(0));
        var unsafe=new SpotifyApi((u,b,a)->ok(SONG.replace("https://open.spotify.com/track/test","https://example.com/track/test"))).current(new SpotifyApi.Tokens("a","r",1),1);assertTrue(unsafe.url().isEmpty());
    }
    @Test void idleUnsupportedMalformedAndControlTextHandled()throws Exception{
        assertNull(new SpotifyApi((u,b,a)->new SpotifyApi.Reply(204,"","")).current(new SpotifyApi.Tokens("a","r",1),1));assertNull(new SpotifyApi((u,b,a)->ok(SONG.replace("\"track\"","\"episode\""))).current(new SpotifyApi.Tokens("a","r",1),1));assertThrows(IOException.class,()->new SpotifyApi((u,b,a)->ok("{")).current(new SpotifyApi.Tokens("a","r",1),1));var track=new SpotifyApi((u,b,a)->ok(SONG.replace("A song","A §csong\\n"))).current(new SpotifyApi.Tokens("a","r",1),1);assertFalse(track.title().contains("§"));assertFalse(track.title().contains("\n"));
    }
    @Test void apiErrorsDoNotExposeBodyAndRetryBoundsAreSafe()throws Exception{
        var error=assertThrows(SpotifyApi.Failure.class,()->new SpotifyApi((u,b,a)->new SpotifyApi.Reply(429,"secret error data","120")).current(new SpotifyApi.Tokens("a","r",1),1));assertEquals(120000,error.retryMillis);assertFalse(error.getMessage().contains("secret"));assertEquals(5000,SpotifyApi.Failure.retry("-8"));assertEquals(99999999000L,SpotifyApi.Failure.retry("99999999"));assertEquals(Long.MAX_VALUE,SpotifyApi.Failure.retry(Long.toString(Long.MAX_VALUE)));assertEquals(30000,SpotifyApi.Failure.retry("invalid"));
    }
    @Test void streamingResponseRejectsOversizeBeforeAccumulatingIt()throws Exception{
        var limited=new SpotifyApi.LimitedBody();var cancelled=new AtomicBoolean();limited.onSubscribe(new Flow.Subscription(){public void request(long n){}public void cancel(){cancelled.set(true);}});limited.onNext(List.of(ByteBuffer.wrap(new byte[131072])));limited.onNext(List.of(ByteBuffer.wrap(new byte[1])));assertTrue(cancelled.get());assertThrows(ExecutionException.class,()->limited.getBody().toCompletableFuture().get());
    }
    @Test void pollRefreshesAndDisconnectClearsAllPlaybackAndSessionTokens()throws Exception{
        var calls=new AtomicInteger();try(var service=service((u,b,a)->{calls.incrementAndGet();return b==null?ok(SONG):ok(TOKEN);})){service.session(new SpotifyApi.Tokens("old","refresh",0));service.poll();assertTrue(service.connected());assertEquals("A song",service.track().title());assertEquals(2,calls.get());service.disconnect();assertFalse(service.connected());assertNull(service.track());service.poll();assertEquals(2,calls.get());assertFalse(Files.readString(folder.resolve("spotify.json")).contains("test-access"));}
    }
    @Test void unauthorizedRetriesRefreshOnceAndRateLimitStopsPolling()throws Exception{
        var calls=new AtomicInteger();try(var service=service((u,b,a)->{int n=calls.incrementAndGet();return n==1?new SpotifyApi.Reply(401,"",""):b!=null?ok(TOKEN):ok(SONG);})){service.poll();assertEquals(3,calls.get());assertNotNull(service.track());}
        calls.set(0);try(var service=service((u,b,a)->{calls.incrementAndGet();return new SpotifyApi.Reply(429,"","60");})){service.poll();service.save(new SpotifyOptions(ID,true,true,6,100,1));service.poll();assertEquals(1,calls.get());assertTrue(service.status().contains("rate limit"));assertNull(service.track());}
    }
    @Test void refreshRejectionRequiresReconnectAndDisabledOverlayDoesNotPoll()throws Exception{
        var calls=new AtomicInteger();try(var service=service((u,b,a)->{calls.incrementAndGet();return new SpotifyApi.Reply(400,"secret","");})){service.session(new SpotifyApi.Tokens("old","refresh",0));service.poll();assertFalse(service.connected());assertTrue(service.status().contains("Connect again"));service.poll();assertEquals(1,calls.get());}
        try(var service=service((u,b,a)->{fail("Disabled overlay fetched playback");return ok(SONG);})){service.save(new SpotifyOptions(ID,false,true,6,100,0));service.poll();assertNull(service.track());}
    }
    @Test void disconnectDuringNetworkRequestCannotRestoreOldPlayback()throws Exception{
        var started=new CountDownLatch(1);var release=new CountDownLatch(1);try(var service=service((u,b,a)->{started.countDown();if(!release.await(3,TimeUnit.SECONDS))throw new IOException();return ok(SONG);})){var running=CompletableFuture.runAsync(service::poll);assertTrue(started.await(2,TimeUnit.SECONDS));service.disconnect();release.countDown();running.get(3,TimeUnit.SECONDS);assertFalse(service.connected());assertNull(service.track());}
    }
    @Test void fullServiceBrowserCallbackConnectsUsingFakeApiAndCancelsCleanly()throws Exception{
        try(var service=service((u,b,a)->b==null?ok(SONG):ok(TOKEN))){service.disconnect();URI uri=service.connect();assertTrue(service.connecting());var q=SpotifyOAuth.query(uri.getRawQuery());URI redirect=URI.create(SpotifyOAuth.REDIRECT);request(redirect,"/callback?state="+q.get("state")+"&code=fake-code","127.0.0.1:"+redirect.getPort());long deadline=System.nanoTime()+2_000_000_000L;while(service.connecting()&&System.nanoTime()<deadline)Thread.onSpinWait();assertTrue(service.connected());service.poll();assertNotNull(service.track());service.connect();service.disconnect();assertFalse(service.connecting());assertFalse(service.connected());}
    }
}
