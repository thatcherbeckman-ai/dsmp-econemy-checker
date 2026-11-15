package net.minecraft;
import java.util.*;
public final class class_332 {
    public record Shape(String kind, int color) {}
    public final List<Shape> shapes = new ArrayList<>();
    public final List<String> text = new ArrayList<>();
    private final org.joml.Matrix3x2fStack pose = new org.joml.Matrix3x2fStack(16);
    public int scissorDepth;
    public org.joml.Matrix3x2fStack method_51448() { return pose; }
    public void method_44379(int x1,int y1,int x2,int y2) { scissorDepth++; }
    public void method_44380() { if(--scissorDepth<0)throw new IllegalStateException("Unbalanced clip"); }
    public void method_51439(class_327 font,class_2561 value,int x,int y,int color,boolean shadow) { method_51433(font,value.getString(),x,y,color,shadow); }
    public final List<Float> scales = new ArrayList<>();
    public void method_25294(int x1, int y1, int x2, int y2, int color) { shapes.add(new Shape("fill", color)); }
    public void method_51433(class_327 font, String value, int x, int y, int color, boolean shadow) { text.add(value); shapes.add(new Shape("text", color)); }
    public void rounded(int x, int y, int width, int height, int color) { shapes.add(new Shape("rounded", color)); }
    public void border(int x, int y, int width, int height, int color) { shapes.add(new Shape("border", color)); }
}
