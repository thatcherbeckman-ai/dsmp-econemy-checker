package net.minecraft;
import java.util.*;
/** Compile-only API signatures plus a native-free widget harness; never shipped. */
public class class_437 {
    public int field_22789 = 640, field_22790 = 480;
    public final class_327 field_22793 = new class_327();
    public final List<class_4185> widgets = new ArrayList<>();
    public class_437(class_2561 title) {}
    protected void method_25426() {}
    protected class_364 method_37063(class_364 widget) { widgets.add((class_4185)widget); return widget; }
    public void method_41843() { widgets.clear(); method_25426(); }
    public void method_25394(class_332 graphics, int mx, int my, float delta) {}
    public void method_25419() {}
    public boolean method_25421() { return false; }
}
