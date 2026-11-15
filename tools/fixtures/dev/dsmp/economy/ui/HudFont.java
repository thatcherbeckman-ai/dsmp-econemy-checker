package dev.dsmp.economy.ui;
import net.minecraft.class_2561;
/** Native-free substitute for the retained Inter font metrics. Never shipped. */
final class HudFont {
    static int width(String text,boolean display){return text.codePointCount(0,text.length())*(display?7:6);}
    static class_2561 text(String value,boolean display){return class_2561.method_43470(value);}
}
