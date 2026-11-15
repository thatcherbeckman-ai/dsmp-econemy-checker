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
        var next=box.copy(); next.text=text.replace("\\n","\n"); next.startColor=TextBox.rgb(start); next.endColor=TextBox.rgb(end);next.trimStyles(); next.validate();
        box.text=next.text; box.startColor=next.startColor; box.endColor=next.endColor;box.lineStyles=next.lineStyles;
    }
    public void size(float change) { if(selected()!=null) selected().size=Math.clamp(Math.round((selected().size+change)*100)/100f,.4f,4); }
    public void preset() { if(selected()!=null) { selected().startColor=0x49E8D4; selected().endColor=0x55C900; selected().gradient=TextBox.DOWN; } }
    public void reference(String username){
        var b=selected();if(b==null)b=add();b.text="IGN "+username+"\nMAX 25 BILLION\nMINIMUM 750M\nBlackjack Multihand\nMINIMUM 500M\nBlackjack\nBaccarat\nRussian Roulette\nMINIMUM 300M\nMexican Jack\n45/45/10\nHigher or Lower\nOdd or Even";
        b.font=3;b.align=0;b.shadow=false;b.startColor=0xFFFFFF;b.endColor=0xFFFFFF;b.gradient=0;b.backgroundStyle=0;b.size=1.4f;b.lineStyles.clear();
        int[] headings={0,1,2,4,8};String[] icons={"cards","cards","cards","roulette","joker","gem","arrows","dice"};int icon=0;
        for(int i=0;i<b.lines().length;i++){var s=b.editLine(i);s.custom=true;s.outline=true;s.startColor=s.endColor=0xFFFFFF;boolean heading=false;for(int h:headings)if(i==h)heading=true;
            if(heading){s.textSize=i==0?1.9f:i==1?1.25f:.8f;s.startColor=s.endColor=i==0?0xFFFFFF:i<3?0xE9B85E:i==4?0xA5BCE2:0xD4AD70;}
            else{s.shape=LineStyle.PILL;s.bullet=true;s.bulletColor=i==3?0xD8AF5C:i<8?0x9EB5DF:0xD39A53;s.image="builtin:"+icons[icon++];s.note=i==6?"Tie = Loss":i==11?"Tie = Loss":i==12?"Creeper = Loss":i==10||i==7?"":"Tie = DD";}
        }b.validate();
    }
    public void cancel() { config.textBoxes=new ArrayList<>(original.stream().map(TextBox::copy).toList()); }
}
