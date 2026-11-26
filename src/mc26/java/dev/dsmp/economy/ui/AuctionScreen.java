package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.commands.AuctionAction;

import java.util.*;

/** Explicit auction setup and searchable live-registry item picker. Draft edits never start a lot. */
public final class AuctionScreen extends WindowedScreen {
    private final EconomyTrackerClient mod;
    private final net.minecraft.client.gui.screens.Screen parent;
    private String query="",appliedQuery="",selected="minecraft:elytra",quantity="2",seconds="120",notice="Payments add to each player's total bid. First to a tied total wins.";
    private int page;
    public AuctionScreen(EconomyTrackerClient mod,net.minecraft.client.gui.screens.Screen parent){super(net.minecraft.network.chat.Component.literal(MenuLayout.BRAND+" / Auctions"));this.mod=mod;this.parent=parent;var r=AuctionService.current();if(r!=null){selected=r.item();quantity=""+r.quantity();seconds=""+((r.endsAt()-r.startedAt())/1000);}}
    private int pickerWidth(){var m=MenuLayout.of(width,height);return (m.contentWidth()-m.gap()*2)/2;}
    private int right(){var m=MenuLayout.of(width,height);return m.margin()+pickerWidth()+m.gap()*2;}
    private int listTop(){var m=MenuLayout.of(width,height);return m.top()+m.step()+8;}
    private int actionTop(){var m=MenuLayout.of(width,height);return height-58-m.buttonHeight()-m.step();}
    private int rows(){var m=MenuLayout.of(width,height);return Math.max(1,(actionTop()-listTop()-m.gap())/m.step());}
    private List<AuctionItems.Entry> results(){return AuctionItems.search(appliedQuery);}
    private int pages(){return Math.max(1,(results().size()+rows()-1)/rows());}
    @Override protected void initMenu(){
        page=Math.min(page,pages()-1);var m=MenuLayout.of(width,height);int w=pickerWidth(),right=right(),rh=width-m.margin()-right,cw=(rh-m.gap())/2;
        field(m.margin(),m.top()+6,w,query,128,"Search any item / identifier",value->query=value);
        field(right,m.top()+6,cw,quantity,2,"Quantity",value->quantity=value);
        field(right+cw+m.gap(),m.top()+6,cw,seconds,5,"Timer in seconds",value->seconds=value);
        int from=page*rows();var found=results();
        for(int i=0;i<rows()&&from+i<found.size();i++){var e=found.get(from+i);button(m.margin(),listTop()+i*m.step(),w,m.buttonHeight(),"    "+e.name(),()->{selected=e.id();rebuildWidgets();});}
        int nav=(w-m.gap()*2)/3,y=actionTop();
        button(m.margin(),y,nav,m.buttonHeight(),"Prev",()->{page=Math.max(0,page-1);rebuildWidgets();});button(m.margin()+nav+m.gap(),y,nav,m.buttonHeight(),"Next",()->{page=Math.min(pages()-1,page+1);rebuildWidgets();});button(m.margin()+(nav+m.gap())*2,y,nav,m.buttonHeight(),"Search",()->{appliedQuery=query;page=0;rebuildWidgets();});
        button(right,y,cw,m.buttonHeight(),"Start",()->act(()->{AuctionItems.find(selected);AuctionService.start(selected,Integer.parseInt(quantity.trim()),Integer.parseInt(seconds.trim()),System.currentTimeMillis(),AuctionHologram.capture());AuctionHologram.tick(net.minecraft.client.Minecraft.getInstance());},"Placed at your feet. Close settings and walk away to view."));
        button(right+cw+m.gap(),y,cw,m.buttonHeight(),"End early",()->act(AuctionService::end,"Auction finished. Winner stays above the item."));
        button(right,y+m.step(),cw,m.buttonHeight(),"Cancel lot",()->act(AuctionService::cancel,"Auction cancelled; bids retained for review."));
        button(right+cw+m.gap(),y+m.step(),cw,m.buttonHeight(),"Hide result",()->act(AuctionService::hide,"Auction hologram removed."));
        button(m.cancelX(),m.actionY(),m.actionWidth(),m.buttonHeight(),"Done",this::onClose);
    }
    private void field(int x,int y,int w,String value,int max,String placeholder,java.util.function.Consumer<String> setter){var field=new SettingsEditBox(font,x,y,w,MenuLayout.of(width,height).buttonHeight(),net.minecraft.network.chat.Component.literal(placeholder));field.setMaxLength(max);field.setValue(value);field.setHint(net.minecraft.network.chat.Component.literal(placeholder));field.setResponder(setter);addRenderableWidget(field);}
    private void button(int x,int y,int w,int h,String label,Runnable action){addRenderableWidget(new SettingsButton(x,y,w,h,label,label.equals("Start")||label.equals("Done"),mod.config.menuAccent,action));}
    private void act(Runnable action,String success){int result=((AuctionAction)(Object)mod).auctionAction(action);notice=result==1?success:"Could not update auction. See chat for details.";rebuildWidgets();}
    @Override protected void drawMenu(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta){
        AuctionService.tick(System.currentTimeMillis());var m=MenuLayout.of(width,height);FestiveUi.background(g,width,height,mod.config);m.header(g,"Auctions / item picker and live bids");
        int w=pickerWidth(),right=right(),rh=width-m.margin()-right,cw=(rh-m.gap())/2;
        FestiveUi.text(g,"ITEM PICKER",m.margin(),m.top()-5,0xFFB6A4DE,.65f,true);FestiveUi.text(g,"QUANTITY",right,m.top()-5,0xFFB6A4DE,.65f,true);FestiveUi.text(g,"SECONDS",right+cw+m.gap(),m.top()-5,0xFFB6A4DE,.65f,true);
        String selectedName;try{selectedName=AuctionItems.find(selected).name();}catch(IllegalArgumentException ex){selectedName=selected;}
        int infoTop=listTop()+8,previewTop=infoTop+24;
        FestiveUi.textFit(g,"Selected: "+selectedName,right,infoTop,rh,0xFFE5C6F2,.78f,true);
        var r=AuctionService.current();int available=actionTop()-previewTop-m.gap();
        if(r!=null&&available>=85)AuctionHud.drawLot(g,r,right+rh/2,previewTop,Math.min(.75f,rh/350f),System.currentTimeMillis());
        else if(r!=null&&available>=12)FestiveUi.textFit(g,r.state()+" / "+r.bids().size()+" bidders",right,previewTop,rh,0xFFA7A7BC,.7f,false);
        if(r!=null){int bidY=previewTop+100,count=Math.max(0,(actionTop()-m.gap()-bidY)/18);var bids=r.ranking();for(int i=0;i<Math.min(count,bids.size());i++){var bid=bids.get(i);FestiveUi.textFit(g,(i+1)+". "+bid.player()+"  "+dev.dsmp.economy.core.Money.format(bid.cents(),false),right,bidY+i*18,rh,0xFFBFC4D9,.75f,false);}}
        String status=AuctionService.error()!=null?AuctionService.error():notice;
        FestiveUi.textFit(g,status,m.margin(),height-34,m.contentWidth(),0xFFE6B6CE,.65f,false);
        FestiveUi.textFit(g,"Local display. Deliver items/refunds yourself; pause Auto bets for refunds.",m.margin(),height-18,m.contentWidth(),0xFFA7A7BC,.65f,false);
        super.drawMenu(g,mx,my,delta);
        var found=results();int from=page*rows();for(int i=0;i<rows()&&from+i<found.size();i++){var e=found.get(from+i);g.item(e.stack(),m.margin()+5,listTop()+i*m.step()+(m.buttonHeight()-16)/2);if(e.id().equals(selected))g.fill(m.margin(),listTop()+i*m.step()+3,m.margin()+2,listTop()+i*m.step()+m.buttonHeight()-3,0xFFFF65C3);}
        FestiveUi.text(g,"Page "+(page+1)+" / "+pages(),m.margin(),actionTop()+m.step()+7,0xFFA7A7BC,.7f,false);
    }
    @Override public void onClose(){net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
