package dev.dsmp.economy.menubackground;

/** A cosmetic scene layer. No world, player, inventory or camera state is stored or changed. */
public final class CharacterComposition {
    public enum View {REAR("Rear",0),LEFT("Rear left",35),LEFT_SIDE("Left side",90),FRONT_LEFT("Front left",135),FRONT("Front",180),FRONT_RIGHT("Front right",225),RIGHT_SIDE("Right side",270),RIGHT("Rear right",325);public final String label;public final int yaw;View(String label,int yaw){this.label=label;this.yaw=yaw;}}
    public enum Base {LEDGE("Ledge"),BLOCK("Block"),DECK("Wide deck");public final String label;Base(String label){this.label=label;}}
    public enum Platform {
        GRASS("Grass",0x719B57,0x735842),STONE("Stone",0x8C929C,0x565E6C),OAK("Oak deck",0xC0A171,0x806343),CHERRY("Cherry deck",0xD59FA8,0x916B7C),SNOW("Snow",0xE0EDF4,0xA3BDCF),MOSS("Moss",0x769268,0x465E43),SANDSTONE("Sandstone",0xDDCB96,0xAD9467),QUARTZ("Quartz",0xDFDAE5,0xA7A4BA),DEEPSLATE("Dark stone",0x666A7C,0x353E50),BAMBOO("Bamboo deck",0xC4C182,0x7F8A52),NONE("No platform",0,0);
        public final String label;public final int top,side;Platform(String label,int top,int side){this.label=label;this.top=top;this.side=side;}
    }
    public enum Caption {TOP_LEFT,TOP_CENTER,TOP_RIGHT,BOTTOM_LEFT,BOTTOM_CENTER,BOTTOM_RIGHT}
    public static final String[] FONTS={"Inter bold","Inter","Bubble","Minecraft"};
    public boolean enabled=false,cape=true,shadow=true,textShadow=true,matchLighting=true;
    public Base base=Base.LEDGE;
    public View view=View.REAR;public Platform platform=Platform.GRASS;public Caption caption=Caption.TOP_CENTER;
    public int size=38,x=50,ground=78,platformSize=100,turn=0,tilt=0,font=0,textSize=100,titleColor=0xFFFFFF,subtitleColor=0xE4D5EB;
    public String title="{player}",subtitle="";
    public CharacterComposition copy(){var n=new CharacterComposition();n.enabled=enabled;n.base=base;n.matchLighting=matchLighting;n.cape=cape;n.shadow=shadow;n.textShadow=textShadow;n.view=view;n.platform=platform;n.caption=caption;n.size=size;n.x=x;n.ground=ground;n.platformSize=platformSize;n.turn=turn;n.tilt=tilt;n.font=font;n.textSize=textSize;n.titleColor=titleColor;n.subtitleColor=subtitleColor;n.title=title;n.subtitle=subtitle;return n;}
    public void validate(){
        if(base==null||view==null||platform==null||caption==null||title==null||subtitle==null)throw new IllegalArgumentException("Invalid character scene");
        if(size<20||size>55||x<0||x>100||ground<55||ground>90||platformSize<50||platformSize>150||turn< -180||turn>180||tilt< -15||tilt>15||font<0||font>=FONTS.length||textSize<50||textSize>200||titleColor<0||titleColor>0xFFFFFF||subtitleColor<0||subtitleColor>0xFFFFFF)throw new IllegalArgumentException("Character scene option is outside its range");
        text(title,120);text(subtitle,240);
    }
    private static void text(String value,int max){if(value.length()>max||value.split("\n",-1).length>4||value.codePoints().anyMatch(c->Character.isISOControl(c)&&c!='\n'))throw new IllegalArgumentException("Use up to four lines of plain text");}
}
