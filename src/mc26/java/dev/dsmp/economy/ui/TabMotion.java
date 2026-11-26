package dev.dsmp.economy.ui;

/** A bounded 180 ms ease-out slide. A new selection restarts from a short fixed distance. */
public final class TabMotion {
    public static final long DURATION=180_000_000L;
    private long started=Long.MIN_VALUE;private int direction=1;
    public void start(long now,int direction){started=now;this.direction=direction<0?-1:1;}
    public float offset(long now,boolean reduced){
        if(reduced||started==Long.MIN_VALUE||now<started)return 0;
        double t=Math.clamp((now-started)/(double)DURATION,0,1);
        if(t>=1)return 0;
        return (float)(direction*28*Math.pow(1-t,3));
    }
}
