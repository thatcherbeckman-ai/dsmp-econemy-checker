package dev.dsmp.economy.ui;

import dev.dsmp.economy.EconomyTrackerClient;
import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.commands.AuctionAction;
import net.minecraft.*;
import java.util.*;

/** Explicit auction setup and searchable live-registry item picker. Draft edits never start a lot. */
public final class AuctionScreen extends class_437 {
    private final EconomyTrackerClient mod;
    private final class_437 parent;
    private String query="",appliedQuery="",selected="minecraft:elytra",quantity="2",seconds="120",notice="Payments add to each player's total bid. First to a tied total wins.";
    private int page;
    private final List<Control> controls=new ArrayList<>();
    private record Control(int x,int y,int w,int h,String label,Runnable action){}
    public AuctionScreen(EconomyTrackerClient mod,class_437 parent){super(class_2561.method_43470("Auctions"));this.mod=mod;this.parent=parent;var r=AuctionService.current();if(r!=null){selected=r.item();quantity=""+r.quantity();seconds=""+((r.endsAt()-r.startedAt())/1000);}}
    private int pickerWidth(){return Math.max(140,(field_22789-36)/2);}
    private int rows(){return Math.max(1,(field_22790-156)/24);}
    private List<AuctionItems.Entry> results(){return AuctionItems.search(appliedQuery);}
    private int pages(){return Math.max(1,(results().size()+rows()-1)/rows());}
    @Override protected void method_25426(){
        controls.clear();page=Math.min(page,pages()-1);int w=pickerWidth(),right=w+24;
        field(12,48,w,query,128,"Search any item / identifier",value->query=value);
        field(right,48,Math.max(44,(field_22789-right-20)/2),quantity,2,"Quantity",value->quantity=value);
        field(right,78,Math.max(76,field_22789-right-12),seconds,5,"Timer in seconds",value->seconds=value);
        int from=page*rows();var found=results();
        for(int i=0;i<rows()&&from+i<found.size();i++){var e=found.get(from+i);button(12,76+i*24,w,22,"    "+e.name(),()->{selected=e.id();method_41843();});}
        button(12,field_22790-68,44,20,"Prev",()->{page=Math.max(0,page-1);method_41843();});button(60,field_22790-68,44,20,"Next",()->{page=Math.min(pages()-1,page+1);method_41843();});button(108,field_22790-68,Math.max(44,w-96),20,"Search",()->{appliedQuery=query;page=0;method_41843();});
        int cw=Math.max(36,(field_22789-right-18)/2),y=field_22790-92;
        button(right,y,cw,20,"Start",()->act(()->{AuctionItems.find(selected);AuctionService.start(selected,Integer.parseInt(quantity.trim()),Integer.parseInt(seconds.trim()),System.currentTimeMillis());},"Auction started. Close settings to see the item overlay."));
        button(right+cw+6,y,cw,20,"End early",()->act(AuctionService::end,"Auction finished. Winner remains on screen."));
        button(right,y+24,cw,20,"Cancel lot",()->act(AuctionService::cancel,"Auction cancelled; bids retained for review."));
        button(right+cw+6,y+24,cw,20,"Hide result",()->act(AuctionService::hide,"Auction overlay hidden."));
        button(field_22789-70,12,58,20,"Done",this::method_25419);
    }
    private void field(int x,int y,int w,String value,int max,String placeholder,java.util.function.Consumer<String> setter){var field=new class_342(field_22793,x,y,w,20,class_2561.method_43470(placeholder));field.method_1880(max);field.method_1852(value);field.method_47404(class_2561.method_43470(placeholder));field.method_1863(setter);method_37063(field);}
    private void button(int x,int y,int w,int h,String label,Runnable action){controls.add(new Control(x,y,w,h,label,action));method_37063((class_364)class_4185.method_46430(class_2561.method_43470(label),b->action.run()).method_46434(x,y,w,h).method_46431());}
    private void act(Runnable action,String success){int result=((AuctionAction)(Object)mod).auctionAction(action);notice=result==1?success:"Could not update auction. See chat for details.";method_41843();}
    @Override public void method_25394(class_332 g,int mx,int my,float delta){
        AuctionService.tick(System.currentTimeMillis());FestiveUi.background(g,field_22789,field_22790,mod.config);FestiveUi.text(g,"AUCTIONS",12,17,0xFFE7EAF3,1.1f,true);
        int w=pickerWidth(),right=w+24;FestiveUi.text(g,"ITEM PICKER",12,35,0xFFB6A4DE,.7f,true);FestiveUi.text(g,"QUANTITY",right,35,0xFFB6A4DE,.7f,true);
        FestiveUi.text(g,"TIMER (seconds)",right,70,0xFFB6A4DE,.6f,true);
        String selectedName;try{selectedName=AuctionItems.find(selected).name();}catch(IllegalArgumentException ex){selectedName=selected;}
        FestiveUi.text(g,FestiveUi.fit("Selected: "+selectedName,field_22789-right-12,.75f),right,104,0xFFE5C6F2,.75f,true);
        var r=AuctionService.current();int previewTop=124,available=field_22790-92-previewTop;
        if(r!=null&&available>=85){AuctionHud.drawLot(g,r,(right+field_22789-12)/2,previewTop,Math.min(.75f,(field_22789-right-16)/350f),System.currentTimeMillis());}
        else if(r!=null&&available>=20)FestiveUi.text(g,r.state()+" / "+r.bids().size()+" bidders",right,previewTop,0xFFA7A7BC,.7f,false);
        if(r!=null){int bidY=previewTop+96;int count=Math.max(0,(field_22790-104-bidY)/15);var bids=r.ranking();for(int i=0;i<Math.min(count,bids.size());i++){var bid=bids.get(i);FestiveUi.text(g,FestiveUi.fit((i+1)+". "+bid.player()+"  "+dev.dsmp.economy.core.Money.format(bid.cents(),false),field_22789-right-12,.7f),right,bidY+i*15,0xFFBFC4D9,.7f,false);}}
        String status=AuctionService.error()!=null?AuctionService.error():notice;FestiveUi.text(g,FestiveUi.fit(status,field_22789-24,.65f),12,field_22790-39,0xFFE6B6CE,.65f,false);
        FestiveUi.text(g,FestiveUi.fit("Local display only. Deliver items/refunds yourself; pause Auto bets for refunds.",field_22789-24,.65f),12,field_22790-25,0xFFA7A7BC,.65f,false);
        super.method_25394(g,mx,my,delta);for(var c:controls)FestiveUi.control(g,c.x,c.y,c.w,c.h,c.label,c.label.equals("Start"),mx>=c.x&&mx<c.x+c.w&&my>=c.y&&my<c.y+c.h,mod.config.menuAccent);
        var found=results();int from=page*rows();for(int i=0;i<rows()&&from+i<found.size();i++){var e=found.get(from+i);g.method_51427(e.stack(),16,79+i*24);if(e.id().equals(selected))g.method_25294(12,76+i*24,14,98+i*24,0xFFFF65C3);}
        FestiveUi.text(g,"Page "+(page+1)+"/"+pages(),12,field_22790-46,0xFFA7A7BC,.6f,false);
    }
    @Override public void method_25419(){class_310.method_1551().method_1507(parent);}
    @Override public boolean method_25421(){return false;}
}
