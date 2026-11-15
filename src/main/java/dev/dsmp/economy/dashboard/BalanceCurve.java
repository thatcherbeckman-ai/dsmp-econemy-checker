package dev.dsmp.economy.dashboard;

import dev.dsmp.economy.core.Ledger;
import java.util.*;

/** Shape-preserving interpolation between observed balances, without adding a zero origin. */
public final class BalanceCurve {
    public record Sample(double x,double cents){}
    public record Plot(List<Sample> samples,double min,double max){}
    private static List<Ledger.Point> cached=List.of();private static Plot plot=new Plot(List.of(),0,0);
    private BalanceCurve(){}
    public static Plot of(List<Ledger.Point> points){if(!points.equals(cached)){cached=List.copyOf(points);plot=build(points);}return plot;}
    public static Plot build(List<Ledger.Point> points){
        if(points.isEmpty())return new Plot(List.of(),0,0);
        if(points.size()==1){double value=points.getFirst().cents();return new Plot(List.of(new Sample(.5,value)),value,value);}
        int n=points.size();double[] y=new double[n],slope=new double[n-1],tangent=new double[n];double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
        for(int i=0;i<n;i++){y[i]=points.get(i).cents();min=Math.min(min,y[i]);max=Math.max(max,y[i]);if(i>0)slope[i-1]=y[i]-y[i-1];}
        tangent[0]=slope[0];tangent[n-1]=slope[n-2];
        for(int i=1;i<n-1;i++)tangent[i]=slope[i-1]*slope[i]<=0?0:2*slope[i-1]*slope[i]/(slope[i-1]+slope[i]);
        var samples=new ArrayList<Sample>();samples.add(new Sample(0,y[0]));int steps=Math.max(4,512/(n-1));
        for(int i=0;i<n-1;i++)for(int j=1;j<=steps;j++){double t=j/(double)steps,t2=t*t,t3=t2*t;double value=(2*t3-3*t2+1)*y[i]+(t3-2*t2+t)*tangent[i]+(-2*t3+3*t2)*y[i+1]+(t3-t2)*tangent[i+1];samples.add(new Sample((i+t)/(n-1),Math.clamp(value,Math.min(y[i],y[i+1]),Math.max(y[i],y[i+1]))));}
        return new Plot(List.copyOf(samples),min,max);
    }
}
