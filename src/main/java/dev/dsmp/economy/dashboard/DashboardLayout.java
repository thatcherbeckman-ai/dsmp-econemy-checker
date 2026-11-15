package dev.dsmp.economy.dashboard;

public record DashboardLayout(int width,int height,int sidebar,int left,int contentWidth,int rowHeight) {
    public static DashboardLayout of(int width,int height){int side=Math.clamp(width/5,76,144),left=side+12;return new DashboardLayout(width,height,side,left,Math.max(180,width-left-12),Math.clamp((height-86)/6,25,46));}
    public int navStep(){return Math.clamp((height-70)/9,17,26);}
    public int navY(int index){return 58+index*navStep();}
    public int rowY(int index){return 58+index*rowHeight;}
    public int loanRows(){return Math.max(1,(height-58-152)/15);}
    public record Rect(int x,int y,int width,int height){public boolean contains(double px,double py){return px>=x&&px<x+width&&py>=y&&py<y+height;}}
}
