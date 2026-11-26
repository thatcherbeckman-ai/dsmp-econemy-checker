package dev.dsmp.economy.text;

import java.util.function.ToIntBiFunction;

/** Whole-box styling. Content, positions, images and note text are never replaced by a preset. */
public final class TextTypography {
    public static final String[] COLOR_NAMES={"White","Black","Red","Orange","Yellow","Lime","Green","Mint","Teal","Cyan","Sky","Blue","Indigo","Purple","Pink","Rose","Gold","Silver","Coral","Peach","Lilac","Ice","Crimson","Slate"};
    public static final int[] COLORS={0xFFFFFF,0x080B12,0xFF5454,0xFF9B44,0xFFE66D,0xA9F44A,0x48CE78,0x52EDC3,0x37BDB3,0x4DE8F2,0x79BBFF,0x537AF7,0x8B7DF4,0xBC82F4,0xF780D1,0xEF90A7,0xEDC86A,0xCDD5E2,0xFF8D77,0xFFC4A0,0xD2B9FA,0xCDF7FF,0xC93662,0x66768D};
    public static final String[] PRESETS={"Neon Mint","Sunset","Ocean","Gold","Candy","Frost","Lava","Lavender","Emerald","Mono","Bubble Pop","Soft Rose"};
    public static final int[][] PAIRS={{0x56F5C9,0x8DFF80},{0xFFB16A,0xF178C4},{0x67E3E8,0x657CF4},{0xFFF1B0,0xE4AC45},{0xFD9CDC,0xAB9AFF},{0xFFFFFF,0x8EDCEB},{0xFFD76C,0xF26A55},{0xECD8FF,0x9B8CE8},{0xB3F6C4,0x4CC3AA},{0xFFFFFF,0xCAD2DF},{0xF6AAE4,0x9BDFFF},{0xFFE3E5,0xE99CAC}};
    private TextTypography(){}
    public static boolean active(TextBox b){return b.typography||b.bold||b.italic||b.outline||b.customBackground||b.letterSpacing!=0||b.lineGap!=3||b.textOpacity!=255||b.outlineWidth!=.6f||b.shadowOffset!=1||b.shadowColor!=0;}
    public static int effectiveFont(TextBox box,int font){return box.bold&&font==0?1:font;}
    public static double width(TextBox box,String text,int font,ToIntBiFunction<String,Integer> measure){int count=text.codePointCount(0,text.length());return measure.applyAsInt(text,font)+(box.bold&&font>=2?count:0)+Math.max(0,count-1)*box.letterSpacing+(box.italic&&count>0?2:0);}
    public static void font(TextBox b,int font){b.font=font;for(var s:b.lineStyles)if(s.custom)s.font=font;}
    public static boolean anyOutline(TextBox b){return b.outline||b.lineStyles.stream().anyMatch(s->s.custom&&s.outline);}
    public static void outline(TextBox b,boolean value){b.outline=value;for(var s:b.lineStyles)if(s.custom)s.outline=value;}
    public static void gradient(TextBox b,int gradient){b.gradient=gradient;for(var s:b.lineStyles)if(s.custom)s.gradient=gradient;}
    public static int color(TextBox b,int role){return switch(role){case 0->b.startColor;case 1->b.endColor;case 2->b.outlineColor;case 3->b.shadowColor;case 4->b.backgroundColor;default->throw new IllegalArgumentException("Unknown text color");};}
    public static void color(TextBox b,int role,int rgb){if((rgb&0xFF000000)!=0)throw new IllegalArgumentException("Use six hex digits");switch(role){case 0->{b.startColor=rgb;for(var s:b.lineStyles)if(s.custom)s.startColor=rgb;}case 1->{b.endColor=rgb;for(var s:b.lineStyles)if(s.custom)s.endColor=rgb;}case 2->{b.outlineColor=rgb;for(var s:b.lineStyles)s.outlineColor=rgb;}case 3->b.shadowColor=rgb;case 4->{b.backgroundColor=rgb;b.customBackground=true;}default->throw new IllegalArgumentException("Unknown text color");}}
    public static void pair(TextBox b,int index){color(b,0,PAIRS[index][0]);color(b,1,PAIRS[index][1]);gradient(b,TextBox.ACROSS);}
    public static void preset(TextBox b,int index){if(index<0||index>=PRESETS.length)throw new IllegalArgumentException("Unknown text preset");pair(b,index);font(b,index==10?3:index==9||index==11?0:1);b.bold=false;b.italic=false;b.letterSpacing=index==3?.5f:0;b.lineGap=4;outline(b,index==0||index==6||index==10);b.outlineWidth=index==0?1.2f:.6f;b.outlineColor=0x11131D;b.shadow=index!=9;b.shadowColor=0x060910;b.shadowOffset=1;b.textOpacity=255;}
    public static void reset(TextBox b){b.bold=b.italic=b.customBackground=false;outline(b,false);b.shadow=false;b.backgroundColor=0x17373D;b.letterSpacing=0;b.lineGap=3;b.textOpacity=255;b.outlineWidth=.6f;b.shadowOffset=1;b.shadowColor=0;b.outlineColor=0x10131B;}
}
