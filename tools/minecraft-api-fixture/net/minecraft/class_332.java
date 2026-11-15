package net.minecraft;
import java.util.*;
public final class class_332 {
    public record Shape(String kind, int color) {}
    public final List<Shape> shapes = new ArrayList<>();
    public final List<String> text = new ArrayList<>();
    public final List<Float> scales = new ArrayList<>();
    public void method_25294(int x1, int y1, int x2, int y2, int color) { shapes.add(new Shape("fill", color)); }
    public void method_51433(class_327 font, String value, int x, int y, int color, boolean shadow) { text.add(value); shapes.add(new Shape("text", color)); }
    public void rounded(int x, int y, int width, int height, int color) { shapes.add(new Shape("rounded", color)); }
    public void border(int x, int y, int width, int height, int color) { shapes.add(new Shape("border", color)); }
}
