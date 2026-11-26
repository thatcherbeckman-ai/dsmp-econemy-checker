package dev.dsmp.economy.ui;



/** The live local player supplies its real skin and model; no external skin downloads. */
public final class WelcomeBanner {
    private WelcomeBanner(){}
    public static int height(int screenHeight){return screenHeight>=340?76:44;}
    public static void draw(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int width,int height,int accent){
        FestiveUi.panel(g,x,y,width,height,accent);
        UiSurfaces.fill(g,x+1,y+1,width-2,height-2,0x16000000|accent);
        var client=net.minecraft.client.Minecraft.getInstance();int avatarWidth=Math.min(78,width/4);
        if(client.player!=null)GuiEntities.player(g,x+4,y+2,x+avatarWidth,y+height-2,Math.max(15,(height-8)/2),.0625f,x+avatarWidth/2f,y+height/3f,client.player);
        int textX=x+avatarWidth+8,room=width-avatarWidth-16;
        String user=client.getUser().getName();float size=height>=70?1.5f:1f;
        String welcome="Welcome, "+user;
        FestiveUi.textFit(g,welcome,textX,y+(height>=70?22:9),room,0xFFD9BDFF,size,true);
        FestiveUi.textFit(g,"Have a great time on DonutSMP!",textX,y+(height>=70?45:26),room,0xFFAAA3C3,.75f,false);
    }
}
