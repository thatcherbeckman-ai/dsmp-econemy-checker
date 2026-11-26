package dev.dsmp.economy.menubackground;

/** Shared photo palette for the foreground terrain, atmosphere and avatar light level. */
public record SceneLighting(int color,int sky,int block) {
    public static SceneLighting of(MenuScene scene){return switch(scene){
        case CHERRY_DAWN->new SceneLighting(0xE6B7B5,14,12);case CHERRY_DUSK->new SceneLighting(0x969DCF,11,11);
        case LANTERN_GARDEN->new SceneLighting(0xDAB28F,12,13);case AUTUMN_RIVER->new SceneLighting(0xD7B181,14,12);
        case WINTER_CABIN,AURORA_LAKE->new SceneLighting(0x91B6D4,12,12);case MISTY_PINES,ENCHANTED_FOREST->new SceneLighting(0x91B3AB,12,12);
        case ALPINE_SUNRISE->new SceneLighting(0xE3C2B7,14,12);case OCEAN_SUNSET->new SceneLighting(0xE5AF98,13,13);
        case TROPICAL_LAGOON->new SceneLighting(0xA9D4CA,15,12);case DESERT_DUNES->new SceneLighting(0xDBB28D,14,12);
        case LAVENDER_FIELDS->new SceneLighting(0xC2AECF,14,12);case STARRY_MOUNTAINS->new SceneLighting(0x7B8EBE,10,11);
        case RAINY_CITY->new SceneLighting(0x9B87B5,11,12);case SPACE_NEBULA->new SceneLighting(0xA48AC7,10,12);
        case FLOATING_ISLANDS->new SceneLighting(0xC8D2BE,15,12);case BAMBOO_FOREST->new SceneLighting(0xAABD97,13,12);
        default->new SceneLighting(0xC3B4C6,14,12);
    };}
}
