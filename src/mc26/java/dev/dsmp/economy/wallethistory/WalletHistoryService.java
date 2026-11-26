package dev.dsmp.economy.wallethistory;

import dev.dsmp.economy.core.Ledger;
import java.util.*;
import java.nio.file.Path;

/** Passive account/server-isolated wallet history. It never requests a balance or records bets. */
public final class WalletHistoryService {
    private static WalletHistory history=new WalletHistory();private static WalletHistoryStore store;private static String loadError,writeError;private static boolean dirty;private static long savedAt;private static Ledger.Point last;
    private WalletHistoryService(){}
    public static void open(String scope,Path folder,List<Ledger.Point> existing){close();if(scope==null)return;store=new WalletHistoryStore(folder,scope);try{history=store.load(System.currentTimeMillis());history.merge(existing,System.currentTimeMillis());dirty=!existing.isEmpty();last=existing.isEmpty()?null:existing.getLast();flush();}catch(Exception ex){loadError=ex.getMessage();org.slf4j.LoggerFactory.getLogger("WalletHistory").warn("Wallet history retained without overwriting",ex);}}
    public static void capture(List<Ledger.Point> readings,long now){if(store==null||loadError!=null||readings.isEmpty())return;var latest=readings.getLast();if(!latest.equals(last)){history.merge(List.of(latest),now);last=latest;dirty=true;}if(now-savedAt>=30_000)flush();}
    public static void flush(){if(store==null||loadError!=null||!dirty)return;try{store.save(history);dirty=false;writeError=null;savedAt=System.currentTimeMillis();}catch(Exception ex){writeError="Wallet history could not be saved";savedAt=System.currentTimeMillis();org.slf4j.LoggerFactory.getLogger("WalletHistory").warn(writeError,ex);}}
    public static void close(){flush();store=null;history=new WalletHistory();loadError=writeError=null;dirty=false;savedAt=0;last=null;}
    public static List<Ledger.Point> points(Ledger ledger){if(store==null||loadError!=null)return ledger.balances();var result=new ArrayList<>(history.points());if(!ledger.balances().isEmpty()){var latest=ledger.balances().getLast();if(!result.contains(latest))result.add(latest);}return result;}
    public static String error(){return loadError!=null?loadError:writeError;}
}
