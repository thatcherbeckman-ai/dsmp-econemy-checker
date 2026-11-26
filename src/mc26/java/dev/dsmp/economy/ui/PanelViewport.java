package dev.dsmp.economy.ui;

/** One invertible transform for a settings page inside the dashboard's right-hand panel. */
public record PanelViewport(int left,int top,int width,int height,int contentWidth,int contentHeight,float scale) {
    public static PanelViewport of(int left,int top,int width,int height){if(width<1||height<1)throw new IllegalArgumentException("Invalid panel");int cw=Math.max(640,width),ch=Math.max(360,height);return new PanelViewport(left,top,width,height,cw,ch,Math.min(width/(float)cw,height/(float)ch));}
    public double localX(double x){return (x-left)/scale;}
    public double localY(double y){return (y-top)/scale;}
    public double x(double local){return left+local*scale;}
    public double y(double local){return top+local*scale;}
    public boolean contains(double x,double y){return x>=left&&y>=top&&x<left+width&&y<top+height;}
}
