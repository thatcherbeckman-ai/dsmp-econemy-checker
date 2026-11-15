package dev.dsmp.economy.ui;

import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.core.Money;
import net.minecraft.*;

/** Client-only world entities: vanilla dropped-item rendering and a billboard label. */
public final class AuctionHologram {
    private static class_638 world;
    private static VisualItem item;
    private static class_8113.class_8123 label;
    private static AuctionAnchor anchor;
    private static String itemId,lastText;
    private static long lotStart;
    private static int quantity,nextId=Integer.MIN_VALUE+16_384;
    /** Client glow reads flag 6, while vanilla setGlowing consults the existing client flag.
     * Keep the never-despawn counter separate from the renderer's bob/rotation clock. */
    private static final class VisualItem extends class_1542 {
        VisualItem(class_1937 world,double x,double y,double z,class_1799 stack){super(world,x,y,z,stack);}
        void auctionGlow(boolean value){method_5729(6,value);}
        @Override public int method_6985(){return field_6012;}
    }
    private AuctionHologram(){}
    public static AuctionAnchor capture(){
        var client=class_310.method_1551();
        if(client.field_1687==null||client.field_1724==null)throw new IllegalStateException("Join the world before placing an auction");
        return new AuctionAnchor(dimension(client.field_1687),client.field_1724.method_23317(),client.field_1724.method_23318(),client.field_1724.method_23321());
    }
    private static String dimension(class_638 world){return world.method_27983().method_29177().toString();}
    public static void clear(){
        if(world!=null){remove(item);remove(label);}
        world=null;item=null;label=null;anchor=null;itemId=null;lastText=null;
    }
    private static void remove(class_1297 entity){if(entity!=null&&world.method_8469(entity.method_5628())==entity)world.method_2945(entity.method_5628(),class_1297.class_5529.field_26999);}
    private static int localId(class_638 world){if(nextId>=-1)nextId=Integer.MIN_VALUE+16_384;while(world.method_8469(nextId)!=null){nextId++;if(nextId>=-1)nextId=Integer.MIN_VALUE+16_384;}return nextId++;}
    public static void tick(class_310 client){
        var round=AuctionService.current();
        if(round==null||!round.visible()||round.anchor()==null||client.field_1687==null||client.field_1724==null){clear();return;}
        var target=round.anchor();var player=client.field_1724;
        if(!target.nearby(dimension(client.field_1687),player.method_23317(),player.method_23318(),player.method_23321())){clear();return;}
        if(world!=client.field_1687||!target.equals(anchor)||round.startedAt()!=lotStart||!round.item().equals(itemId)||round.quantity()!=quantity||item==null||label==null||world.method_8469(item.method_5628())!=item||world.method_8469(label.method_5628())!=label){
            clear();AuctionItems.Entry selected;try{selected=AuctionItems.find(round.item());}catch(IllegalArgumentException ex){return;}
            world=client.field_1687;anchor=target;lotStart=round.startedAt();itemId=round.item();quantity=round.quantity();
            var stack=selected.stack().method_46651(quantity);
            item=new VisualItem(world,target.x(),target.y()+.08,target.z(),stack);
            item.method_5838(localId(world));item.method_5875(true);item.method_18800(0,0,0);item.method_6989();item.method_35190();world.method_53875(item);
            label=new class_8113.class_8123(class_1299.field_42457,world);label.method_5838(localId(world));label.method_5814(target.x(),target.y()+1.1,target.z());
            label.method_48847(class_8113.class_8114.field_42409);label.method_48908(400);label.method_48910(0x50000000);label.method_48912((byte)1);world.method_53875(label);
        }
        // Stop local physics/water drift. Vanilla still supplies the item's bob and rotation.
        item.method_18800(0,0,0);item.method_5814(target.x(),target.y()+.08,target.z());
        item.auctionGlow(round.state()==AuctionRound.State.RUNNING);
        String text=caption(round,AuctionItems.find(round.item()).name(),System.currentTimeMillis());
        if(!text.equals(lastText)){label.method_48911(class_2561.method_43470(text));lastText=text;}
    }
    public static String caption(AuctionRound round,String name,long now){
        String header="x"+round.quantity()+" "+name+" - "+(round.state()==AuctionRound.State.RUNNING?"("+round.countdown(now)+")":switch(round.state()){case ENDED->"Finished";case CANCELLED->"Cancelled";default->"Interrupted";});
        String detail=switch(round.state()){
            case RUNNING->round.leader().map(b->b.player()+" - "+Money.format(b.cents(),false)).orElse("No bids yet");
            case ENDED->round.leader().map(b->"Winner: "+b.player()+" - "+Money.format(b.cents(),false)).orElse("No bids received");
            case CANCELLED->"Auction cancelled";
            case INTERRUPTED->"Auction interrupted - review bids";
        };
        return header+"\n"+detail+(AuctionService.error()==null?"":"\nAuction storage error - check settings");
    }
}
