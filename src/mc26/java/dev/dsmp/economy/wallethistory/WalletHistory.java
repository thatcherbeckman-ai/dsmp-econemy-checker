package dev.dsmp.economy.wallethistory;

import dev.dsmp.economy.core.Ledger;
import java.util.*;

/** Minute readings for 48 hours and hourly readings for a year, plus a prior anchor. */
public final class WalletHistory {
    public static final int MAX_POINTS=12_000;private static final long DAY=86_400_000L;
    private List<Ledger.Point> points=List.of();
    public List<Ledger.Point> points(){return points;}
    public void merge(List<Ledger.Point> added,long now){
        if(now<0)throw new IllegalArgumentException("Invalid history time");var all=new ArrayList<Ledger.Point>(points);all.addAll(added);all.sort(Comparator.comparingLong(Ledger.Point::time));
        var buckets=new TreeMap<Long,Ledger.Point>();Ledger.Point anchor=null;long cutoff=Math.max(0,now-367*DAY),recent=Math.max(0,now-2*DAY);
        for(var point:all){if(point.time()<0)throw new IllegalArgumentException("Invalid balance timestamp");if(point.time()>now+300_000)continue;if(point.time()<cutoff){anchor=point;continue;}long unit=point.time()>=recent?60_000:3_600_000;buckets.put(point.time()/unit*unit,point);}
        var result=new ArrayList<Ledger.Point>();if(anchor!=null)result.add(anchor);else if(!all.isEmpty()&&!buckets.isEmpty()&&all.getFirst().time()<buckets.firstEntry().getValue().time())result.add(all.getFirst());result.addAll(buckets.values());if(result.size()>MAX_POINTS)result=new ArrayList<>(result.subList(result.size()-MAX_POINTS,result.size()));points=List.copyOf(result);
    }
    public static WalletHistory restore(List<Ledger.Point> points,long now){if(points==null||points.size()>MAX_POINTS)throw new IllegalArgumentException("Invalid wallet history size");var history=new WalletHistory();history.merge(points,now);return history;}
}
