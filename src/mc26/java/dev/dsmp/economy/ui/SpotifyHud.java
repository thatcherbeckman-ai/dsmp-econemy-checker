package dev.dsmp.economy.ui;

import dev.dsmp.economy.spotify.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Independent of server/chat tracking. The preview always identifies its sample content. */
public final class SpotifyHud {
    private SpotifyHud(){}
    public static void draw(GuiGraphicsExtractor g,int width,int height){
        var service=Spotify.service();var options=service.options();if(!options.enabled())return;
        var track=service.track();if(options.hideIdle()&&(track==null||!track.playing()))return;
        var bounds=SpotifyPlacement.bounds(options,width,height);
        g.pose().pushMatrix();g.pose().translate(bounds.x(),bounds.y());g.pose().scale(bounds.scale());
        try{card(g,options,track,service.connected()?"Open Spotify and play a song":options.source()==SpotifySource.DESKTOP?"Start desktop mode in the menu":"Connect Spotify in the menu",false);}finally{g.pose().popMatrix();}
    }
    static void card(GuiGraphicsExtractor g,SpotifyOptions options,SpotifyApi.Track track,String idle,boolean preview){
        int color=SpotifyOptions.COLORS[options.palette()];
        UiSurfaces.shadow(g,0,0,250,78);UiSurfaces.card(g,0,0,250,78,0xEB101619);UiSurfaces.cardOutline(g,0,0,250,78,0x603F6258);
        // Branded text attribution, with a neutral music motif rather than a substitute Spotify logo.
        FestiveUi.text(g,preview?"Spotify / SAMPLE PREVIEW":"Spotify",12,8,0xFF1ED760,.65f,true);
        FestiveUi.text(g,track==null?"IDLE":track.playing()?"PLAYING":"PAUSED",196,9,color,.5f,true);
        FestiveUi.textFit(g,track==null?"Nothing playing":track.title(),12,24,226,0xFFF0F5F3,.95f,true);
        FestiveUi.textFit(g,track==null?idle:track.artist(),12,41,226,0xFFB2C1BC,.7f,false);
        long progress=track==null?0:track.progressAt(System.currentTimeMillis()),duration=track==null?0:track.duration();
        UiSurfaces.fill(g,12,60,185,3,0xFF34443D);if(duration>0)UiSurfaces.fill(g,12,60,(int)(185*progress/duration),3,color);
        FestiveUi.text(g,track!=null&&duration==0?"Progress not supplied by app":time(progress)+" / "+time(duration),12,68,0xFF95AAA0,.5f,false);
        FestiveUi.text(g,"NOW PLAYING",194,66,0xFF95AAA0,.45f,true);
    }
    private static String time(long ms){long s=Math.max(0,ms)/1000;return (s/60)+":"+String.format(java.util.Locale.ROOT,"%02d",s%60);}
}
