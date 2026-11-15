package dev.dsmp.economy.ui;
import net.minecraft.class_2561;
/** Native-free substitute for the retained Inter font metrics. Never shipped. */
final class HudFont {
    static int width(String text){return width(text,false);}
    static String fit(String text,int width){if(width(text)<=width)return text;return text.substring(0,Math.max(0,Math.min(text.length(),width/6-1)))+"…";}
    static int width(String text,boolean display){return text.codePointCount(0,text.length())*(display?7:6);}
    static class_2561 text(String value,boolean display){return new net.minecraft.class_5250(value,display);}
}
