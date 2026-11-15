package dev.dsmp.economy.text;

import dev.dsmp.economy.TrackerConfig;
import java.util.*;

public final class TextBoxEditor {
    private final TrackerConfig config;
    private final List<TextBox> original;
    private int selected;
    public TextBoxEditor(TrackerConfig config) { this.config=config; original=config.textBoxes.stream().map(TextBox::copy).toList(); }
    public TextBox selected() { return config.textBoxes.isEmpty()?null:config.textBoxes.get(Math.min(selected,config.textBoxes.size()-1)); }
    public String label() { return config.textBoxes.isEmpty()?"No boxes":"Box "+(selected+1)+"/"+config.textBoxes.size(); }
    public void select(String id) { for(int i=0;i<config.textBoxes.size();i++) if(config.textBoxes.get(i).id.equals(id)) selected=i; }
    public void next() { if(!config.textBoxes.isEmpty()) selected=(selected+1)%config.textBoxes.size(); }
    public TextBox add() {
        if(config.textBoxes.size()>=32) throw new IllegalArgumentException("Up to 32 text boxes are supported");
        var box=new TextBox(); config.textBoxes.add(box); selected=config.textBoxes.size()-1; return box;
    }
    public void delete() { if(selected()!=null) { config.textBoxes.remove(selected); selected=Math.min(selected,Math.max(0,config.textBoxes.size()-1)); } }
    public void fields(String text, String start, String end) {
        var box=selected(); if(box==null)return;
        var next=box.copy(); next.text=text.replace("\\n","\n"); next.startColor=TextBox.rgb(start); next.endColor=TextBox.rgb(end); next.validate();
        box.text=next.text; box.startColor=next.startColor; box.endColor=next.endColor;
    }
    public void size(float change) { if(selected()!=null) selected().size=Math.clamp(Math.round((selected().size+change)*100)/100f,.4f,4); }
    public void preset() { if(selected()!=null) { selected().startColor=0x49E8D4; selected().endColor=0x55C900; selected().gradient=TextBox.DOWN; } }
    public void cancel() { config.textBoxes=new ArrayList<>(original.stream().map(TextBox::copy).toList()); }
}
