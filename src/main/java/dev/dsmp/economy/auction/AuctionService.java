package dev.dsmp.economy.auction;

import java.nio.file.Path;

/** Client-thread only. Bids use confirmed receipts and are saved separately from bets. */
public final class AuctionService {
    private static AuctionRound round;
    private static AuctionStore store;
    private static String error;
    private AuctionService(){}
    public static void close(){
        if(round!=null&&round.state()==AuctionRound.State.RUNNING&&error==null)try{commit(round.withState(AuctionRound.State.INTERRUPTED));}catch(IllegalStateException ignored){}
        round=null;store=null;error=null;
    }
    public static void open(String scope,Path directory){
        close();if(scope==null)return;
        try{store=new AuctionStore(directory,scope);round=store.load();if(round!=null&&round.state()==AuctionRound.State.RUNNING)commit(round.withState(AuctionRound.State.INTERRUPTED));}
        catch(Exception ex){error="Auction file unreadable; original preserved. Reconnect after fixing the file.";}
    }
    public static String error(){return error;}
    public static AuctionRound current(){return round;}
    private static void ready(){if(error!=null)throw new IllegalStateException(error);if(store==null)throw new IllegalStateException("Join the configured server first");}
    private static void commit(AuctionRound next){
        try{store.save(next);round=next;}catch(Exception ex){error="Auction save failed. Bidding stopped; reconnect after fixing storage.";if(round!=null&&round.state()==AuctionRound.State.RUNNING)round=round.withState(AuctionRound.State.INTERRUPTED);throw new IllegalStateException(error,ex);}
    }
    public static void start(String item,int quantity,int seconds,long now){start(item,quantity,seconds,now,null);}
    public static void start(String item,int quantity,int seconds,long now,AuctionAnchor anchor){ready();if(round!=null&&round.state()==AuctionRound.State.RUNNING)throw new IllegalStateException("An auction is already running");var next=AuctionRound.start(item,quantity,seconds,now);commit(anchor==null?next:next.at(anchor));}
    public static void end(){ready();if(round==null||round.state()!=AuctionRound.State.RUNNING)throw new IllegalStateException("No running auction");commit(round.withState(AuctionRound.State.ENDED));}
    public static void cancel(){ready();if(round==null||round.state()!=AuctionRound.State.RUNNING)throw new IllegalStateException("No running auction");commit(round.withState(AuctionRound.State.CANCELLED));}
    public static void hide(){ready();if(round!=null)commit(round.hide());}
    public static void tick(long now){if(error==null&&round!=null&&round.state()==AuctionRound.State.RUNNING){var next=round.advance(now);if(next!=round)try{commit(next);}catch(IllegalStateException ignored){}}}
    /** True reserves this receipt for the auction, including a receipt whose save failed. */
    public static boolean receive(String player,long cents,long now){
        if(error!=null)return round!=null&&round.state()==AuctionRound.State.INTERRUPTED&&now>=round.startedAt()&&now<round.endsAt();
        if(round==null||!round.accepts(now)){tick(now);return false;}
        try{commit(round.receive(player,cents,now));}catch(RuntimeException ex){if(error==null){error="Auction bid could not be recorded. Auction interrupted.";round=round.withState(AuctionRound.State.INTERRUPTED);}}
        return true;
    }
}
