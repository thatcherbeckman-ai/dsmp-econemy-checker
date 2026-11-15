package net.minecraft;
import java.util.*;
public final class class_332 {
    public record Shape(String kind, int color) {}
    public record Draw(String kind,float x,float y,float width,float height,int color,String text,float scale,boolean bold){}
    public final List<Draw> draws=new ArrayList<>();
    private void captureText(String value,int x,int y,int color,boolean bold){draws.add(new Draw("text",pose.m00()*x+pose.m10()*y+pose.m20(),pose.m01()*x+pose.m11()*y+pose.m21(),0,0,color,value,pose.m00(),bold));}
    public final List<Shape> shapes = new ArrayList<>();
    public final List<String> text = new ArrayList<>();
    private final org.joml.Matrix3x2fStack pose = new org.joml.Matrix3x2fStack(16);
    public int scissorDepth;
    public org.joml.Matrix3x2fStack method_51448() { return pose; }
    public void method_44379(int x1,int y1,int x2,int y2) { scissorDepth++; }
    public void method_44380() { if(--scissorDepth<0)throw new IllegalStateException("Unbalanced clip"); }
    public void method_51439(class_327 font,class_2561 value,int x,int y,int color,boolean shadow) { text.add(value.getString());shapes.add(new Shape("text",color));captureText(value.getString(),x,y,color,value instanceof class_5250 component&&component.bold()); }
    public final java.util.List<class_1799> items=new java.util.ArrayList<>();
    public void method_51427(class_1799 item,int x,int y){items.add(item);}
    public final List<Float> scales = new ArrayList<>();
    public void method_25294(int x1, int y1, int x2, int y2, int color) { shapes.add(new Shape("fill", color));draws.add(new Draw("rect",x1,y1,x2-x1,y2-y1,color,"",1,false)); }
    public void method_51433(class_327 font, String value, int x, int y, int color, boolean shadow) { text.add(value); shapes.add(new Shape("text", color));captureText(value,x,y,color,false); }
    public void rounded(int x, int y, int width, int height, int color) { shapes.add(new Shape("rounded", color)); }
    public void border(int x, int y, int width, int height, int color) { shapes.add(new Shape("border", color)); }
}
