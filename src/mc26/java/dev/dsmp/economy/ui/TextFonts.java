package dev.dsmp.economy.ui;



final class TextFonts {
    static final String[] NAMES={"Inter","Inter bold","Minecraft","Bubble"};
    private static final net.minecraft.network.chat.FontDescription BUBBLE=new net.minecraft.network.chat.FontDescription.Resource(net.minecraft.resources.Identifier.fromNamespaceAndPath("economy_tracker","bubble"));
    private TextFonts(){}
    static net.minecraft.network.chat.Component text(String text,int font){return font==3?net.minecraft.network.chat.Component.literal(text).withStyle(style->style.withFont(BUBBLE)):font==2?net.minecraft.network.chat.Component.literal(text):HudFont.text(text,font==1);}
    static int width(String text,int font){return font==3?net.minecraft.client.Minecraft.getInstance().font.width(text(text,font)):font==2?net.minecraft.client.Minecraft.getInstance().font.width(text):HudFont.width(text,font==1);}
}
