package dev.dsmp.economy.inventory;

/** Original palettes and pixel motifs; theme names are stable saved identifiers. */
public enum InventoryTheme {
    VANILLA("Vanilla",0xC6C6C6,0xFFFFFF,0x777777,Motif.SNOW),
    CHRISTMAS("Christmas",0x102C28,0x70DDA7,0xEC6D80,Motif.TREE),
    HALLOWEEN("Halloween",0x241630,0xFFAC58,0xC88CF0,Motif.PUMPKIN),
    NEW_YEAR("New Year",0x151F36,0xF5D478,0x79D6E5,Motif.FIREWORK),
    VALENTINE("Valentine's Day",0x331E30,0xF39DC7,0xDAB1EB,Motif.HEART),
    LUNAR_NEW_YEAR("Lunar New Year",0x361F27,0xF1CF78,0xED7A75,Motif.LANTERN),
    ST_PATRICK("St. Patrick's Day",0x142E29,0x86DA94,0xE9D589,Motif.CLOVER),
    EASTER("Easter",0x26243B,0xB7A5EF,0x8DDFC1,Motif.EGG),
    EID("Eid",0x152B37,0xE5CD83,0x82D1CD,Motif.CRESCENT),
    DIWALI("Diwali",0x30223A,0xF5BD72,0xE397BC,Motif.LAMP),
    HANUKKAH("Hanukkah",0x152740,0x8EBDF3,0xE7DCA2,Motif.CANDLE),
    THANKSGIVING("Thanksgiving",0x33281F,0xEFB67C,0xD3916C,Motif.LEAF),
    INDEPENDENCE("Independence Day",0x1E2940,0x94BDEF,0xEC919D,Motif.STAR),
    BIRTHDAY("Birthday",0x2A2340,0xE1A6E8,0x85DDE0,Motif.GIFT),
    SPRING("Spring",0x203030,0xA8DCAC,0xEEB8D6,Motif.FLOWER),
    SUMMER("Summer",0x17303B,0xF2DA8B,0x89D9DD,Motif.SUN),
    AUTUMN("Autumn",0x32251F,0xF1B275,0xD69485,Motif.LEAF),
    WINTER("Winter",0x1C2B40,0xB5DCEF,0xB8B6ED,Motif.SNOW);

    public enum Motif {SNOW,TREE,PUMPKIN,FIREWORK,HEART,LANTERN,CLOVER,EGG,CRESCENT,LAMP,CANDLE,LEAF,STAR,GIFT,FLOWER,SUN}
    public final String label;
    public final int background,accent,secondary;
    public final Motif motif;
    InventoryTheme(String label,int background,int accent,int secondary,Motif motif){this.label=label;this.background=background;this.accent=accent;this.secondary=secondary;this.motif=motif;}
}
