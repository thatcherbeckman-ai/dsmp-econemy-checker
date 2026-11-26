package dev.dsmp.economy.wallethistory;

import dev.dsmp.economy.core.Ledger;
import dev.dsmp.economy.design.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class WalletHistoryStore {
    private record Data(int version,List<Ledger.Point> points){}
    private final Path file;
    public WalletHistoryStore(Path folder,String scope){if(scope==null||scope.isBlank())throw new IllegalArgumentException("Missing wallet scope");try{file=folder.resolve(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(scope.getBytes(StandardCharsets.UTF_8)))+".json");}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
    public Path path(){return file;}
    public WalletHistory load(long now)throws IOException{if(!Files.exists(file,LinkOption.NOFOLLOW_LINKS))return new WalletHistory();if(Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>2_097_152)throw new IOException("Invalid wallet history file");try{var data=DesignLook.JSON.fromJson(Files.readString(file),Data.class);if(data==null||data.version!=1)throw new IllegalArgumentException("Invalid wallet history");return WalletHistory.restore(data.points,now);}catch(RuntimeException ex){throw new IOException("Wallet history unreadable; original retained",ex);}}
    public void save(WalletHistory history)throws IOException{if(history.points().size()>WalletHistory.MAX_POINTS)throw new IllegalArgumentException("Too many balance readings");DesignFiles.write(file,DesignLook.JSON.toJson(new Data(1,history.points())));}
}
