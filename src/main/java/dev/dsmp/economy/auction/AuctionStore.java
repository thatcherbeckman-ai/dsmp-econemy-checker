package dev.dsmp.economy.auction;

import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.io.IOException;

/** Isolated by server/account; failed reads never overwrite the original. */
public final class AuctionStore {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path path;
    public AuctionStore(Path directory,String scope){
        if(scope==null||scope.isBlank())throw new IllegalArgumentException("Join the configured server first");
        try{path=directory.resolve(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(scope.getBytes(StandardCharsets.UTF_8)))+".json");}catch(java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
    }
    public AuctionRound load()throws IOException{
        if(!Files.exists(path))return null;
        if(Files.size(path)>2_000_000)throw new IOException("Auction file too large");
        try(var reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){
            var result=JSON.fromJson(reader,AuctionRound.class);if(result==null)throw new IllegalArgumentException("Empty auction file");return result;
        }catch(RuntimeException ex){throw new IOException("Invalid auction file; original preserved",ex);}
    }
    public void save(AuctionRound round)throws IOException{
        Files.createDirectories(path.getParent());Path temporary=Files.createTempFile(path.getParent(),"auction-",".tmp");
        try{Files.writeString(temporary,JSON.toJson(round),StandardCharsets.UTF_8);try{Files.move(temporary,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(temporary,path,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temporary);}
    }
}
