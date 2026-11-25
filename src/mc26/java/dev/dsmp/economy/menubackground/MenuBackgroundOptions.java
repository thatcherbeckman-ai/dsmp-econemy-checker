package dev.dsmp.economy.menubackground;

/** Independent cosmetic preferences; drafts never share mutable state with saved preferences. */
public final class MenuBackgroundOptions {
    public MenuScene scene=MenuScene.ORIGINAL;
    public int brightness=100,overlay=0,blur=0,tint=0,tintAmount=15,vignette=0,cards=93,zoom=100;
    public boolean animations=true,parallax=false,contain=false;
    public MenuScene.Particles particles=MenuScene.Particles.AUTO;
    public int density=1,speed=100,particleOpacity=65,decorations=0;
    public String customImage="";
    public static final String[] TINTS={"None","Rose","Lavender","Teal","Amber","Midnight"};
    public static final int[] TINT_COLORS={0,0xF2A6BE,0xA997DC,0x68B9AD,0xEBC17A,0x344779};
    public static final String[] LEVELS={"Off","Low","Medium","High"},DENSITIES={"Low","Medium","High"},DECORATIONS={"Auto","Off","Christmas"};
    public MenuBackgroundOptions copy(){var n=new MenuBackgroundOptions();n.scene=scene;n.brightness=brightness;n.overlay=overlay;n.blur=blur;n.tint=tint;n.tintAmount=tintAmount;n.vignette=vignette;n.cards=cards;n.zoom=zoom;n.animations=animations;n.parallax=parallax;n.contain=contain;n.particles=particles;n.density=density;n.speed=speed;n.particleOpacity=particleOpacity;n.decorations=decorations;n.customImage=customImage;return n;}
    public void validate(){
        if(scene==null||particles==null||customImage==null||(!customImage.isEmpty()&&!customImage.matches("[0-9a-f]{64}\\.png")))throw new IllegalArgumentException("Invalid menu background selection");
        if(brightness<50||brightness>150||overlay<0||overlay>80||blur<0||blur>3||tint<0||tint>=TINTS.length||tintAmount<0||tintAmount>50||vignette<0||vignette>3||cards<45||cards>100||zoom<100||zoom>150||density<0||density>2||speed<25||speed>200||particleOpacity<20||particleOpacity>100||decorations<0||decorations>2)throw new IllegalArgumentException("Menu background option is outside its range");
    }
    public double time(double seconds){return animations&&Double.isFinite(seconds)?Math.max(0,seconds)%4096*speed/100d:0;}
    public MenuScene.Particles effectiveParticles(){return particles==MenuScene.Particles.AUTO?scene.particles:particles;}
}
