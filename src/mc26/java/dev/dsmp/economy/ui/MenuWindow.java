package dev.dsmp.economy.ui;

/** A fixed coordinate space scaled into a centered window, leaving the world visible. */
public record MenuWindow(int viewportWidth,int viewportHeight,int width,int height,float scale,float x,float y) {
    public static MenuWindow of(int viewportWidth,int viewportHeight){
        if(viewportWidth<1||viewportHeight<1)throw new IllegalArgumentException("Invalid viewport");
        int width=Math.clamp(Math.round(viewportWidth*.86f),640,960);
        int height=Math.clamp(Math.round(width*.56f),360,520);
        float scale=Math.min(1,Math.min(viewportWidth*.88f/width,viewportHeight*.82f/height));
        return new MenuWindow(viewportWidth,viewportHeight,width,height,scale,(viewportWidth-width*scale)/2,(viewportHeight-height*scale)/2);
    }
    public double localX(double screenX){return (screenX-x)/scale;}
    public double localY(double screenY){return (screenY-y)/scale;}
    public double screenX(double localX){return x+localX*scale;}
    public double screenY(double localY){return y+localY*scale;}
    public boolean contains(double screenX,double screenY){return screenX>=x&&screenY>=y&&screenX<x+width*scale&&screenY<y+height*scale;}
}
