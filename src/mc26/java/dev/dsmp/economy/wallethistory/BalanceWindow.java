package dev.dsmp.economy.wallethistory;

import dev.dsmp.economy.core.Ledger;
import java.util.*;

/** Rolling windows use saved balance readings only; missing start coverage is marked partial. */
public record BalanceWindow(int days,long from,long until,List<Ledger.Point> points,OptionalLong change,boolean complete,long firstReading) {
    public static final List<Integer> DAYS=List.of(1,7,28,365);
    public static BalanceWindow of(List<Ledger.Point> readings,int days,long now){
        if(!DAYS.contains(days)||now<0)throw new IllegalArgumentException("Invalid balance range");long start=Math.max(0,now-days*86_400_000L);var sorted=new TreeMap<Long,Long>();for(var p:readings)if(p.time()>=0&&p.time()<=now)sorted.put(p.time(),p.cents());
        var anchor=sorted.floorEntry(start);var visible=new ArrayList<Ledger.Point>();for(var p:sorted.tailMap(start,true).entrySet())visible.add(new Ledger.Point(p.getKey(),p.getValue()));
        if(visible.isEmpty())return new BalanceWindow(days,start,now,List.of(),OptionalLong.empty(),false,sorted.isEmpty()?0:sorted.firstKey());
        if(anchor!=null&&anchor.getKey()<start)visible.addFirst(new Ledger.Point(anchor.getKey(),anchor.getValue()));
        OptionalLong delta=OptionalLong.empty();if(visible.size()>1)try{delta=OptionalLong.of(Math.subtractExact(visible.getLast().cents(),visible.getFirst().cents()));}catch(ArithmeticException ignored){}
        return new BalanceWindow(days,start,now,List.copyOf(visible),delta,anchor!=null,sorted.firstKey());
    }
    public double position(long time){return Math.clamp((time-from)/(double)Math.max(1,until-from),0,1);}
}
