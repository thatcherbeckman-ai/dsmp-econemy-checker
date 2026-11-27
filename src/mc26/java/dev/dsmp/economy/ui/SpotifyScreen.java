package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.spotify.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.net.URI;

/** Browser sign-in, explicit app setup, and an isolated overlay appearance draft. */
public final class SpotifyScreen extends WindowedScreen {
    private final EconomyTrackerClient mod;private final Screen parent;
    private SpotifyOptions draft=Spotify.service().options();private String clientId=draft.clientId();private int page;
    private String notice="Done saves your overlay. Connecting saves the Client ID. Login lasts until Minecraft closes.";
    private SettingsEditBox id;
    public SpotifyScreen(EconomyTrackerClient mod,Screen parent){super(Component.literal("Spotify"));this.mod=mod;this.parent=parent;}
    private void change(Runnable action){action.run();rebuildWidgets();}
    private void button(MenuLayout m,int row,int col,int columns,String text,boolean selected,Runnable action){addRenderableWidget(new SettingsButton(m.x(col,columns),m.row(row),m.cellWidth(col,columns),m.buttonHeight(),text,selected,mod.config.menuAccent,action));}
    private void options(boolean enabled,boolean hideIdle,int position,int scale,int palette){draft=new SpotifyOptions(draft.clientId(),enabled,hideIdle,position,scale,palette);}
    @Override protected void initMenu(){
        var m=MenuLayout.of(width,height);addRenderableWidget(new SettingsButton(m.doneX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",true,mod.config.menuAccent,this::onClose));addRenderableWidget(new SettingsButton(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Cancel",false,mod.config.menuAccent,()->MenuNavigation.open(parent)));
        button(m,0,0,2,"Account",page==0,()->change(()->page=0));button(m,0,1,2,"Overlay",page==1,()->change(()->page=1));
        if(page==0){
            id=new SettingsEditBox(font,m.margin(),m.row(2),m.contentWidth(),m.buttonHeight(),Component.literal("Spotify Client ID"));id.setMaxLength(32);id.setHint(Component.literal("Paste your Spotify developer Client ID"));id.setValue(clientId);id.setResponder(s->clientId=s);addRenderableWidget(id);
            button(m,3,0,2,"Connect Spotify",true,this::connect);
            button(m,3,1,2,"Disconnect / cancel",false,()->{Spotify.service().disconnect();rebuildWidgets();});
            button(m,4,0,3,"Developer dashboard",false,()->open("https://developer.spotify.com/dashboard"));
            button(m,4,1,3,"Copy redirect URI",false,()->{minecraft.keyboardHandler.setClipboard(SpotifyOAuth.REDIRECT);notice="Redirect URI copied. Add it exactly to the Spotify app settings.";});
            button(m,4,2,3,"App permissions",false,()->open("https://www.spotify.com/account/apps/"));
        }else{
            button(m,1,0,2,"Overlay: "+(draft.enabled()?"on":"off"),draft.enabled(),()->change(()->options(!draft.enabled(),draft.hideIdle(),draft.position(),draft.scale(),draft.palette())));
            button(m,1,1,2,"Hide when idle: "+(draft.hideIdle()?"yes":"no"),draft.hideIdle(),()->change(()->options(draft.enabled(),!draft.hideIdle(),draft.position(),draft.scale(),draft.palette())));
            button(m,2,0,3,"Smaller",false,()->change(()->options(draft.enabled(),draft.hideIdle(),draft.position(),draft.scale()-10,draft.palette())));
            button(m,2,1,3,"Size: "+draft.scale()+"%",true,()->change(()->options(draft.enabled(),draft.hideIdle(),draft.position(),100,draft.palette())));
            button(m,2,2,3,"Larger",false,()->change(()->options(draft.enabled(),draft.hideIdle(),draft.position(),draft.scale()+10,draft.palette())));
            for(int i=0;i<9;i++){int position=i;String[] rows={"Top","Middle","Bottom"},cols={"left","center","right"};button(m,3+i/3,i%3,3,rows[i/3]+" "+cols[i%3],draft.position()==i,()->change(()->options(draft.enabled(),draft.hideIdle(),position,draft.scale(),draft.palette())));}
            button(m,6,0,2,"Color: "+SpotifyOptions.PALETTES[draft.palette()],true,()->change(()->options(draft.enabled(),draft.hideIdle(),draft.position(),draft.scale(),(draft.palette()+1)%SpotifyOptions.PALETTES.length)));
            button(m,6,1,2,"Open current song",false,()->{var t=Spotify.service().track();if(t!=null&&!t.url().isEmpty())open(t.url());else notice="Play a song on Spotify first.";});
        }
    }
    private void open(String url){com.mojang.blaze3d.Blaze3D.openUri(URI.create(url));}
    private void connect(){try{var value=new SpotifyOptions(clientId,true,draft.hideIdle(),draft.position(),draft.scale(),draft.palette());Spotify.service().save(value);draft=value;URI uri=Spotify.service().connect();open(uri.toString());notice="Approve in Spotify's browser page. No password or client secret belongs in this menu.";rebuildWidgets();}catch(Exception ex){notice=ex instanceof IllegalArgumentException?ex.getMessage():"Could not connect. Check the Client ID and that port 43821 is available.";}}
    @Override protected void drawMenu(GuiGraphicsExtractor g,int mx,int my,float delta){
        var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,"Spotify / "+(page==0?"Account":"Overlay"));
        if(page==0){
            FestiveUi.textFit(g,"SPOTIFY CLIENT ID (PUBLIC APP ID)",m.margin(),m.row(1)+4,m.contentWidth(),0xFFB9CDC3,.75f,true);
            int y=m.row(5)+4;String[] lines={Spotify.service().status(),"Create an app in Spotify's developer dashboard. Select Web API.","Add this exact redirect URI: "+SpotifyOAuth.REDIRECT,"Copy its Client ID here, then Connect. Never enter the Client Secret.","Your account must have access to that app under Spotify's developer rules.","Read-only song details. Tokens stay in memory; reconnect after restarting."};
            for(int i=0;i<lines.length;i++)FestiveUi.textFit(g,lines[i],m.margin(),y+i*14,m.contentWidth(),i==0?0xFF73DDA2:0xFFACBBAF,.7f,i==0);
        }else{
            int y=m.row(7)+6;float scale=Math.min(1,(height-y-30)/78f);if(scale>=.5f){g.pose().pushMatrix();g.pose().translate(m.margin(),y);g.pose().scale(scale);try{SpotifyHud.card(g,draft,Spotify.service().track()==null?new SpotifyApi.Track("Your song title","Artist name","",true,42_000,180_000,System.currentTimeMillis()):Spotify.service().track(),"",Spotify.service().track()==null);}finally{g.pose().popMatrix();}}
        }
        m.footer(g,notice);super.drawMenu(g,mx,my,delta);
    }
    @Override public void onClose(){try{draft=new SpotifyOptions(clientId,draft.enabled(),draft.hideIdle(),draft.position(),draft.scale(),draft.palette());Spotify.service().save(draft);MenuNavigation.open(parent);}catch(Exception ex){notice=ex instanceof IllegalArgumentException?ex.getMessage():"Could not save Spotify preferences. Please retry.";}}
    @Override public boolean isPauseScreen(){return false;}
}
