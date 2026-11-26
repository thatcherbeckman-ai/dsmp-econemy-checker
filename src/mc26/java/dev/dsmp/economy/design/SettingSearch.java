package dev.dsmp.economy.design;
import java.util.*;
public final class SettingSearch {
    public record Item(String label,String tags,String target,int tab){}
    private static final List<Item> ITEMS=List.of(
        new Item("Player scenery","background landscape picture image cinematic","scene",0),new Item("Character view and size","skin player cape angle camera rotate turn tilt","scene",1),new Item("Scene captions and fonts","title subtitle text color font name username bubble","scene",2),new Item("Scene placement and dragging","position layout drag snap alignment size","scene",3),new Item("Scene lighting and shadows","light direction strength softness blur exposure","scene",4),
        new Item("Menu background pictures","scenery images gallery holiday christmas","background",0),new Item("Background colors and blur","tint brightness darkness vignette opacity","background",1),new Item("Background animation","particles snow speed motion petals","background",2),new Item("Import background PNG","custom image file picture zoom fit","background",3),
        new Item("Panel appearance","hud color size design glass edges","appearance",0),new Item("Text boxes","font bubble gradient colors words custom","text",0),new Item("HUD placement","drag layout position panels","layout",0),new Item("Inventory themes","holiday christmas halloween new year animated decorations","inventory",0),
        new Item("Saved design profiles","look preset save switch named","tools",0),new Item("Backups and restore","recover history automatic undo settings","tools",1),new Item("Tracking controls","disable pause stop recording hide bets profit player wins nets privacy","privacy",0),new Item("Accessibility","large text contrast reduced motion readable","tools",2));
    private SettingSearch(){}
    public static List<Item> find(String query){if(query==null||query.isBlank())return ITEMS;String[] words=query.toLowerCase(Locale.ROOT).trim().split("\\s+");return ITEMS.stream().filter(item->{String text=(item.label+" "+item.tags).toLowerCase(Locale.ROOT);return Arrays.stream(words).allMatch(text::contains);}).toList();}
}
