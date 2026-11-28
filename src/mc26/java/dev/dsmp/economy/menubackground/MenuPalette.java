package dev.dsmp.economy.menubackground;

/** Colorful, readable menu palettes. Classic retains the previous seasonal styling. */
public enum MenuPalette {
    AURORA("Aurora",0x385C91,0x68518E,0x233854,0x344B6C,0x9EAAFF,0x64E8CD,0xFFC2EB),
    CANDY("Candy",0xA865A6,0x6777BC,0x493451,0x604968,0xFFB5E5,0x97E4FF,0xFFE29D),
    MINT("Mint",0x3A8B89,0x486F9C,0x1C464C,0x2F6067,0x94F2CC,0x95CBFF,0xFFD8AE),
    SKY("Sky",0x5596BB,0x6376BD,0x263F63,0x3D6083,0xA4E9FF,0xCDB9FF,0xFFCFDF),
    SUNSET("Sunset",0xB76B82,0x7564A2,0x4C354C,0x6C4B67,0xFFD19D,0xFFABCB,0xBCB7FF),
    CLASSIC("Classic",0x301316,0x301316,0x201113,0x22171C,0xC84E48,0xD8695E,0xE7C4B3);
    public final String label;public final int top,bottom,sidebar,card,accent,secondary,warm;
    MenuPalette(String label,int top,int bottom,int sidebar,int card,int accent,int secondary,int warm){this.label=label;this.top=top;this.bottom=bottom;this.sidebar=sidebar;this.card=card;this.accent=accent;this.secondary=secondary;this.warm=warm;}
    public boolean colorful(){return this!=CLASSIC;}
    public int background(double x,double y){double t=Math.clamp(.08+.7*y+.22*x,0,1);int base=mix(top,bottom,t);double glow=Math.max(0,1-Math.hypot(x-.9,y-.12)*1.5)*.16;return mix(base,secondary,glow);}
    public static int mix(int a,int b,double t){int rgb=0;for(int shift=0;shift<=16;shift+=8)rgb|=Math.clamp((int)Math.round((a>>shift&255)*(1-t)+(b>>shift&255)*t),0,255)<<shift;return rgb;}
}
