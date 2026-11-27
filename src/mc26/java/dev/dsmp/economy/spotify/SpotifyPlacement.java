package dev.dsmp.economy.spotify;

/** One viewport calculation for the live HUD, exact pixel editing, dragging and resize. */
public final class SpotifyPlacement {
    public record Bounds(int x,int y,int width,int height,float scale){
        public boolean contains(double px,double py){return px>=x&&px<x+width&&py>=y&&py<y+height;}
    }
    private SpotifyPlacement(){}
    public static Bounds bounds(SpotifyOptions options,int width,int height){
        if(width<1||height<1)throw new IllegalArgumentException("Invalid viewport");
        float scale=Math.min(options.scale()/100f,Math.min(Math.max(1,width-12)/250f,Math.max(1,height-12)/78f));
        int w=Math.min(width,(int)Math.ceil(250*scale)),h=Math.min(height,(int)Math.ceil(78*scale));
        int availableX=width-w,availableY=height-h,col=options.position()%3,row=options.position()/3;
        int x=options.freePosition()?(int)Math.round(options.x()*availableX):col==0?6:col==1?availableX/2:availableX-6;
        int y=options.freePosition()?(int)Math.round(options.y()*availableY):row==0?6:row==1?availableY/2:availableY-6;
        return new Bounds(Math.clamp(x,0,availableX),Math.clamp(y,0,availableY),w,h,scale);
    }
    public static SpotifyOptions atPixels(SpotifyOptions options,double x,double y,int width,int height){
        var b=bounds(options,width,height);int dx=width-b.width(),dy=height-b.height();
        return options.at(dx==0?0:x/dx,dy==0?0:y/dy);
    }
    public static SpotifyOptions move(SpotifyOptions options,int dx,int dy,int width,int height){var b=bounds(options,width,height);return atPixels(options,(double)b.x()+dx,(double)b.y()+dy,width,height);}
    public static SpotifyOptions resize(SpotifyOptions options,int percent,int width,int height){
        var before=bounds(options,width,height);var next=options.size(percent);var after=bounds(next,width,height);
        return atPixels(next,before.x()+(before.width()-after.width())/2d,before.y()+(before.height()-after.height())/2d,width,height);
    }
}
