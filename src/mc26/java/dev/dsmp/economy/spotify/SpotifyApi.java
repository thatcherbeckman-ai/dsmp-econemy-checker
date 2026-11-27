package dev.dsmp.economy.spotify;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.Flow;

/** Read-only Web API with bounded responses, deadlines and no cross-host redirects. */
public final class SpotifyApi {
    public record Reply(int status,String body,String retryAfter){}
    public record Tokens(String access,String refresh,long expiresAt){@Override public String toString(){return "Spotify credentials [redacted]";}}
    public record Track(String title,String artist,String url,boolean playing,long progress,long duration,long sampledAt){
        public long progressAt(long now){return Math.clamp(progress+(playing?Math.clamp(now-sampledAt,0,15_000):0),0,duration);}
    }
    public static final class Failure extends IOException{
        public final int status;public final long retryMillis;
        Failure(int status,String retry){super("Spotify request failed ("+status+").");this.status=status;this.retryMillis=retry(retry);}
        static long retry(String value){
            try{long seconds=Math.max(5,Long.parseLong(value));return seconds>Long.MAX_VALUE/1000?Long.MAX_VALUE:seconds*1000;}
            catch(Exception ex){try{return Math.max(5000,java.time.ZonedDateTime.parse(value,java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()-System.currentTimeMillis());}catch(Exception ignored){return 30_000;}}
        }
    }
    @FunctionalInterface public interface Transport{Reply request(URI uri,String body,String bearer)throws IOException,InterruptedException;}
    private final Transport transport;
    public SpotifyApi(){
        HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NEVER).build();
        transport=(uri,body,bearer)->{
            var builder=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(12)).header("Accept","application/json");
            if(bearer!=null)builder.header("Authorization","Bearer "+bearer);
            if(body!=null)builder.header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body));
            var future=client.sendAsync(builder.build(),info->new LimitedBody());
            try{var response=future.get(12,TimeUnit.SECONDS);return new Reply(response.statusCode(),new String(response.body(),java.nio.charset.StandardCharsets.UTF_8),response.headers().firstValue("Retry-After").orElse("30"));}
            catch(TimeoutException ex){future.cancel(true);throw new IOException("Spotify request timed out.");}
            catch(ExecutionException ex){throw new IOException("Spotify request failed.");}
            catch(InterruptedException ex){future.cancel(true);throw ex;}
        };
    }
    public SpotifyApi(Transport transport){this.transport=transport;}
    static final class LimitedBody implements HttpResponse.BodySubscriber<byte[]> {
        private final ByteArrayOutputStream bytes=new ByteArrayOutputStream();private final CompletableFuture<byte[]> body=new CompletableFuture<>();private Flow.Subscription subscription;
        public CompletionStage<byte[]> getBody(){return body;}
        public void onSubscribe(Flow.Subscription value){subscription=value;value.request(1);}
        public void onNext(List<ByteBuffer> buffers){for(var buffer:buffers){if(buffer.remaining()>131_072-bytes.size()){subscription.cancel();body.completeExceptionally(new IOException("Spotify response exceeds the size limit."));return;}byte[] part=new byte[buffer.remaining()];buffer.get(part);bytes.writeBytes(part);}subscription.request(1);}
        public void onError(Throwable ex){body.completeExceptionally(ex);}
        public void onComplete(){body.complete(bytes.toByteArray());}
    }
    public Tokens exchange(String clientId,String code,String verifier,String redirect,long now)throws IOException,InterruptedException{return token(Map.of("client_id",clientId,"grant_type","authorization_code","code",code,"code_verifier",verifier,"redirect_uri",redirect),null,now);}
    public Tokens refresh(String clientId,Tokens token,long now)throws IOException,InterruptedException{return token(Map.of("client_id",clientId,"grant_type","refresh_token","refresh_token",token.refresh),token.refresh,now);}
    private Tokens token(Map<String,String> form,String oldRefresh,long now)throws IOException,InterruptedException{
        Reply reply=transport.request(URI.create("https://accounts.spotify.com/api/token"),SpotifyOAuth.form(form),null);check(reply);
        try{var json=JsonParser.parseString(reply.body).getAsJsonObject();String access=secret(json,"access_token"),refresh=json.has("refresh_token")?secret(json,"refresh_token"):oldRefresh;
            if(refresh==null||!"Bearer".equalsIgnoreCase(json.get("token_type").getAsString()))throw new IllegalArgumentException();
            if(json.has("scope")&&!Arrays.asList(json.get("scope").getAsString().split(" ")).contains("user-read-currently-playing"))throw new IllegalArgumentException();
            long expires=json.get("expires_in").getAsLong();if(expires<1||expires>86400)throw new IllegalArgumentException();return new Tokens(access,refresh,now+expires*1000);
        }catch(RuntimeException ex){throw new IOException("Invalid Spotify authorization response.");}
    }
    public Track current(Tokens token,long now)throws IOException,InterruptedException{
        Reply reply=transport.request(URI.create("https://api.spotify.com/v1/me/player/currently-playing"),null,token.access);if(reply.status==204)return null;check(reply);
        try{var root=JsonParser.parseString(reply.body).getAsJsonObject();if(!root.has("item")||root.get("item").isJsonNull())return null;var item=root.getAsJsonObject("item");
            if(!"track".equals(item.get("type").getAsString()))return null;
            var artists=new ArrayList<String>();for(var a:item.getAsJsonArray("artists")){if(artists.size()>=12)break;artists.add(clean(a.getAsJsonObject().get("name").getAsString()));}
            String url="";if(item.has("external_urls")){var urls=item.getAsJsonObject("external_urls");if(urls.has("spotify")){String candidate=urls.get("spotify").getAsString();URI uri=URI.create(candidate);if("https".equals(uri.getScheme())&&"open.spotify.com".equals(uri.getHost())&&uri.getUserInfo()==null&&uri.getPort()==-1)url=candidate;}}
            long duration=item.get("duration_ms").getAsLong();if(duration<1||duration>86_400_000)throw new IllegalArgumentException();
            long progress=root.has("progress_ms")&&!root.get("progress_ms").isJsonNull()?root.get("progress_ms").getAsLong():0;
            return new Track(clean(item.get("name").getAsString()),String.join(", ",artists),url,root.get("is_playing").getAsBoolean(),Math.clamp(progress,0,duration),duration,now);
        }catch(RuntimeException ex){throw new IOException("Could not read Spotify playback details.");}
    }
    private static String secret(JsonObject json,String key){String value=json.get(key).getAsString();if(value.isBlank()||value.length()>8192||value.chars().anyMatch(c->c<33||c>126))throw new IllegalArgumentException();return value;}
    private static String clean(String value){return value.replaceAll("[\\p{Cc}\\p{Cf}§]","").codePoints().limit(256).collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append).toString();}
    private static void check(Reply reply)throws Failure{if(reply.status!=200)throw new Failure(reply.status,reply.retryAfter);}
}
