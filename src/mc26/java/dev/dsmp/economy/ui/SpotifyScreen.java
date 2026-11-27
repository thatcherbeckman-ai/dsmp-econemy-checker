package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.spotify.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.net.URI;

/** Local Windows desktop metadata, optional Web API sign-in, and an isolated overlay draft. */
public final class SpotifyScreen extends WindowedScreen {
    private final EconomyTrackerClient mod;private final Screen parent;
    private SpotifyOptions draft=Spotify.service().options();private String clientId=draft.clientId();private int page;
    private String notice="Desktop app needs no developer account. Done saves appearance; Start/Stop applies immediately.";
    private SettingsEditBox id;
    public SpotifyScreen(EconomyTrackerClient mod,Screen parent){super(Component.literal("Spotify"));this.mod=mod;this.parent=parent;}
    private void change(Runnable action){action.run();rebuildWidgets();}
    private void button(MenuLayout m,int row,int col,int columns,String text,boolean selected,Runnable action){addRenderableWidget(new SettingsButton(m.x(col,columns),m.row(row),m.cellWidth(col,columns),m.buttonHeight(),text,selected,mod.config.menuAccent,action));}
    private void options(boolean enabled,boolean hideIdle,int position,int scale,int palette){var old=draft;draft=draft.appearance(enabled,hideIdle,scale,palette);if(old.freePosition()&&old.scale()!=draft.scale()){int w=minecraft.getWindow().getGuiScaledWidth(),h=minecraft.getWindow().getGuiScaledHeight();var b=SpotifyPlacement.bounds(old,w,h);draft=SpotifyPlacement.atPixels(draft,b.x(),b.y(),w,h);}}
    @Override protected void initMenu(){
        var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,()->MenuNavigation.open(parent)));
        button(m,0,0,3,"Desktop app",page==0,()->change(()->page=0));button(m,0,1,3,"Account (advanced)",page==1,()->change(()->page=1));button(m,0,2,3,"Overlay",page==2,()->change(()->page=2));
        if(page==0){
            button(m,2,0,2,"Use desktop app",true,this::startDesktop);
            button(m,2,1,2,"Stop Spotify reader",false,this::stopDesktop);
            button(m,3,0,2,"Open Spotify app",false,()->open("spotify:home"));
            button(m,3,1,2,"Overlay settings",false,()->change(()->page=2));
        }else if(page==1){
            id=new SettingsEditBox(font,m.margin(),m.row(2),m.contentWidth(),m.buttonHeight(),Component.literal("Spotify Client ID"));id.setMaxLength(32);id.setHint(Component.literal("Paste your Spotify developer Client ID"));id.setValue(clientId);id.setResponder(s->clientId=s);addRenderableWidget(id);
            button(m,3,0,2,"Connect Spotify",true,this::connect);
            button(m,3,1,2,"Disconnect / cancel",false,()->{Spotify.service().disconnect();rebuildWidgets();});
            button(m,4,0,3,"Developer dashboard",false,()->open("https://developer.spotify.com/dashboard"));
            button(m,4,1,3,"Copy redirect URI",false,()->{minecraft.keyboardHandler.setClipboard(SpotifyOAuth.REDIRECT);notice="Redirect URI copied. Add it exactly to the Spotify app settings.";});
            button(m,4,2,3,"App permissions",false,()->open("https://www.spotify.com/account/apps/"));
        }else{
            button(m,1,0,2,"Overlay: "+(draft.enabled()?"on":"off"),draft.enabled(),()->change(()->options(!draft.enabled(),draft.hideIdle(),draft.position(),draft.scale(),draft.palette())));
            button(m,1,1,2,"Hide when idle: "+(draft.hideIdle()?"yes":"no"),draft.hideIdle(),()->change(()->options(draft.enabled(),!draft.hideIdle(),draft.position(),draft.scale(),draft.palette())));
            button(m,2,0,3,"Smaller",false,()->change(()->options(draft.enabled(),draft.hideIdle(),draft.position(),draft.scale()-5,draft.palette())));
            button(m,2,1,3,"Size: "+draft.scale()+"%",true,()->change(()->options(draft.enabled(),draft.hideIdle(),draft.position(),100,draft.palette())));
            button(m,2,2,3,"Larger",false,()->change(()->options(draft.enabled(),draft.hideIdle(),draft.position(),draft.scale()+5,draft.palette())));
            for(int i=0;i<9;i++){int position=i;String[] rows={"Top","Middle","Bottom"},cols={"left","center","right"};button(m,3+i/3,i%3,3,rows[i/3]+" "+cols[i%3],!draft.freePosition()&&draft.position()==i,()->change(()->draft=draft.anchor(position)));}
            button(m,6,0,2,"Color: "+SpotifyOptions.PALETTES[draft.palette()],true,()->change(()->options(draft.enabled(),draft.hideIdle(),draft.position(),draft.scale(),(draft.palette()+1)%SpotifyOptions.PALETTES.length)));
            button(m,6,1,2,"Move / exact size",true,()->MenuNavigation.open(new SpotifyPlacementScreen(this,draft,mod.config.menuAccent,value->{draft=value;notice="Spotify placement saved. Size range: 25-150%.";})));
        }
    }
    private void open(String url){com.mojang.blaze3d.Blaze3D.openUri(URI.create(url));}
    private void startDesktop(){try{Spotify.service().save(draft.enabled(true).source(SpotifySource.DESKTOP));Spotify.service().startDesktop();draft=Spotify.service().options();clientId=draft.clientId();notice="Desktop mode started. Open Spotify and play music; no sign-in needed here.";rebuildWidgets();}catch(Exception ex){notice="Could not start/save desktop mode. Please retry.";}}
    private void stopDesktop(){try{Spotify.service().stopDesktop();draft=Spotify.service().options();notice="Spotify reader stopped. It will stay off after restarting Minecraft.";rebuildWidgets();}catch(Exception ex){draft=Spotify.service().options();notice="Reader stopped, but its preference could not be saved. Please retry.";}}
    private void connect(){try{var value=draft.clientId(clientId).enabled(true).source(SpotifySource.WEB_API);Spotify.service().save(value);draft=value;URI uri=Spotify.service().connect();open(uri.toString());notice="Approve in Spotify's browser page. No password or client secret belongs in this menu.";rebuildWidgets();}catch(Exception ex){notice=ex instanceof IllegalArgumentException?ex.getMessage():"Could not connect. Check the Client ID and that port 43821 is available.";}}
    @Override protected void drawMenu(GuiGraphicsExtractor g,int mx,int my,float delta){
        var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,"Spotify / "+(page==0?"Desktop app":page==1?"Account":"Overlay"));
        if(page==0){
            FestiveUi.textFit(g,"SPOTIFY DESKTOP / WINDOWS 10 & 11",m.margin(),m.row(1)+4,m.contentWidth(),0xFFB9CDC3,.75f,true);
            int y=m.row(4)+8;String[] lines={Spotify.service().status(),"Works with Spotify Free. No developer app, Client ID or browser sign-in.","1. Open the Spotify desktop app on this PC and play a song.","2. Click Use desktop app, then choose your position in Overlay settings.","Reads title, artist and playback state from local Windows media controls.","Progress appears when Spotify supplies timing. Windows desktop only.","Selected source: "+(draft.source()==SpotifySource.DESKTOP?"Desktop app":"Spotify Web API")};
            for(int i=0;i<lines.length;i++)FestiveUi.textFit(g,lines[i],m.margin(),y+i*14,m.contentWidth(),i==0?0xFF73DDA2:0xFFACBBAF,.7f,i==0);
        }else if(page==1){
            FestiveUi.textFit(g,"SPOTIFY CLIENT ID (PUBLIC APP ID)",m.margin(),m.row(1)+4,m.contentWidth(),0xFFB9CDC3,.75f,true);
            int y=m.row(5)+4;String[] lines={Spotify.service().status(),"Create an app in Spotify's developer dashboard. Select Web API.","Add this exact redirect URI: "+SpotifyOAuth.REDIRECT,"Copy its Client ID here, then Connect. Never enter the Client Secret.","App access and Spotify developer/Premium eligibility are required.","Read-only song details. Tokens stay in memory; reconnect after restarting."};
            for(int i=0;i<lines.length;i++)FestiveUi.textFit(g,lines[i],m.margin(),y+i*14,m.contentWidth(),i==0?0xFF73DDA2:0xFFACBBAF,.7f,i==0);
        }else{
            int y=m.row(7)+6;float scale=Math.min(draft.scale()/100f,Math.min(1,(height-y-30)/78f));if(scale>=.2f){g.pose().pushMatrix();g.pose().translate(m.margin(),y);g.pose().scale(scale);try{SpotifyHud.card(g,draft,Spotify.service().track()==null?new SpotifyApi.Track("Your song title","Artist name","",true,42_000,180_000,System.currentTimeMillis()):Spotify.service().track(),"",Spotify.service().track()==null);}finally{g.pose().popMatrix();}}
        }
        m.footer(g,notice);super.drawMenu(g,mx,my,delta);
    }
    @Override public void onClose(){try{draft=draft.clientId(draft.source()==SpotifySource.DESKTOP?draft.clientId():clientId);Spotify.service().save(draft);MenuNavigation.open(parent);}catch(Exception ex){notice=ex instanceof IllegalArgumentException?ex.getMessage():"Could not save Spotify preferences. Please retry.";}}
    @Override public boolean isPauseScreen(){return false;}
}
