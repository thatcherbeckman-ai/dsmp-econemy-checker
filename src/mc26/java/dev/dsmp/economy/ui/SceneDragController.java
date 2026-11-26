package dev.dsmp.economy.ui;

import dev.dsmp.economy.design.SceneDrag;
import dev.dsmp.economy.menubackground.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.function.*;

/** One history step per pointer gesture; actual rendered bounds are used for hit testing. */
final class SceneDragController {
    record Viewport(int x,int y,int w,int h){boolean contains(double mx,double my){return w>0&&h>0&&mx>=x&&my>=y&&mx<x+w&&my<y+h;}}
    private final Supplier<MenuBackgroundOptions> source;private final Consumer<MenuBackgroundOptions> completed;
    private int mode;private double offsetX,offsetY;private MenuBackgroundOptions before;
    SceneDragController(Supplier<MenuBackgroundOptions> source,Consumer<MenuBackgroundOptions> completed){this.source=source;this.completed=completed;}
    boolean active(){return mode!=0;}
    boolean click(Viewport box,double mx,double my){
        if(!box.contains(mx,my))return false;var c=source.get().character;var text=CharacterSceneRenderer.captionBounds(box.x,box.y,box.w,box.h,c);var p=CharacterPlacement.of(c,box.w,box.h);
        if(text.contains(mx,my)){mode=2;offsetX=mx-(text.x()+text.width()/2d);offsetY=my-text.y();}
        else if(mx>=box.x+p.centerX()-p.bodyHeight()*.45&&mx<=box.x+p.centerX()+p.bodyHeight()*.45&&my>=box.y+p.feetY()-p.bodyHeight()&&my<=box.y+p.feetY()+4){mode=1;offsetX=mx-box.x-p.centerX();offsetY=my-box.y-p.feetY();}
        else return false;before=source.get().copy();return true;
    }
    boolean drag(Viewport box,double mx,double my){if(mode==0)return false;var c=source.get().character;if(mode==1)SceneDrag.character(c,mx-box.x-offsetX,my-box.y-offsetY,box.w,box.h);else SceneDrag.caption(c,mx-box.x-offsetX,my-box.y-offsetY,box.w,box.h);return true;}
    boolean release(){if(mode==0)return false;mode=0;completed.accept(before);before=null;return true;}
    void guides(GuiGraphicsExtractor g,Viewport box){if(mode==0)return;int color=0xA8D7B4FF;for(int target:new int[]{25,50,75}){int x=box.x+box.w*target/100;g.fill(x,box.y,x+1,box.y+box.h,color);}for(int target:new int[]{10,50,78,83,90}){int y=box.y+box.h*target/100;g.fill(box.x,y,box.x+box.w,y+1,color);}}
}
