package dev.dsmp.economy.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MenuWindowTest {
    @Test void windowAlwaysLeavesWorldVisibleAndStaysCentered(){
        for(int width:new int[]{240,320,480,960,1280,1920,3840})for(int height:new int[]{160,240,350,700,1080,2160}){
            var w=MenuWindow.of(width,height);assertTrue(w.scale()>0&&w.scale()<=1);assertTrue(w.x()>0&&w.y()>0);assertEquals(width-w.x(),w.screenX(w.width()),.001);assertEquals(height-w.y(),w.screenY(w.height()),.001);assertTrue(w.width()*w.scale()<=width*.881);assertTrue(w.height()*w.scale()<=height*.821);assertTrue(w.width()>=640&&w.height()>=360);
        }
    }
    @Test void pointerCoordinatesRoundTripAtEveryScale(){
        for(int[] size:new int[][]{{320,240},{480,350},{960,700},{2560,1440}}){var w=MenuWindow.of(size[0],size[1]);for(double x:new double[]{0,15.5,210,w.width()-1})for(double y:new double[]{0,20.25,100,w.height()-1}){assertEquals(x,w.localX(w.screenX(x)),.0001);assertEquals(y,w.localY(w.screenY(y)),.0001);assertTrue(w.contains(w.screenX(x),w.screenY(y)));}assertFalse(w.contains(0,0));assertFalse(w.contains(size[0]-1,size[1]-1));}
    }
    @Test void invalidViewportIsRejected(){assertThrows(IllegalArgumentException.class,()->MenuWindow.of(0,500));assertThrows(IllegalArgumentException.class,()->MenuWindow.of(500,-1));}
    @Test void tabSlideIsBoundedSmoothAndCompletesWithoutOvershoot(){
        var motion=new TabMotion();long start=1_000_000_000L;assertEquals(0,motion.offset(start,false));motion.start(start,1);assertEquals(28,motion.offset(start,false));float previous=28;for(long t=0;t<=TabMotion.DURATION;t+=1_000_000){float current=motion.offset(start+t,false);assertTrue(current>=0&&current<=previous);previous=current;}assertEquals(0,motion.offset(start+TabMotion.DURATION,false));assertEquals(0,motion.offset(start+TabMotion.DURATION*2,false));motion.start(start,-1);assertEquals(-28,motion.offset(start,false));
    }
    @Test void reducedMotionAndRapidSelectionDoNotLeaveResidualOffsets(){
        var motion=new TabMotion();long now=1_000_000_000L;motion.start(now,1);assertEquals(0,motion.offset(now,true));motion.start(now+10,-1);assertEquals(-28,motion.offset(now+10,false));assertEquals(0,motion.offset(now+10+TabMotion.DURATION,false));assertEquals(0,motion.offset(now-1,false));
    }
}
