package dev.dsmp.economy.ui;

import dev.dsmp.economy.TrackerConfig;
import dev.dsmp.economy.menubackground.MenuBackgrounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Consistent menu surfaces and typography, independent of the in-game HUD appearance. */
public final class FestiveUi {
    private static int accent=0x8B6CF2;
    private FestiveUi(){}
    public static int accent(){return accent;}
    public static void background(GuiGraphicsExtractor g,int width,int height,TrackerConfig config){background(g,width,height,config,true);}
    public static void background(GuiGraphicsExtractor g,int width,int height,TrackerConfig config,boolean character){
        accent=config.menuAccent;var options=MenuBackgrounds.visible();MenuBackgroundRenderer.background(g,width,height,config,options);if(character)CharacterSceneRenderer.draw(g,0,0,width,height,options);
        g.fillGradient(0,0,width,Math.min(62,height),0x9C0A0E18,0x000A0E18);
        g.fillGradient(0,Math.max(0,height-32),width,height,0x000A0E18,0x850A0E18);
    }
    public static String fit(String text,int width,float scale){return MenuText.fit(text,Math.max(0,(int)(width/scale)),false);}
    public static void text(GuiGraphicsExtractor g,String value,int x,int y,int color,float scale,boolean bold){
        var pose=g.pose();pose.pushMatrix();pose.translate(x,y);pose.scale(scale);
        try{g.text(Minecraft.getInstance().font,MenuText.text(value,bold),0,0,color,false);}finally{pose.popMatrix();}
    }
    public static void surface(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){UiSurfaces.fill(g,x,y,w,h,color);UiSurfaces.outline(g,x,y,w,h,0x354E5A72);}
    public static void panel(GuiGraphicsExtractor g,int x,int y,int w,int h,int accent){
        UiSurfaces.shadow(g,x,y,w,h);surface(g,x,y,w,h,MenuBackgrounds.visible().cards*255/100<<24|0x111827);
        if(w>32&&h>24)g.fill(x+12,y+1,x+Math.min(w-12,44),y+2,0x90000000|accent);
    }
    static int blend(int a,int b,float t){int result=0;for(int shift=0;shift<=24;shift+=8)result|=Math.round((a>>>shift&255)*(1-t)+(b>>>shift&255)*t)<<shift;return result;}
    public static void control(GuiGraphicsExtractor g,int x,int y,int w,int h,String label,boolean selected,boolean hovered,int accent){control(g,x,y,w,h,label,selected,hovered?1:0,false,true,accent,false);}
    static void control(GuiGraphicsExtractor g,int x,int y,int w,int h,String label,boolean selected,float hover,boolean focused,boolean enabled,int accent,boolean leading){
        boolean primary=label.equals("Save")||label.equals("Done");
        int tone=0xFF000000|accent;if(selected&&label.equals("ON"))tone=0xFF70CDB2;
        int base=primary?blend(0xFF161E31,tone,.78f):selected?blend(0xFF141B2A,tone,.22f):0xF5192130;
        int border=primary?blend(tone,0xFFD8CFFF,.25f):selected?blend(tone,0xFF7786A0,.3f):0x7055637B;
        if(!enabled){base=0xB0141924;border=0x30465065;}
        UiSurfaces.shadow(g,x,y,w,h);UiSurfaces.fill(g,x,y,w,h,blend(base,0xFF303C53,hover*(primary?.08f:.32f)));
        UiSurfaces.outline(g,x,y,w,h,focused?0xFFE0D9FF:blend(border,0xFFC8BFF1,hover*.6f));
        if(selected&&!primary&&w>=60)UiSurfaces.fill(g,x+4,y+Math.max(4,(h-14)/2),3,Math.min(14,h-8),tone);
        float scale=h>=24?.98f:.88f;boolean bold=primary||selected;String shown=MenuText.fit(label,Math.max(0,(int)((w-(w<60?10:20))/scale)),bold);int textWidth=Math.round(MenuText.width(shown,bold)*scale);
        int brightness=((base>>16&255)*299+(base>>8&255)*587+(base&255)*114)/1000;
        int color=!enabled?0xFF728098:primary?(brightness>145?0xFF101726:0xFFFFFFFF):selected?0xFFF0EAFE:0xFFDCE5F4;
        text(g,shown,leading?x+16:x+Math.max(w<60?4:8,(w-textWidth)/2),y+Math.max(2,(h-11)/2),color,scale,bold);
    }
}
