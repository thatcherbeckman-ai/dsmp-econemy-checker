package dev.dsmp.economy.ui;

import net.minecraft.*;
import java.util.*;

/** Enumerates Minecraft's live item registry, including installed mods' items. */
public final class AuctionItems {
    public record Entry(String id,String name,class_1799 stack){}
    private static List<Entry> all;
    private AuctionItems(){}
    public static List<Entry> all(){
        if(all==null){var list=new ArrayList<Entry>();for(class_1792 item:class_7923.field_41178){String id=((class_2378<class_1792>)class_7923.field_41178).method_10221(item).toString();if(!id.equals("minecraft:air")){var stack=new class_1799(item);list.add(new Entry(id,stack.method_7964().getString(),stack));}}list.sort(Comparator.comparing(Entry::id));all=List.copyOf(list);}return all;
    }
    public static Entry find(String id){return all().stream().filter(e->e.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown item: "+id));}
    public static List<Entry> search(String query){String q=query.trim().toLowerCase(Locale.ROOT);return all().stream().filter(e->e.id().toLowerCase(Locale.ROOT).contains(q)||e.name().toLowerCase(Locale.ROOT).contains(q)).toList();}
}
