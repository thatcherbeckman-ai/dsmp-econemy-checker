package dev.dsmp.economy.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;

final class HudFont {
    private static final FontDescription HUD = new FontDescription.Resource(Identifier.fromNamespaceAndPath((String)"economy_tracker", (String)"hud"));
    private static final FontDescription DISPLAY = new FontDescription.Resource(Identifier.fromNamespaceAndPath((String)"economy_tracker", (String)"display"));

    private HudFont() {
    }

    static Component text(String text, boolean display) {
        return Component.literal((String)text).withStyle(style -> style.withFont(display ? DISPLAY : HUD));
    }

    static int width(String text) {
        return HudFont.width(text, false);
    }

    static int width(String text, boolean display) {
        return Minecraft.getInstance().font.width((FormattedText)HudFont.text(text, display));
    }

    static String fit(String text, int width) {
        if (HudFont.width(text) <= width) {
            return text;
        }
        String tail = "\u2026";
        if (HudFont.width(tail) > width) {
            return "";
        }
        int end = text.length();
        while (end > 0 && HudFont.width(text.substring(0, end) + tail) > width) {
            end = text.offsetByCodePoints(end, -1);
        }
        return text.substring(0, end) + tail;
    }
}
