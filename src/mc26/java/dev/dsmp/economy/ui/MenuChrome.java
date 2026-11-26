package dev.dsmp.economy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Small festive details drawn inside the window, without covering the world around it. */
final class MenuChrome {
    private static final int[] BULBS={0xFFFFD25E,0xFFDB5A58,0xFF65CC7C,0xFFF27CB4};
    static void lights(GuiGraphicsExtractor g,int width,int height){
        var options=dev.dsmp.economy.menubackground.MenuBackgrounds.visible();
        if(options.scene==dev.dsmp.economy.menubackground.MenuScene.HOLIDAY&&options.menuTheme!=dev.dsmp.economy.inventory.InventoryTheme.CHRISTMAS){for(var r:dev.dsmp.economy.inventory.HolidayScene.header(dev.dsmp.economy.menubackground.WindowBackdrop.animation(options),width))g.fill(r.x(),r.y(),r.x()+r.width(),r.y()+r.height(),r.color());for(int x=12;x<width-8;x+=24)UiSurfaces.fill(g,x,height-3,3,3,0xFF000000|options.menuTheme.secondary);return;}
        int previous=3;for(int x=0;x<width;x++){int y=3+(int)Math.round(2*Math.sin(x*.045));g.fill(x,Math.min(y,previous),x+1,Math.max(y,previous)+1,0xFF36764E);previous=y;}
        for(int i=0,x=12;x<width-6;i++,x+=25){int y=5+(int)Math.round(2*Math.sin(x*.045)),c=BULBS[i%4];g.fill(x,y-2,x+1,y+2,0xFF456E42);UiSurfaces.fill(g,x-4,y,8,9,0x23000000|c&0xFFFFFF);UiSurfaces.fill(g,x-2,y+1,4,6,c);g.fill(x-1,y+2,x,y+4,0xAFFFFFFF);}
        for(int i=0,x=18;x<width-12;i++,x+=30)UiSurfaces.fill(g,x,height-3,3,3,BULBS[i%4]);
        UiSurfaces.outline(g,0,0,width,height,0x754E3432);
    }
    static void brand(GuiGraphicsExtractor g,int sidebar){
        UiSurfaces.fill(g,10,20,22,22,0xFFB34542);UiSurfaces.outline(g,10,20,22,22,0xFFA85C51);icon(g,"Overview",17,26,0xFFFFDDD0);
        FestiveUi.textFit(g,"thatchacats",39,21,sidebar-46,0xFFF1DFD7,.85f,true);FestiveUi.textFit(g,"econemy checker",39,35,sidebar-46,0xFFAA8783,.6f,false);
        g.fill(10,49,sidebar-10,50,0x444E3736);FestiveUi.text(g,"MODULES",10,53,0xFF967774,.52f,true);
    }
    static void account(GuiGraphicsExtractor g,int sidebar,int height){
        int y=height-34;UiSurfaces.card(g,8,y,sidebar-16,25,0xE21B1417);UiSurfaces.cardOutline(g,8,y,sidebar-16,25,0x604E3736);
        var client=Minecraft.getInstance();if(client.player!=null)net.minecraft.client.gui.components.PlayerFaceExtractor.extractRenderState(g,client.player.getSkin(),13,y+5,15);else{UiSurfaces.fill(g,13,y+5,15,15,0xFF8A5147);g.fill(17,y+9,24,y+14,0xFFDCA98B);}
        FestiveUi.textFit(g,client.getUser().getName(),34,y+5,sidebar-56,0xFFE9DBD8,.65f,true);FestiveUi.text(g,client.level==null?"Local menu":"Connected",34,y+15,0xFF9A8584,.5f,false);UiSurfaces.fill(g,sidebar-20,y+10,4,5,0xFF64CC85);
    }
    static void icon(GuiGraphicsExtractor g,String label,int x,int y,int color){
        switch(label){
            case "Overview"->{for(int row=0;row<2;row++)for(int col=0;col<2;col++)outline(g,x+col*5,y+row*5,3,3,color);}
            case "HUD modules","HUD layout"->{outline(g,x,y,9,7,color);g.fill(x+3,y+8,x+6,y+9,color);}
            case "Appearance","Inventory themes"->{outline(g,x+1,y+1,7,7,color);g.fill(x+3,y+3,x+7,y+5,color);}
            case "Text boxes"->{g.fill(x,y+1,x+9,y+2,color);g.fill(x+4,y+1,x+5,y+9,color);}
            case "Loans"->{outline(g,x,y+2,9,6,color);g.fill(x+6,y+4,x+9,y+6,color);}
            case "Tracking"->{for(int i=0;i<3;i++)g.fill(x+i*3,y+6-i*2,x+i*3+2,y+9,color);}
            case "Player background"->{outline(g,x,y,9,9,color);g.fill(x+2,y+5,x+4,y+7,color);g.fill(x+4,y+3,x+7,y+7,color);}
            default->{outline(g,x+1,y+1,7,7,color);g.fill(x+4,y+3,x+5,y+6,color);g.fill(x+3,y+4,x+6,y+5,color);}
        }
    }
    private static void outline(GuiGraphicsExtractor g,int x,int y,int w,int h,int c){g.fill(x,y,x+w,y+1,c);g.fill(x,y+h-1,x+w,y+h,c);g.fill(x,y,x+1,y+h,c);g.fill(x+w-1,y,x+w,y+h,c);}
}
