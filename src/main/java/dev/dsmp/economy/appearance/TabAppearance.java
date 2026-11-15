package dev.dsmp.economy.appearance;

/** One independent appearance for a HUD view or widget. Colors are RGB. */
public final class TabAppearance {
    public static final int GLASS = 0, OUTLINE = 1, SQUARE = 2, MINIMAL = 3;
    public static final String[] DESIGNS = {"Glass", "Outline", "Square", "Minimal"};
    public float size = 1;
    public int accent = -2974791 & 0xFFFFFF;
    public int background = 0x0F1722;
    public int text = -1380105 & 0xFFFFFF;
    public int positive = -10688352 & 0xFFFFFF;
    public int negative = -1416577 & 0xFFFFFF;
    public int opacity = -1; // Inherit the existing global glass opacity until edited.
    public int design = GLASS;
    public TabAppearance copy() {
        var copy = new TabAppearance();
        copy.size = size; copy.accent = accent; copy.background = background; copy.text = text;
        copy.positive = positive; copy.negative = negative; copy.opacity = opacity; copy.design = design;
        return copy;
    }
    public void validate() {
        if (!Float.isFinite(size) || size < .5f || size > 2f || opacity != -1 && (opacity < 40 || opacity > 255)
                || design < 0 || design >= DESIGNS.length) throw new IllegalArgumentException("Invalid appearance settings");
        for (int color : new int[]{accent, background, text, positive, negative})
            if ((color & 0xFF000000) != 0) throw new IllegalArgumentException("Appearance colors must be RGB");
    }
    public int color(int role) {
        return switch (role) { case 0 -> accent; case 1 -> background; case 2 -> text; case 3 -> positive; case 4 -> negative;
            default -> throw new IllegalArgumentException("Unknown color role"); };
    }
    public void color(int role, int rgb) {
        if ((rgb & 0xFF000000) != 0) throw new IllegalArgumentException("Invalid RGB color");
        switch (role) { case 0 -> accent = rgb; case 1 -> background = rgb; case 2 -> text = rgb;
            case 3 -> positive = rgb; case 4 -> negative = rgb; default -> throw new IllegalArgumentException("Unknown color role"); }
    }
}
