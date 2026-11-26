package dev.dsmp.economy.ui;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PanelViewportTest {
    @Test void pagesFitRightHandPanelWithoutCoveringSidebar(){for(int w:new int[]{220,480,651,748,960})for(int h:new int[]{180,270,403,520}){var p=PanelViewport.of(175,60,w,h);assertTrue(p.x(p.contentWidth())<=175+w+.01);assertTrue(p.y(p.contentHeight())<=60+h+.01);assertTrue(p.scale()>0);assertFalse(p.contains(100,80));assertTrue(p.contains(176,61));}}
    @Test void pointerCoordinatesRemainInvertibleInsideNestedPanels(){var p=PanelViewport.of(150,60,480,270);for(double x:new double[]{0,24,200,639})for(double y:new double[]{0,20,150,359}){assertEquals(x,p.localX(p.x(x)),.001);assertEquals(y,p.localY(p.y(y)),.001);}}
    @Test void invalidPanelDimensionsFailBeforeRendering(){assertThrows(IllegalArgumentException.class,()->PanelViewport.of(0,0,0,100));assertThrows(IllegalArgumentException.class,()->PanelViewport.of(0,0,100,-1));}
}
