package dev.dsmp.economy.ui;

import org.joml.Matrix3x2f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuiEntitiesTest {
    @Test void nativePlayerBoundsFollowTheWindowAndNestedPanelTransform(){
        var window=MenuWindow.of(480,350);var pose=new Matrix3x2f().translation(window.x(),window.y()).scale(window.scale()).translate(150,60);
        var box=GuiEntities.bounds(pose,8,4,72,54);assertEquals(window.screenX(158),box.left(),1);assertEquals(window.screenY(64),box.top(),1);assertEquals(window.screenX(222),box.right(),1);assertEquals(window.screenY(114),box.bottom(),1);assertEquals(window.scale(),GuiEntities.scale(pose),.0001);
    }
    @Test void fullScreenPlayerWorkspacesKeepTheirExistingCoordinates(){
        var box=GuiEntities.bounds(new Matrix3x2f(),20,30,100,200);assertEquals(20,box.left());assertEquals(30,box.top());assertEquals(100,box.right());assertEquals(200,box.bottom());assertEquals(1,GuiEntities.scale(new Matrix3x2f()));
    }
}
