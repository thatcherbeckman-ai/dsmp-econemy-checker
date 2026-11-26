package dev.dsmp.economy.menubackground;

/** Matching geometry for a standing model and its projected platform, fitted to the viewport. */
public record CharacterPlacement(int centerX,int feetY,int bodyHeight,int platformSize) {
    public static CharacterPlacement of(CharacterComposition options,int w,int h){
        double ratio=options.platform==CharacterComposition.Platform.NONE?0:.65*options.platformSize/100d;
        int body=Math.max(1,Math.min((int)Math.round(h*options.size/100d),(int)((h-12)/(1+ratio*.75))));
        int platform=(int)Math.round(body*ratio),half=Math.max((int)(body*.4),platform/2)+6;
        int cx=Math.clamp((int)Math.round(w*options.x/100d),Math.min(w/2,half),Math.max(w/2,w-half));
        int minimum=Math.min(h-6,body+6),maximum=Math.max(minimum,h-(int)(platform*.75)-6);
        int feet=Math.clamp((int)Math.round(h*options.ground/100d),minimum,maximum);
        return new CharacterPlacement(cx,feet,body,platform);
    }
}
