package dev.dsmp.economy.ui;

import dev.dsmp.economy.spotify.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Independent of server/chat tracking. The preview always identifies its sample content. */
public final class SpotifyHud {
    private SpotifyHud(){}
    public static void draw(GuiGraphicsExtractor g,int width,int height){
        var service=Spotify.service();var options=service.options();if(!options.enabled())return;
        var track=service.track();if(options.hideIdle()&&(track==null||!track.playing()))return;
        float scale=Math.min(options.scale()/100f,Math.min((width-12)/250f,(height-12)/78f));
        int w=Math.round(250*scale),h=Math.round(78*scale),col=options.position()%3,row=options.position()/3;
        int x=col==0?6:col==1?(width-w)/2:width-w-6,y=row==0?6:row==1?(height-h)/2:height-h-6;
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(scale);
        try{card(g,options,track,service.connected()?"Open Spotify and play a song":"Connect Spotify in the menu",false);}finally{g.pose().popMatrix();}
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
        FestiveUi.text(g,time(progress)+" / "+time(duration),12,68,0xFF95AAA0,.5f,false);
        FestiveUi.text(g,"NOW PLAYING",194,66,0xFF95AAA0,.45f,true);
    }
    private static String time(long ms){long s=Math.max(0,ms)/1000;return (s/60)+":"+String.format(java.util.Locale.ROOT,"%02d",s%60);}
}
