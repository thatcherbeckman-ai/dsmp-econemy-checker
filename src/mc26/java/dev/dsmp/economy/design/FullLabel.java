package dev.dsmp.economy.design;

import java.util.List;
import java.util.function.ToDoubleFunction;

/** Fits complete words without ellipses. A second line is used only when it improves readability. */
public final class FullLabel {
    public record Fit(List<String> lines,float scale,float lineHeight){}
    private FullLabel(){}
    public static Fit fit(String value,int width,int height,float preferred,float lineHeight,ToDoubleFunction<String> measure){
        String text=value==null?"":value.strip().replaceAll("\\s+"," ");int w=Math.max(1,width),h=Math.max(1,height);
        float scale=scale(List.of(text),w,h,preferred,lineHeight,measure);var best=new Fit(List.of(text),scale,lineHeight);
        for(int i=0;i<text.length();i++)if(text.charAt(i)==' '){var lines=List.of(text.substring(0,i),text.substring(i+1));float candidate=scale(lines,w,h,preferred,lineHeight,measure);if(candidate>best.scale*1.06f)best=new Fit(lines,candidate,lineHeight);}
        return best;
    }
    private static float scale(List<String> lines,int w,int h,float preferred,float lineHeight,ToDoubleFunction<String> measure){double max=1;for(String line:lines)max=Math.max(max,measure.applyAsDouble(line));return (float)Math.min(preferred,Math.min(w/max,h/(lineHeight*lines.size())));}
}
