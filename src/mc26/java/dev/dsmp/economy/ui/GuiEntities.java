package dev.dsmp.economy.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.joml.Vector3fc;
import org.joml.Quaternionfc;

/** Native picture-in-picture entities ignore the GUI pose; explicitly map their bounds and size. */
final class GuiEntities {
    static ScreenRectangle bounds(Matrix3x2fc pose,int left,int top,int right,int bottom){return new ScreenRectangle(left,top,right-left,bottom-top).transformAxisAligned(pose);}
    static float scale(Matrix3x2fc pose){return (float)Math.hypot(pose.m00(),pose.m01());}
    static void entity(GuiGraphicsExtractor g,EntityRenderState state,float size,Vector3fc translation,Quaternionfc rotation,Quaternionfc camera,int left,int top,int right,int bottom){
        var box=bounds(g.pose(),left,top,right,bottom);g.entity(state,size*scale(g.pose()),translation,rotation,camera,box.left(),box.top(),box.right(),box.bottom());
    }
    static void player(GuiGraphicsExtractor g,int left,int top,int right,int bottom,int size,float offset,float mouseX,float mouseY,LivingEntity player){
        var box=bounds(g.pose(),left,top,right,bottom);var mouse=g.pose().transformPosition(new Vector2f(mouseX,mouseY));
        net.minecraft.client.gui.screens.inventory.InventoryScreen.extractEntityInInventoryFollowsMouse(g,box.left(),box.top(),box.right(),box.bottom(),Math.max(1,Math.round(size*scale(g.pose()))),offset,mouse.x(),mouse.y(),player);
    }
}
