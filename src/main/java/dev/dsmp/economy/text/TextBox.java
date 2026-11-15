package dev.dsmp.economy.text;

import java.util.UUID;

public final class TextBox {
    public static final int SOLID = 0, ACROSS = 1, DOWN = 2;
    public String id = UUID.randomUUID().toString();
    public String text = "New text";
    public float x = .5f, y = .5f, size = 1;
    public int startColor = 0x49E8D4, endColor = 0x55C900;
    public int gradient = SOLID, font = 0, align = 0, backgroundOpacity = 80, backgroundStyle = 1;
    public boolean visible = true, shadow = false;
    public java.util.List<LineStyle> lineStyles=new java.util.ArrayList<>();
    public LineStyle style(int index){return index<lineStyles.size()?lineStyles.get(index):new LineStyle();}
    public LineStyle editLine(int index){if(index<0||index>=lines().length)throw new IllegalArgumentException("Unknown text line");while(lineStyles.size()<=index)lineStyles.add(new LineStyle());return lineStyles.get(index);}
    public void trimStyles(){while(lineStyles.size()>lines().length)lineStyles.removeLast();}
    public String[] lines() { return text.split("\\n", -1); }
    public void validate() {
        if (id == null || id.isBlank() || id.length() > 64 || text == null || text.length() > 1024)
            throw new IllegalArgumentException("Invalid text box content (maximum 1024 characters)");
        if (lines().length > 24) throw new IllegalArgumentException("A text box supports up to 24 lines");
        for (String line : lines()) if (line.codePointCount(0,line.length()) > 160) throw new IllegalArgumentException("Each line supports up to 160 characters");
        if (text.codePoints().anyMatch(c -> Character.isISOControl(c) && c != '\n')) throw new IllegalArgumentException("Unsupported text control character");
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(size) || x < 0 || x > 1 || y < 0 || y > 1 || size < .4f || size > 4)
            throw new IllegalArgumentException("Invalid text box size or position");
        if ((startColor & 0xFF000000) != 0 || (endColor & 0xFF000000) != 0 || gradient < 0 || gradient > 2
                || font < 0 || font > 3 || align < 0 || align > 2 || backgroundOpacity < 0 || backgroundOpacity > 255 || backgroundStyle < 0 || backgroundStyle > 2)
            throw new IllegalArgumentException("Invalid text box colors or design");
        if(lineStyles==null||lineStyles.size()>lines().length)throw new IllegalArgumentException("Invalid line styles");
        for(var style:lineStyles){if(style==null)throw new IllegalArgumentException("Missing line style");style.validate();}
    }
    public TextBox copy() {
        var copy = new TextBox(); copy.id=id; copy.text=text; copy.x=x; copy.y=y; copy.size=size;
        copy.startColor=startColor; copy.endColor=endColor; copy.gradient=gradient; copy.font=font; copy.align=align;
        copy.backgroundOpacity=backgroundOpacity; copy.backgroundStyle=backgroundStyle; copy.visible=visible; copy.shadow=shadow;copy.lineStyles=new java.util.ArrayList<>(lineStyles.stream().map(LineStyle::copy).toList()); return copy;
    }
    public void restore(TextBox saved){var c=saved.copy();text=c.text;x=c.x;y=c.y;size=c.size;startColor=c.startColor;endColor=c.endColor;gradient=c.gradient;font=c.font;align=c.align;backgroundOpacity=c.backgroundOpacity;backgroundStyle=c.backgroundStyle;visible=c.visible;shadow=c.shadow;lineStyles=c.lineStyles;}
    public static int rgb(String input) {
        String value = input.trim().replaceFirst("^#", "");
        if (!value.matches("[a-fA-F0-9]{6}")) throw new IllegalArgumentException("Colors use 6 hex digits, such as 49E8D4");
        return Integer.parseInt(value,16);
    }
    public static int blend(int start, int end, double fraction) {
        double t = Math.clamp(fraction,0,1); int result=0xFF000000;
        for(int shift : new int[]{16,8,0}) {
            int a=start>>shift&255,b=end>>shift&255;
            result |= (int)Math.round(a+(b-a)*t)<<shift;
        }
        return result;
    }
}
