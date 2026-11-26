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
    static int width(String value,boolean bold){return Math.round(Minecraft.getInstance().font.width(text(value,bold))*dev.dsmp.economy.design.DesignAccess.scale());}
}
