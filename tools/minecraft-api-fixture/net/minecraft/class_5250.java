package net.minecraft;
public record class_5250(String text,boolean bold,String font) implements class_2561 {
 public class_5250(String text){this(text,false,"");} public class_5250(String text,boolean bold){this(text,bold,"");}
 public String getString(){return text;}
 public class_5250 method_27694(java.util.function.UnaryOperator<class_2583> fn){var style=fn.apply(new class_2583(null));return new class_5250(text,bold,style.font() instanceof class_11719.class_11721 file?file.id().toString():"");}
}
