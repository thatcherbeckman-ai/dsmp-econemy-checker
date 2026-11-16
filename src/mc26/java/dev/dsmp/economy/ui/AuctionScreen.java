package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.commands.AuctionAction;

import java.util.*;

/** Explicit auction setup and searchable live-registry item picker. Draft edits never start a lot. */
public final class AuctionScreen extends net.minecraft.client.gui.screens.Screen {
    private final EconomyTrackerClient mod;
    private final net.minecraft.client.gui.screens.Screen parent;
    private String query="",appliedQuery="",selected="minecraft:elytra",quantity="2",seconds="120",notice="Payments add to each player's total bid. First to a tied total wins.";
    private int page;
    private final List<Control> controls=new ArrayList<>();
    private record Control(int x,int y,int w,int h,String label,Runnable action){}
    public AuctionScreen(EconomyTrackerClient mod,net.minecraft.client.gui.screens.Screen parent){super(net.minecraft.network.chat.Component.literal("Auctions"));this.mod=mod;this.parent=parent;var r=AuctionService.current();if(r!=null){selected=r.item();quantity=""+r.quantity();seconds=""+((r.endsAt()-r.startedAt())/1000);}}
    private int pickerWidth(){return Math.max(140,(width-36)/2);}
    private int rows(){return Math.max(1,(height-156)/24);}
    private List<AuctionItems.Entry> results(){return AuctionItems.search(appliedQuery);}
    private int pages(){return Math.max(1,(results().size()+rows()-1)/rows());}
    @Override protected void init(){
        controls.clear();page=Math.min(page,pages()-1);int w=pickerWidth(),right=w+24;
        field(12,48,w,query,128,"Search any item / identifier",value->query=value);
        field(right,48,Math.max(44,(width-right-20)/2),quantity,2,"Quantity",value->quantity=value);
        field(right,78,Math.max(76,width-right-12),seconds,5,"Timer in seconds",value->seconds=value);
        int from=page*rows();var found=results();
        for(int i=0;i<rows()&&from+i<found.size();i++){var e=found.get(from+i);button(12,76+i*24,w,22,"    "+e.name(),()->{selected=e.id();rebuildWidgets();});}
        button(12,height-68,44,20,"Prev",()->{page=Math.max(0,page-1);rebuildWidgets();});button(60,height-68,44,20,"Next",()->{page=Math.min(pages()-1,page+1);rebuildWidgets();});button(108,height-68,Math.max(44,w-96),20,"Search",()->{appliedQuery=query;page=0;rebuildWidgets();});
        int cw=Math.max(36,(width-right-18)/2),y=height-92;
        button(right,y,cw,20,"Start",()->act(()->{AuctionItems.find(selected);AuctionService.start(selected,Integer.parseInt(quantity.trim()),Integer.parseInt(seconds.trim()),System.currentTimeMillis(),AuctionHologram.capture());AuctionHologram.tick(net.minecraft.client.Minecraft.getInstance());},"Placed at your feet. Close settings and walk away to view."));
        button(right+cw+6,y,cw,20,"End early",()->act(AuctionService::end,"Auction finished. Winner stays above the item."));
        button(right,y+24,cw,20,"Cancel lot",()->act(AuctionService::cancel,"Auction cancelled; bids retained for review."));
        button(right+cw+6,y+24,cw,20,"Hide result",()->act(AuctionService::hide,"Auction hologram removed."));
        button(width-70,12,58,20,"Done",this::onClose);
    }
    private void field(int x,int y,int w,String value,int max,String placeholder,java.util.function.Consumer<String> setter){var field=new net.minecraft.client.gui.components.EditBox(font,x,y,w,20,net.minecraft.network.chat.Component.literal(placeholder));field.setMaxLength(max);field.setValue(value);field.setHint(net.minecraft.network.chat.Component.literal(placeholder));field.setResponder(setter);addRenderableWidget(field);}
    private void button(int x,int y,int w,int h,String label,Runnable action){controls.add(new Control(x,y,w,h,label,action));addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.literal(label),b->action.run()).bounds(x,y,w,h).build());}
    private void act(Runnable action,String success){int result=((AuctionAction)(Object)mod).auctionAction(action);notice=result==1?success:"Could not update auction. See chat for details.";rebuildWidgets();}
    @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta){
        AuctionService.tick(System.currentTimeMillis());FestiveUi.background(g,width,height,mod.config);FestiveUi.text(g,"AUCTIONS",12,17,0xFFE7EAF3,1.1f,true);
        int w=pickerWidth(),right=w+24;FestiveUi.text(g,"ITEM PICKER",12,35,0xFFB6A4DE,.7f,true);FestiveUi.text(g,"QUANTITY",right,35,0xFFB6A4DE,.7f,true);
        FestiveUi.text(g,"TIMER (seconds)",right,70,0xFFB6A4DE,.6f,true);
        String selectedName;try{selectedName=AuctionItems.find(selected).name();}catch(IllegalArgumentException ex){selectedName=selected;}
        FestiveUi.text(g,FestiveUi.fit("Selected: "+selectedName,width-right-12,.75f),right,104,0xFFE5C6F2,.75f,true);
        var r=AuctionService.current();int previewTop=124,available=height-92-previewTop;
        if(r!=null&&available>=85){AuctionHud.drawLot(g,r,(right+width-12)/2,previewTop,Math.min(.75f,(width-right-16)/350f),System.currentTimeMillis());}
        else if(r!=null&&available>=20)FestiveUi.text(g,r.state()+" / "+r.bids().size()+" bidders",right,previewTop,0xFFA7A7BC,.7f,false);
        if(r!=null){int bidY=previewTop+96;int count=Math.max(0,(height-104-bidY)/15);var bids=r.ranking();for(int i=0;i<Math.min(count,bids.size());i++){var bid=bids.get(i);FestiveUi.text(g,FestiveUi.fit((i+1)+". "+bid.player()+"  "+dev.dsmp.economy.core.Money.format(bid.cents(),false),width-right-12,.7f),right,bidY+i*15,0xFFBFC4D9,.7f,false);}}
        String status=AuctionService.error()!=null?AuctionService.error():notice;FestiveUi.text(g,FestiveUi.fit(status,width-24,.65f),12,height-39,0xFFE6B6CE,.65f,false);
        FestiveUi.text(g,FestiveUi.fit("Local display only. Deliver items/refunds yourself; pause Auto bets for refunds.",width-24,.65f),12,height-25,0xFFA7A7BC,.65f,false);
        super.extractRenderState(g,mx,my,delta);for(var c:controls)FestiveUi.control(g,c.x,c.y,c.w,c.h,c.label,c.label.equals("Start"),mx>=c.x&&mx<c.x+c.w&&my>=c.y&&my<c.y+c.h,mod.config.menuAccent);
        var found=results();int from=page*rows();for(int i=0;i<rows()&&from+i<found.size();i++){var e=found.get(from+i);g.item(e.stack(),16,79+i*24);if(e.id().equals(selected))g.fill(12,76+i*24,14,98+i*24,0xFFFF65C3);}
        FestiveUi.text(g,"Page "+(page+1)+"/"+pages(),12,height-46,0xFFA7A7BC,.6f,false);
    }
    @Override public void onClose(){net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
