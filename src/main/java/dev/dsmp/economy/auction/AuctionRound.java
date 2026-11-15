package dev.dsmp.economy.auction;

import java.util.*;

/** One immutable auction lot. All amounts are integer cents, never floating point. */
public record AuctionRound(int version,String item,int quantity,long startedAt,long endsAt,
                           State state,boolean visible,List<Bid> bids) {
    public enum State { RUNNING, ENDED, CANCELLED, INTERRUPTED }
    public record Bid(String player,long cents,long order) {}
    public AuctionRound {
        if(version!=1||item==null||!item.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||item.length()>256||item.equals("minecraft:air"))throw new IllegalArgumentException("Invalid auction item");
        if(quantity<1||quantity>64||startedAt<0||endsAt<=startedAt||endsAt-startedAt>86_400_000L||state==null||bids==null||bids.size()>10_000)throw new IllegalArgumentException("Invalid auction settings");
        var names=new HashSet<String>();var orders=new HashSet<Long>();
        for(var b:bids)if(b==null||b.player()==null||!b.player().matches("\\.?[A-Za-z0-9_]{1,16}")||b.cents()<=0||b.order()<1||!names.add(b.player().toLowerCase(Locale.ROOT))||!orders.add(b.order()))throw new IllegalArgumentException("Invalid auction bid");
        bids=List.copyOf(bids);
    }
    public static AuctionRound start(String item,int quantity,int seconds,long now){
        if(seconds<1||seconds>86400)throw new IllegalArgumentException("Timer must be 1 to 86400 seconds");
        return new AuctionRound(1,item,quantity,now,Math.addExact(now,seconds*1000L),State.RUNNING,true,List.of());
    }
    public boolean accepts(long now){return state==State.RUNNING&&now>=startedAt&&now<endsAt;}
    public AuctionRound advance(long now){return state==State.RUNNING&&now>=endsAt?withState(State.ENDED):this;}
    public AuctionRound withState(State next){return new AuctionRound(version,item,quantity,startedAt,endsAt,next,visible,bids);}
    public AuctionRound hide(){if(state==State.RUNNING)throw new IllegalStateException("End or cancel the auction before hiding it");return new AuctionRound(version,item,quantity,startedAt,endsAt,state,false,bids);}
    public AuctionRound receive(String player,long cents,long now){
        if(!accepts(now))return advance(now);
        if(cents<=0)throw new IllegalArgumentException("Bid must be positive");
        var next=new ArrayList<>(bids);long order=Math.addExact(bids.stream().mapToLong(Bid::order).max().orElse(0),1);
        for(int i=0;i<next.size();i++)if(next.get(i).player().equalsIgnoreCase(player)){
            var old=next.get(i);next.set(i,new Bid(old.player(),Math.addExact(old.cents(),cents),order));return new AuctionRound(version,item,quantity,startedAt,endsAt,state,visible,next);
        }
        next.add(new Bid(player,cents,order));return new AuctionRound(version,item,quantity,startedAt,endsAt,state,visible,next);
    }
    /** Ties go to the first player to reach that total. */
    public List<Bid> ranking(){return bids.stream().sorted(Comparator.comparingLong(Bid::cents).reversed().thenComparingLong(Bid::order)).toList();}
    public Optional<Bid> leader(){return bids.stream().min(Comparator.comparingLong(Bid::cents).reversed().thenComparingLong(Bid::order));}
    public String countdown(long now){long left=now>=endsAt?0:endsAt-Math.max(0,now);long seconds=left/1000+(left%1000==0?0:1);return String.format(Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);}
}
