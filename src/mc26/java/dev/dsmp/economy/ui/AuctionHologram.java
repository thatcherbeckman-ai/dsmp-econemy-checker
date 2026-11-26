package dev.dsmp.economy.ui;

import dev.dsmp.economy.auction.*;
import dev.dsmp.economy.core.Money;


/** Client-only world entities: vanilla dropped-item rendering and a billboard label. */
public final class AuctionHologram {
    private static net.minecraft.client.multiplayer.ClientLevel world;
    private static VisualItem item;
    private static net.minecraft.world.entity.Display.TextDisplay label;
    private static AuctionAnchor anchor;
    private static String itemId,lastText;
    private static long lotStart;
    private static int quantity,nextId=Integer.MIN_VALUE+16_384;
    /** Client glow reads flag 6, while vanilla setGlowing consults the existing client flag.
     * Keep the never-despawn counter separate from the renderer's bob/rotation clock. */
    private static final class VisualItem extends net.minecraft.world.entity.item.ItemEntity {
        VisualItem(net.minecraft.world.level.Level world,double x,double y,double z,net.minecraft.world.item.ItemStack stack){super(world,x,y,z,stack);}
        void auctionGlow(boolean value){setSharedFlag(6,value);}
        @Override public int getAge(){return dev.dsmp.economy.design.DesignAccess.reduced()?0:tickCount;}
    }
    private AuctionHologram(){}
    public static AuctionAnchor capture(){
        var client=net.minecraft.client.Minecraft.getInstance();
        if(client.level==null||client.player==null)throw new IllegalStateException("Join the world before placing an auction");
        return new AuctionAnchor(dimension(client.level),client.player.getX(),client.player.getY(),client.player.getZ());
    }
    private static String dimension(net.minecraft.client.multiplayer.ClientLevel world){return world.dimension().identifier().toString();}
    public static void clear(){
        if(world!=null){remove(item);remove(label);}
        world=null;item=null;label=null;anchor=null;itemId=null;lastText=null;
    }
    private static void remove(net.minecraft.world.entity.Entity entity){if(entity!=null&&world.getEntity(entity.getId())==entity)world.removeEntity(entity.getId(),net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);}
    private static int localId(net.minecraft.client.multiplayer.ClientLevel world){if(nextId>=-1)nextId=Integer.MIN_VALUE+16_384;while(world.getEntity(nextId)!=null){nextId++;if(nextId>=-1)nextId=Integer.MIN_VALUE+16_384;}return nextId++;}
    public static void tick(net.minecraft.client.Minecraft client){
        var round=AuctionService.current();
        if(round==null||!round.visible()||round.anchor()==null||client.level==null||client.player==null){clear();return;}
        var target=round.anchor();var player=client.player;
        if(!target.nearby(dimension(client.level),player.getX(),player.getY(),player.getZ())){clear();return;}
        if(world!=client.level||!target.equals(anchor)||round.startedAt()!=lotStart||!round.item().equals(itemId)||round.quantity()!=quantity||item==null||label==null||world.getEntity(item.getId())!=item||world.getEntity(label.getId())!=label){
            clear();AuctionItems.Entry selected;try{selected=AuctionItems.find(round.item());}catch(IllegalArgumentException ex){return;}
            world=client.level;anchor=target;lotStart=round.startedAt();itemId=round.item();quantity=round.quantity();
            var stack=selected.stack().copyWithCount(quantity);
            item=new VisualItem(world,target.x(),target.y()+.08,target.z(),stack);
            item.setId(localId(world));item.setNoGravity(true);item.setDeltaMovement(0,0,0);item.setNeverPickUp();item.setUnlimitedLifetime();world.addEntity(item);
            label=new net.minecraft.world.entity.Display.TextDisplay(net.minecraft.world.entity.EntityTypes.TEXT_DISPLAY,world);label.setId(localId(world));label.setPos(target.x(),target.y()+1.1,target.z());
            label.setBillboardConstraints(net.minecraft.world.entity.Display.BillboardConstraints.CENTER);label.setLineWidth(400);label.setBackgroundColor(0x50000000);label.setFlags((byte)1);world.addEntity(label);
        }
        // Stop local physics/water drift. Vanilla still supplies the item's bob and rotation.
        item.setDeltaMovement(0,0,0);item.setPos(target.x(),target.y()+.08,target.z());
        item.auctionGlow(round.state()==AuctionRound.State.RUNNING);
        String text=caption(round,AuctionItems.find(round.item()).name(),System.currentTimeMillis());
        if(!text.equals(lastText)){label.setText(net.minecraft.network.chat.Component.literal(text));lastText=text;}
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
