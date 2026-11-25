package dev.dsmp.economy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/** A menu-only type scale; HUD and custom text keep their existing fonts and metrics. */
final class MenuText {
    private static final FontDescription NORMAL=new FontDescription.Resource(Identifier.fromNamespaceAndPath("economy_tracker","menu"));
    private static final FontDescription BOLD=new FontDescription.Resource(Identifier.fromNamespaceAndPath("economy_tracker","menu_bold"));
    private MenuText(){}
    static Component text(String value,boolean bold){return Component.literal(value).withStyle(style->style.withFont(bold?BOLD:NORMAL));}
    static int width(String value){return width(value,false);}
    static int width(String value,boolean bold){return Minecraft.getInstance().font.width(text(value,bold));}
    static String fit(String value,int available,boolean bold){
        if(width(value,bold)<=available)return value;String ellipsis="\u2026";if(width(ellipsis,bold)>available)return "";
        int end=value.length();while(end>0&&width(value.substring(0,end)+ellipsis,bold)>available)end=value.offsetByCodePoints(end,-1);
        return value.substring(0,end)+ellipsis;
    }
}
