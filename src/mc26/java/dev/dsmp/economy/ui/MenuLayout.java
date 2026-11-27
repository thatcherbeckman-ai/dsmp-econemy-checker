package dev.dsmp.economy.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shared gutters and responsive control grids for the settings screens. */
public record MenuLayout(int width,int height,int margin,int gap,int buttonHeight,int top) {
    public static final String BRAND="thatchacats econemy checker";
    public static MenuLayout of(int width,int height){
        boolean roomy=width>=620&&height>=480;
        return new MenuLayout(width,height,width>=620?18:12,roomy?10:6,roomy?32:20,roomy?64:40);
    }
    public int contentWidth(){return width-margin*2;}
    public int step(){return buttonHeight+gap;}
    public int row(int index){return top+index*step();}
    public int x(int column,int columns){return margin+column*(contentWidth()+gap)/columns;}
    public int cellWidth(int column,int columns){return margin+(column+1)*(contentWidth()+gap)/columns-gap-x(column,columns);}
    public int actionWidth(){return width>=620?76:54;}
    public int doneX(){return width-margin-actionWidth()*2-gap;}
    public int cancelX(){return width-margin-actionWidth();}
    public int actionY(){return top>=64?14:8;}
    public void header(GuiGraphicsExtractor g,String subtitle){
        if(FestiveUi.embedded()){FestiveUi.textFit(g,subtitle,margin,top>=64?15:9,doneX()-margin-gap,0xFFB8ABC9,.78f,false);return;}
        int room=doneX()-margin-gap;
        float scale=Math.min(.94f,room/(float)Math.max(1,MenuText.width(BRAND,true)));
        FestiveUi.text(g,BRAND,margin,top>=64?15:9,0xFFF2EDF9,scale,true);
        if(top>=64)FestiveUi.text(g,subtitle,margin,39,0xFFB8ABC9,.78f,false);
    }
    public void footer(GuiGraphicsExtractor g,String notice){
        FestiveUi.surface(g,margin-4,height-24,width-margin*2+8,20,0xDB0D1421);
        FestiveUi.textFit(g,notice,margin,height-16,contentWidth()-8,0xFFB5AEC6,.68f,false);
    }
    public record Dashboard(int width,int height,int sidebar,int left,int contentWidth,int rowHeight,int bodyTop,int gap){
        public static Dashboard of(int width,int height){
            int side=width>=620?Math.clamp(width/5,140,184):Math.clamp(width/4,80,116);
            int gap=width>=620&&height>=480?10:6,left=side+gap+4,top=height>=480?84:60;
            return new Dashboard(width,height,side,left,width-left-gap-4,Math.clamp((height-top-30)/6,25,68),top,gap);
        }
        public int navStep(){return Math.clamp((height-bodyTop-44)/13,14,32);}
        public int navY(int index){return bodyTop+index*navStep();}
        public int navHeight(){return Math.max(12,navStep()-Math.min(gap,Math.max(2,navStep()/4)));}
        public int rowY(int index){return bodyTop+index*rowHeight;}
        public int loanTop(){return bodyTop+(height>=480?114:86);}
        public int loanRows(){return Math.max(1,(height-loanTop()-88)/18);}
        public int controlHeight(){return height>=480?32:18;}
        public int controlY(int row){return rowY(row)+(rowHeight-gap-controlHeight())/2;}
    }
}
