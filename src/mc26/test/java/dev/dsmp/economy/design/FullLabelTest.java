package dev.dsmp.economy.design;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FullLabelTest {
    private double width(String text){return text.codePointCount(0,text.length())*6d;}
    private void bounds(FullLabel.Fit fit,int w,int h){for(var line:fit.lines())assertTrue(width(line)*fit.scale()<=w+.001);assertTrue(fit.lines().size()*fit.lineHeight()*fit.scale()<=h+.001);assertTrue(fit.scale()>0);}
    @Test void ordinaryLabelsRemainCompleteOnOneLine(){var fit=FullLabel.fit("Overview",110,16,.82f,11,this::width);assertEquals(List.of("Overview"),fit.lines());assertEquals(.82f,fit.scale());bounds(fit,110,16);}
    @Test void wrappingRetainsEveryWordAndUsesTheAvailableHeight(){var fit=FullLabel.fit("Background blur: Medium",70,22,.92f,11,this::width);assertEquals("Background blur: Medium",String.join(" ",fit.lines()));assertEquals(2,fit.lines().size());assertFalse(fit.lines().stream().anyMatch(s->s.contains("…")));bounds(fit,70,22);}
    @Test void narrowButtonsRetainLongNamesAndUnicodeWithoutSplittingWords(){for(var label:List.of("Player background","Inventory themes","Thatchacats","🌲 漂亮的风景","OneUnbrokenVeryLongProfileName")){var fit=FullLabel.fit(label,38,8,.82f,14.3f,this::width);assertEquals(label,String.join(" ",fit.lines()));bounds(fit,38,8);}}
    @Test void fullWordsFitAcrossCompactSizesAndAccessibleTextScales(){for(int w:new int[]{20,40,80,142,240})for(int h:new int[]{8,14,22})for(float access:new float[]{1,1.15f,1.3f})for(String label:List.of("HUD modules","Inventory themes","Player background","Before accessibility change","Save PNG")){var fit=FullLabel.fit(label,w,h,.92f,11*access,s->width(s)*access);assertEquals(label,String.join(" ",fit.lines()));for(String line:fit.lines())assertTrue(width(line)*access*fit.scale()<=w+.001);assertTrue(fit.lines().size()*fit.lineHeight()*fit.scale()<=h+.001);}}
}
