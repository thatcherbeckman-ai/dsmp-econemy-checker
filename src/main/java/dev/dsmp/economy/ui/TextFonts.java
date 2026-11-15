package dev.dsmp.economy.ui;

import net.minecraft.*;

final class TextFonts {
    static final String[] NAMES={"Inter","Inter bold","Minecraft","Bubble"};
    private static final class_11719 BUBBLE=new class_11719.class_11721(class_2960.method_60655("economy_tracker","bubble"));
    private TextFonts(){}
    static class_2561 text(String text,int font){return font==3?class_2561.method_43470(text).method_27694(style->style.method_27704(BUBBLE)):font==2?class_2561.method_43470(text):HudFont.text(text,font==1);}
    static int width(String text,int font){return font==3?class_310.method_1551().field_1772.method_27525(text(text,font)):font==2?class_310.method_1551().field_1772.method_1727(text):HudFont.width(text,font==1);}
}
