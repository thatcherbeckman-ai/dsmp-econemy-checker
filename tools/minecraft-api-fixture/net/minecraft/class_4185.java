package net.minecraft;
public final class class_4185 implements class_364 {
    public final String label; public final int x, y, width, height; private final class_4241 action;
    private class_4185(class_7840 builder) { label = builder.text.getString(); x = builder.x; y = builder.y; width = builder.w; height = builder.h; action = builder.action; }
    public void click() { action.onPress(this); }
    public interface class_4241 { void onPress(class_4185 button); }
    public static class_7840 method_46430(class_2561 text, class_4241 action) { return new class_7840(text, action); }
    public static final class class_7840 {
        final class_2561 text; final class_4241 action; int x, y, w, h;
        class_7840(class_2561 text, class_4241 action) { this.text = text; this.action = action; }
        public class_7840 method_46434(int x, int y, int width, int height) { this.x = x; this.y = y; w = width; h = height; return this; }
        public class_4185 method_46431() { return new class_4185(this); }
    }
}
