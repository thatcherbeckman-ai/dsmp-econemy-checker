package net.minecraft;
import java.util.function.Consumer;
public final class class_342 implements class_364 {
    public final String label; public final int x,y,width,height;
    private int max=1024; private String text=""; private Consumer<String> changed=value->{};
    public class_342(class_327 font,int x,int y,int w,int h,class_2561 title) { label=title.getString();this.x=x;this.y=y;width=w;height=h; }
    public void method_47404(class_2561 placeholder) {}
    public void method_1880(int max) { this.max=max; }
    public void method_1852(String text) { this.text=text.substring(0,Math.min(max,text.length())); changed.accept(this.text); }
    public String method_1882() { return text; }
    public void method_1863(Consumer<String> listener) { changed=listener; }
    public void method_1867(String input) { method_1852(text+input); }
}
