package net.minecraft;
public class class_638 extends class_1937 {
 public final java.util.Map<Integer,class_1297> entities=new java.util.HashMap<>();
 public class_638(String dimension){super(dimension);}
 public void method_53875(class_1297 entity){entities.put(entity.method_5628(),entity);}
 public void method_2945(int id,class_1297.class_5529 reason){entities.remove(id);}
 public class_1297 method_8469(int id){return entities.get(id);}
}
