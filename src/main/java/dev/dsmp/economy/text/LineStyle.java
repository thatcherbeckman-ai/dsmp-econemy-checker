package dev.dsmp.economy.text;

/** Optional line override. Text styling inherits the box until Custom is selected. */
public final class LineStyle {
    public static final int PLAIN=0,PILL=1,CARD=2;
    public boolean custom=false,outline=true,bullet=false;
    public int font=3,gradient=0,startColor=0xFFFFFF,endColor=0xFFFFFF,outlineColor=0x18151D;
    public int shape=PLAIN,backgroundColor=0x151219,backgroundOpacity=190,bulletColor=0xD8AF5C,noteColor=0xD8AF5C;
    public float textSize=1,iconSize=14;
    public String image="",note="";
    public void validate(){
        if(font<0||font>3||gradient<0||gradient>2||shape<0||shape>2||backgroundOpacity<0||backgroundOpacity>255||!Float.isFinite(textSize)||textSize<.5f||textSize>2.5f||!Float.isFinite(iconSize)||iconSize<8||iconSize>32)throw new IllegalArgumentException("Invalid line design or size");
        for(int color:new int[]{startColor,endColor,outlineColor,backgroundColor,bulletColor,noteColor})if((color&0xFF000000)!=0)throw new IllegalArgumentException("Line colors use six hex digits");
        if(note==null||note.codePointCount(0,note.length())>80||note.codePoints().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Side note supports up to 80 characters");
        if(image==null||image.length()>160||!image.isEmpty()&&!image.matches("(?:builtin:[a-z_]+|file:[a-zA-Z0-9_.-]+\\.png)"))throw new IllegalArgumentException("Choose an image from the image picker");
        if(image.startsWith("file:")&&(image.substring(5).startsWith(".")||image.contains("..")))throw new IllegalArgumentException("Invalid image filename");
    }
    public LineStyle copy(){var c=new LineStyle();c.custom=custom;c.outline=outline;c.bullet=bullet;c.font=font;c.gradient=gradient;c.startColor=startColor;c.endColor=endColor;c.outlineColor=outlineColor;c.shape=shape;c.backgroundColor=backgroundColor;c.backgroundOpacity=backgroundOpacity;c.bulletColor=bulletColor;c.noteColor=noteColor;c.textSize=textSize;c.iconSize=iconSize;c.image=image;c.note=note;return c;}
}
