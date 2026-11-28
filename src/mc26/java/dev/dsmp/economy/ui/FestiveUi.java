package dev.dsmp.economy.ui;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.menubackground.MenuBackgrounds;
import dev.dsmp.economy.menubackground.MenuPalette;
import dev.dsmp.economy.menubackground.MenuDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Consistent menu surfaces and typography, independent of the in-game HUD appearance. */
public final class FestiveUi {
    private static int accent=0x8B6CF2;private static boolean windowed,embedded,decorated=true;
    static boolean decorated(){return decorated;}
    static void embedded(boolean value){embedded=value;}
    static boolean embedded(){return embedded;}
    static void windowed(boolean value){windowed=value;}
    static boolean windowed(){return windowed;}
    private static dev.dsmp.economy.inventory.InventoryTheme holiday(){var o=MenuBackgrounds.visible();return o.scene==dev.dsmp.economy.menubackground.MenuScene.HOLIDAY?o.menuTheme:null;}
    private static int windowAccent(int value){if(windowed&&palette().colorful()&&value==0x8B6CF2)return palette().accent;var theme=holiday();return windowed&&theme!=null&&!palette().colorful()?theme.accent:windowed&&value==0x8B6CF2?0xC84E48:value;}
    public static MenuPalette palette(){return MenuBackgrounds.visible().palette;}
    public static int frameColor(){return palette().colorful()?0xF0000000|MenuPalette.mix(palette().top,palette().card,.6):0xED351417;}
    public static int frameBorder(){return palette().colorful()?0xC8000000|palette().accent:0xB57E3837;}
    private static boolean scoped(){return windowed||embedded;}
    private static int textColor(int color){return scoped()?MenuDisplay.alpha(color,dev.dsmp.economy.design.DesignAccess.contrast()?100:MenuBackgrounds.visible().textOpacity):color;}
    private static int panelColor(int color){return scoped()?MenuDisplay.alpha(color,dev.dsmp.economy.design.DesignAccess.contrast()?100:MenuBackgrounds.visible().cards):color;}
    private FestiveUi(){}
    public static int sidebarColor(){var o=MenuBackgrounds.visible();if(o.palette.colorful())return 0xF0000000|o.palette.sidebar;return o.scene==dev.dsmp.economy.menubackground.MenuScene.HOLIDAY?(0xF3000000|blend(0xFF000000|o.menuTheme.background,0xFF000000,.5f)&0xFFFFFF):o.scene==dev.dsmp.economy.menubackground.MenuScene.SOLID?(0xF3000000|blend(0xFF000000|o.solidColor,0xFF000000,.5f)&0xFFFFFF):0xF3201113;}
    public static int accent(){return accent;}
    public static void background(GuiGraphicsExtractor g,int width,int height,TrackerConfig config){background(g,width,height,config,true);}
    public static void background(GuiGraphicsExtractor g,int width,int height,TrackerConfig config,boolean character){
        accent=windowAccent(config.menuAccent);var options=MenuBackgrounds.visible();decorated=options.decorations==2||options.decorations==0&&config.festiveBackground;
        if(embedded)return;
        if(windowed){MenuBackgroundRenderer.background(g,width,height,config,options);if(character)CharacterSceneRenderer.draw(g,0,0,width,height,options);return;}
        MenuBackgroundRenderer.background(g,width,height,config,options);if(character)CharacterSceneRenderer.draw(g,0,0,width,height,options);
        if(dev.dsmp.economy.design.DesignAccess.contrast())g.fill(0,0,width,height,0xA0000710);
        g.fillGradient(0,0,width,Math.min(62,height),0x9C0A0E18,0x000A0E18);
        g.fillGradient(0,Math.max(0,height-32),width,height,0x000A0E18,0x850A0E18);
    }
    public static void textFit(GuiGraphicsExtractor g,String value,int x,int y,int width,int color,float preferred,boolean bold){float scale=Math.min(preferred,Math.max(1,width)/(float)Math.max(1,MenuText.width(value,bold)));text(g,value,x,y,color,scale,bold);}
    static dev.dsmp.economy.design.FullLabel.Fit labelLayout(String value,int w,int h,boolean bold,boolean leading){return dev.dsmp.economy.design.FullLabel.fit(value,Math.max(1,w-(leading?28:w<60?8:14)),Math.max(1,h-4),h>=24?.92f:.82f,11*dev.dsmp.economy.design.DesignAccess.scale(),line->MenuText.width(line,bold));}
    public static void text(GuiGraphicsExtractor g,String value,int x,int y,int color,float scale,boolean bold){
        if(windowed)color=palette().colorful()?switch(color){case 0xFFD9BDFF,0xFFC7A5E8->0xFF000000|palette().warm;case 0xFF8993AC,0xFFB3A6D4,0xFFAAA3C3,0xFFB8ABC9,0xFFB5AEC6,0xFFAA8783,0xFF967774,0xFF9A8584,0xFFA68C8B->0xFFD3E2F1;default->color;}:switch(color){case 0xFFD9BDFF,0xFFC7A5E8->0xFFE7C4B3;case 0xFF8993AC,0xFFB3A6D4,0xFFAAA3C3,0xFFB8ABC9,0xFFB5AEC6->0xFFA68C8B;default->color;};
        color=textColor(color);
        var pose=g.pose();pose.pushMatrix();pose.translate(x,y);pose.scale(scale*dev.dsmp.economy.design.DesignAccess.scale());
        try{g.text(Minecraft.getInstance().font,MenuText.text(value,bold),0,0,color,false);}finally{pose.popMatrix();}
    }
    public static void surface(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){color=panelColor(color);if(windowed){UiSurfaces.card(g,x,y,w,h,color&0xFF000000|(palette().colorful()?palette().card:holiday()==null?0x22171C:blend(0xFF000000|holiday().background,0xFF000000,.3f)&0xFFFFFF));UiSurfaces.cardOutline(g,x,y,w,h,palette().colorful()?0x70000000|palette().secondary:0x704C2F33);}else{UiSurfaces.fill(g,x,y,w,h,color);UiSurfaces.outline(g,x,y,w,h,0x354E5A72);}}
    public static void panel(GuiGraphicsExtractor g,int x,int y,int w,int h,int accent){
        UiSurfaces.shadow(g,x,y,w,h);surface(g,x,y,w,h,dev.dsmp.economy.design.DesignAccess.contrast()?0xFF07101B:scoped()?0xFF111827:MenuBackgrounds.visible().cards*255/100<<24|0x111827);
        if(w>32&&h>24)g.fill(x+12,y+1,x+Math.min(w-12,44),y+2,0xB0000000|windowAccent(accent));
    }
    static int blend(int a,int b,float t){int result=0;for(int shift=0;shift<=24;shift+=8)result|=Math.round((a>>>shift&255)*(1-t)+(b>>>shift&255)*t)<<shift;return result;}
    public static void control(GuiGraphicsExtractor g,int x,int y,int w,int h,String label,boolean selected,boolean hovered,int accent){control(g,x,y,w,h,label,selected,hovered?1:0,false,true,accent,false);}
    static void control(GuiGraphicsExtractor g,int x,int y,int w,int h,String label,boolean selected,float hover,boolean focused,boolean enabled,int accent,boolean leading){
        boolean primary=label.equals("Save")||label.equals("Done");
        accent=windowAccent(accent);int tone=0xFF000000|accent;if(selected&&label.equals("ON"))tone=0xFF70CDB2;
        int base=primary?blend(0xFF161E31,tone,.78f):selected?blend(0xFF141B2A,tone,.22f):0xF5192130;
        int border=primary?blend(tone,0xFFD8CFFF,.25f):selected?blend(tone,0xFF7786A0,.3f):0x7055637B;
        if(windowed){base=primary?0xE99A3433:selected?0xF348282A:0xE11B151B;border=primary?0xC8D8695E:selected?0xB1774745:0x70533B43;}
        if(windowed&&holiday()!=null){var theme=holiday();base=primary?blend(0xFF000000|theme.background,0xFF000000|theme.accent,.65f):selected?blend(0xFF000000|theme.background,0xFF000000|theme.accent,.2f):blend(0xFF000000|theme.background,0xFF000000,.5f);border=0xA0000000|theme.accent;}
        if(windowed&&palette().colorful()){var p=palette();base=0xFF000000|(primary?MenuPalette.mix(p.card,p.accent,.75):selected?MenuPalette.mix(p.card,p.accent,.28):MenuPalette.mix(p.sidebar,p.card,.5));border=(selected||primary?0xC8000000:0x80000000)|p.secondary;}
        if(!enabled){base=windowed?0xB01B151B:0xB0141924;border=windowed?0x404D3439:0x30465065;}
        if(windowed&&leading){if(focused)g.fill(x+24,y+h-2,x+w-5,y+h-1,palette().colorful()?0xFF000000|palette().secondary:0xFFE1A38D);}
        else if(windowed){UiSurfaces.card(g,x,y,w,h,blend(base,0xFF614044,hover*.24f));UiSurfaces.cardOutline(g,x,y,w,h,focused?0xFFE9BAA3:blend(border,0xFFD89985,hover*.35f));}
        else{UiSurfaces.shadow(g,x,y,w,h);UiSurfaces.fill(g,x,y,w,h,blend(base,0xFF303C53,hover*(primary?.08f:.32f)));UiSurfaces.outline(g,x,y,w,h,focused?0xFFE0D9FF:blend(border,0xFFC8BFF1,hover*.6f));}
        if(selected&&!primary&&w>=60)UiSurfaces.fill(g,x+4,y+Math.max(4,(h-14)/2),3,Math.min(14,h-8),tone);
        if(leading)MenuChrome.icon(g,label,x+8,y+Math.max(2,(h-9)/2),selected?(0xFF000000|accent):0xFFB19594);
        boolean bold=primary||selected;var layout=labelLayout(label,w,h,bold,leading);float scale=layout.scale();
        int brightness=((base>>16&255)*299+(base>>8&255)*587+(base&255)*114)/1000;
        int color=!enabled?0xFF728098:primary?(brightness>145?0xFF101726:0xFFFFFFFF):selected?0xFFF0EAFE:0xFFDCE5F4;
        if(dev.dsmp.economy.design.DesignAccess.contrast()){if(!(windowed&&leading)){UiSurfaces.fill(g,x,y,w,h,selected?0xFF263A5A:0xFF081322);UiSurfaces.outline(g,x,y,w,h,0xFFDCE9FF);}color=0xFFFFFFFF;}
        if(windowed&&leading&&hover>.1f)color=0xFFFFFFFF;
        float lineHeight=layout.lineHeight()*scale;int top=y+Math.max(2,Math.round((h-layout.lines().size()*lineHeight)/2));
        for(int i=0;i<layout.lines().size();i++){String line=layout.lines().get(i);int textWidth=Math.round(MenuText.width(line,bold)*scale);text(g,line,leading?x+24:x+(w-textWidth)/2,top+Math.round(i*lineHeight),color,scale,bold);}
    }
}
