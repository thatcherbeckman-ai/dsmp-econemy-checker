package dev.dsmp.economy.ui;

import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.core.Money;


/** Settings-only lot preview. The in-world display is owned by AuctionHologram. */
public final class AuctionHud {
    private AuctionHud(){}
    public static void drawLot(net.minecraft.client.gui.GuiGraphicsExtractor g,AuctionRound round,int center,int top,float scale,long now){
        AuctionItems.Entry item;try{item=AuctionItems.find(round.item());}catch(IllegalArgumentException ex){centered(g,"Auction item unavailable",center,top,0xFFE57885,.85f);return;}
        var pose=g.pose();pose.pushMatrix();pose.translate(center,top);pose.scale(scale);
        try{
            String timer=round.state()==AuctionRound.State.RUNNING?"("+round.countdown(now)+")":switch(round.state()){case ENDED->"Finished";case CANCELLED->"Cancelled";default->"Interrupted";};
            String prefix="x"+round.quantity()+" ",name=item.name(),suffix=" - "+timer;
            int pw=MenuText.width(prefix),nw=MenuText.width(name,true),sw=MenuText.width(suffix),left=-(pw+nw+sw)/2;
            pose.pushMatrix();pose.scale(Math.min(1,330f/Math.max(1,pw+nw+sw)));try{FestiveUi.text(g,prefix,left,0,0xFFFFFFFF,1,false);FestiveUi.text(g,name,left+pw,0,0xFFFF49B9,1,true);FestiveUi.text(g,suffix,left+pw+nw,0,0xFFFFFFFF,1,false);}finally{pose.popMatrix();}
            var leader=round.leader();String detail;
            int color=0xFFE8ECF5;
            if(round.state()==AuctionRound.State.RUNNING)detail=leader.map(b->b.player()+" - "+Money.format(b.cents(),false)).orElse("No bids yet");
            else if(round.state()==AuctionRound.State.ENDED){detail=leader.map(b->"Winner: "+b.player()+" - "+Money.format(b.cents(),false)).orElse("No bids received");color=0xFF63D9A0;}
            else{detail=round.state()==AuctionRound.State.CANCELLED?"Auction cancelled":"Auction interrupted - review bids";color=0xFFE5AB78;}
            centered(g,detail,0,21,color,.9f);
            int icons=round.quantity()==2?2:1,size=54,gap=18,total=icons*size+(icons-1)*gap;
            for(int i=0;i<icons;i++){pose.pushMatrix();pose.translate(-total/2f+i*(size+gap),48);pose.scale(size/16f);try{g.item(item.stack(),0,0);}finally{pose.popMatrix();}}
            if(AuctionService.error()!=null)centered(g,"Auction storage error - check settings",0,112,0xFFE57885,.7f);
        }finally{pose.popMatrix();}
    }
    private static void centered(net.minecraft.client.gui.GuiGraphicsExtractor g,String text,int center,int y,int color,float scale){float fitted=Math.min(scale,330f/Math.max(1,MenuText.width(text)));FestiveUi.text(g,text,center-(int)(MenuText.width(text)*fitted/2),y,color,fitted,false);}
}
