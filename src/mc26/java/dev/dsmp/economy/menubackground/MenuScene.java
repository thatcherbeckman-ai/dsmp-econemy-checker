package dev.dsmp.economy.menubackground;

public enum MenuScene {
    ORIGINAL("Original Christmas","",0,Particles.AUTO),
    CHERRY_DAWN("Cherry blossom dawn","spring",0,Particles.PETALS),
    CHERRY_DUSK("Cherry blossom moonlight","spring",1,Particles.PETALS),
    LANTERN_GARDEN("Lantern garden","spring",2,Particles.PETALS),
    AUTUMN_RIVER("Autumn river","spring",3,Particles.LEAVES),
    WINTER_CABIN("Winter cabin","spring",4,Particles.SNOW),
    AURORA_LAKE("Aurora lake","spring",5,Particles.SNOW),
    MISTY_PINES("Misty pines","nature",0,Particles.FIREFLIES),
    ENCHANTED_FOREST("Enchanted forest","nature",1,Particles.FIREFLIES),
    ALPINE_SUNRISE("Alpine sunrise","nature",2,Particles.STARS),
    OCEAN_SUNSET("Ocean sunset","nature",3,Particles.STARS),
    TROPICAL_LAGOON("Tropical lagoon","nature",4,Particles.FIREFLIES),
    DESERT_DUNES("Desert dunes","nature",5,Particles.NONE),
    LAVENDER_FIELDS("Lavender fields","fantasy",0,Particles.FIREFLIES),
    STARRY_MOUNTAINS("Starry mountains","fantasy",1,Particles.STARS),
    RAINY_CITY("Rainy city","fantasy",2,Particles.RAIN),
    SPACE_NEBULA("Space nebula","fantasy",3,Particles.STARS),
    FLOATING_ISLANDS("Floating islands","fantasy",4,Particles.STARS),
    BAMBOO_FOREST("Bamboo forest","fantasy",5,Particles.FIREFLIES),
    GRADIENT("Simple gradient","",0,Particles.NONE),
    CUSTOM("Your imported PNG","",0,Particles.PETALS),
    MINECRAFT_SNOW("Minecraft snow overlook","",0,Particles.NONE);
    public enum Particles {AUTO,NONE,PETALS,SNOW,FIREFLIES,STARS,LEAVES,RAIN,CONFETTI}
    public final String label,atlas;public final int tile;public final Particles particles;
    MenuScene(String label,String atlas,int tile,Particles particles){this.label=label;this.atlas=atlas;this.tile=tile;this.particles=particles;}
}
