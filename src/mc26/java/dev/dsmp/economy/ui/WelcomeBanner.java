package dev.dsmp.economy.ui;



/** The live local player supplies its real skin and model; no external skin downloads. */
public final class WelcomeBanner {
    private WelcomeBanner(){}
    public static int height(int screenHeight){return screenHeight>=340?76:44;}
    public static void draw(net.minecraft.client.gui.GuiGraphicsExtractor g,int x,int y,int width,int height,int accent){
        FestiveUi.panel(g,x,y,width,height,accent);
        for(int i=0;i<width;i+=8){int alpha=40*(width-i)/width;g.fill(x+i,y+1,x+Math.min(width,i+8),y+height-1,alpha<<24|accent);}
        var client=net.minecraft.client.Minecraft.getInstance();int avatarWidth=Math.min(78,width/4);
        if(client.player!=null)net.minecraft.client.gui.screens.inventory.InventoryScreen.extractEntityInInventoryFollowsMouse(g,x+4,y+2,x+avatarWidth,y+height-2,Math.max(15,(height-8)/2),.0625f,x+avatarWidth/2f,y+height/3f,client.player);
        int textX=x+avatarWidth+8,room=width-avatarWidth-16;
        String user=client.getUser().getName();float size=height>=70?1.5f:1f;
        String welcome=FestiveUi.fit("Welcome, "+user,room,size);
        FestiveUi.text(g,welcome,textX,y+(height>=70?22:9),0xFFD9BDFF,size,true);
        FestiveUi.text(g,FestiveUi.fit("Have a great time on DonutSMP!",room,.75f),textX,y+(height>=70?45:26),0xFFAAA3C3,.75f,false);
    }
}
