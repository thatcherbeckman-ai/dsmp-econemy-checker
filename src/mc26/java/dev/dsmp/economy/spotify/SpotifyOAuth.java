package dev.dsmp.economy.spotify;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** One-use OAuth PKCE listener; bound only to IPv4 loopback, never a public interface. */
public final class SpotifyOAuth implements AutoCloseable {
    public static final int PORT=43821;
    public static final String REDIRECT="http://127.0.0.1:"+PORT+"/callback";
    private final ServerSocket server;
    private final String state=random(),verifier=random()+random();
    private final long deadline=System.nanoTime()+180_000_000_000L;
    public SpotifyOAuth()throws IOException{this(PORT);}
    SpotifyOAuth(int port)throws IOException{
        server=new ServerSocket();
        try{server.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),port),4);server.setSoTimeout(1000);}catch(IOException ex){server.close();throw ex;}
    }
    public URI authorize(String clientId){
        if(clientId==null||!clientId.matches("[a-fA-F0-9]{32}"))throw new IllegalArgumentException("A Spotify Client ID is required.");
        return URI.create("https://accounts.spotify.com/authorize?"+form(Map.of("client_id",clientId,"response_type","code","redirect_uri",redirect(),"code_challenge_method","S256","code_challenge",challenge(verifier),"state",state,"scope","user-read-currently-playing")));
    }
    public String verifier(){return verifier;}
    String redirect(){return "http://127.0.0.1:"+server.getLocalPort()+"/callback";}
    public String awaitCode()throws IOException{
        while(!server.isClosed()&&System.nanoTime()<deadline){
            try(Socket socket=server.accept()){
                socket.setSoTimeout(2000);
                try{
                    String header=readHeader(socket.getInputStream());String[] lines=header.split("\r\n"),request=lines[0].split(" ");
                    if(request.length!=3||!request[0].equals("GET")||!request[2].equals("HTTP/1.1"))throw new IllegalArgumentException();
                    long hosts=Arrays.stream(lines).filter(s->s.regionMatches(true,0,"Host:",0,5)).count();
                    if(hosts!=1||Arrays.stream(lines).noneMatch(s->s.equalsIgnoreCase("Host: 127.0.0.1:"+server.getLocalPort())))throw new IllegalArgumentException();
                    URI uri=URI.create(request[1]);if(uri.isAbsolute()||uri.getRawAuthority()!=null||!"/callback".equals(uri.getRawPath())||uri.getRawFragment()!=null)throw new IllegalArgumentException();
                    Map<String,String> params=query(uri.getRawQuery());
                    if(!MessageDigest.isEqual(state.getBytes(StandardCharsets.UTF_8),params.getOrDefault("state","").getBytes(StandardCharsets.UTF_8)))throw new IllegalArgumentException();
                    if(params.containsKey("error")){reply(socket,200,"Spotify sign-in was cancelled. Return to Minecraft.");throw new Denied();}
                    String code=params.get("code");if(code==null||code.isBlank()||code.length()>2048||code.chars().anyMatch(c->c<32||c==127))throw new IllegalArgumentException();
                    reply(socket,200,"Spotify sign-in received. Return to Minecraft to check the connection.");return code;
                }catch(IllegalArgumentException ex){reply(socket,400,"Invalid sign-in callback. Return to the Spotify sign-in tab.");}
                catch(SocketTimeoutException ex){/* Incomplete browser request; keep the valid flow alive. */}
            }catch(SocketTimeoutException ex){/* Allows bounded flow timeout. */}
        }
        throw new IOException("Spotify sign-in expired.");
    }
    private static String readHeader(InputStream input)throws IOException{
        var out=new ByteArrayOutputStream();int b,tail=0;
        while(out.size()<8192&&(b=input.read())!=-1){out.write(b);tail=(tail<<8)|b;if(out.size()>=4&&tail==0x0D0A0D0A)return out.toString(StandardCharsets.US_ASCII);}
        throw new IllegalArgumentException("Invalid callback headers");
    }
    private static void reply(Socket socket,int code,String text)throws IOException{
        byte[] body=text.getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(("HTTP/1.1 "+code+(code==200?" OK":" Bad Request")+"\r\nContent-Type: text/plain; charset=utf-8\r\nCache-Control: no-store\r\nReferrer-Policy: no-referrer\r\nX-Content-Type-Options: nosniff\r\nContent-Security-Policy: default-src 'none'\r\nConnection: close\r\nContent-Length: "+body.length+"\r\n\r\n").getBytes(StandardCharsets.US_ASCII));socket.getOutputStream().write(body);socket.getOutputStream().flush();
    }
    public static Map<String,String> query(String query){
        if(query==null||query.length()>4096)throw new IllegalArgumentException();var map=new HashMap<String,String>();
        for(String entry:query.split("&")){String[] pair=entry.split("=",2);String key=URLDecoder.decode(pair[0],StandardCharsets.UTF_8),value=pair.length==2?URLDecoder.decode(pair[1],StandardCharsets.UTF_8):"";if(map.putIfAbsent(key,value)!=null)throw new IllegalArgumentException("Duplicate callback parameter");}return map;
    }
    public static String form(Map<String,String> values){return values.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));}
    static String challenge(String verifier){try{return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
    private static String random(){byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
    @Override public void close(){try{server.close();}catch(IOException ignored){}}
    public static final class Denied extends IOException {public Denied(){super("Sign-in cancelled.");}}
}
