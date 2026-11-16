package dev.dsmp.economy.ui;


import java.util.*;

/** Enumerates Minecraft's live item registry, including installed mods' items. */
public final class AuctionItems {
    public record Entry(String id,String name,net.minecraft.world.item.ItemStack stack){}
    private static List<Entry> all;
    private static java.lang.ref.WeakReference<net.minecraft.client.multiplayer.ClientLevel> boundWorld=new java.lang.ref.WeakReference<>(null);
    public static void invalidate(){all=null;boundWorld.clear();}
    private AuctionItems(){}
    public static List<Entry> all(){
        var world=net.minecraft.client.Minecraft.getInstance().level;
        if(world==null){invalidate();return List.of();}
        if(boundWorld.get()!=world){all=null;boundWorld=new java.lang.ref.WeakReference<>(world);}
        if(all==null){var list=new ArrayList<Entry>();for(net.minecraft.world.item.Item item:net.minecraft.core.registries.BuiltInRegistries.ITEM){String id=((net.minecraft.core.Registry<net.minecraft.world.item.Item>)net.minecraft.core.registries.BuiltInRegistries.ITEM).getKey(item).toString();if(!id.equals("minecraft:air")){var stack=new net.minecraft.world.item.ItemStack(item);list.add(new Entry(id,stack.getHoverName().getString(),stack));}}list.sort(Comparator.comparing(Entry::id));all=List.copyOf(list);}return all;
    }
    public static Entry find(String id){return all().stream().filter(e->e.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown item: "+id));}
    public static List<Entry> search(String query){String q=query.trim().toLowerCase(Locale.ROOT);return all().stream().filter(e->e.id().toLowerCase(Locale.ROOT).contains(q)||e.name().toLowerCase(Locale.ROOT).contains(q)).toList();}
}
