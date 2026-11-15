package net.minecraft;
public final class class_7923 {
public static final class_7922<class_1792> field_41178=new class_7922<>() {
private final java.util.List<class_1792> items=java.util.List.of(new class_1792("Air"),new class_1792("Elytra"),new class_1792("Diamond"),new class_1792("Netherite Sword"));
public java.util.Iterator<class_1792> iterator(){return items.iterator();}
public class_2960 method_10221(class_1792 item){return new class_2960("minecraft:"+item.name.toLowerCase(java.util.Locale.ROOT).replace(' ','_'));}
}; }
