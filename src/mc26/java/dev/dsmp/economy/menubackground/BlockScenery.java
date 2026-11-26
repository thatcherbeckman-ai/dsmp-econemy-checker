package dev.dsmp.economy.menubackground;

/** Materials and atmosphere for the same scenic choices in cinematic voxel form. */
public record BlockScenery(String top,String side,String rock,String trunk,String leaves,int foliage,int sky,int horizon,int fog,int glow,boolean night,Kind kind) {
    public enum Kind {FOREST,SNOW,OCEAN,DESERT,CITY,SPACE,ISLANDS,BAMBOO}
    /** The illustrated foreground follows the picture's ground, which can differ from its biome. */
    public record Ground(String top,String side,String rock,int color,int accent){}
    public static Ground foreground(MenuScene scene){return switch(scene){
        case CHERRY_DAWN->new Ground("moss_block","mossy_cobblestone","stone",0x99A18E,0xE7ABBC);
        case CHERRY_DUSK->new Ground("moss_block","mossy_cobblestone","stone",0x70768D,0xD2A4D5);
        case LANTERN_GARDEN->new Ground("stone_bricks","stone_bricks","stone",0x9A957B,0);
        case AUTUMN_RIVER->new Ground("podzol_top","podzol_side","stone",0xA68C62,0xD29C56);
        case WINTER_CABIN,MINECRAFT_SNOW,ORIGINAL->new Ground("snow","snow","stone",0xC7D2DC,0);
        case AURORA_LAKE->new Ground("snow","packed_ice","stone",0x8CA7B5,0);
        case MISTY_PINES->new Ground("moss_block","mossy_cobblestone","stone",0x819A8B,0);
        case ENCHANTED_FOREST->new Ground("moss_block","mossy_cobblestone","stone",0x74908F,0xA0BDD2);
        case ALPINE_SUNRISE->new Ground("stone","stone","stone",0xB6ADA0,0);
        case OCEAN_SUNSET->new Ground("sand","sandstone","sandstone",0xCEB9A5,0);
        case TROPICAL_LAGOON->new Ground("sand","sandstone","sandstone",0xD5CEAC,0);
        case DESERT_DUNES->new Ground("sand","sandstone","sandstone",0xD3B48C,0);
        case LAVENDER_FIELDS->new Ground("moss_block","mossy_cobblestone","stone",0xA08A9F,0x9C70BF);
        case STARRY_MOUNTAINS->new Ground("stone","stone","stone",0x737E96,0);
        case RAINY_CITY->new Ground("stone_bricks","stone_bricks","stone",0x716877,0);
        case SPACE_NEBULA->new Ground("amethyst_block","amethyst_block","deepslate_tiles",0x8F7DA8,0);
        case FLOATING_ISLANDS->new Ground("moss_block","mossy_cobblestone","stone",0xA2AC87,0xC7C8AB);
        case BAMBOO_FOREST->new Ground("mossy_stone_bricks","mossy_stone_bricks","stone",0x91A581,0);
        default->new Ground("moss_block","mossy_cobblestone","stone",0x91A080,0);
    };}
    public static BlockScenery of(MenuScene scene){return switch(scene){
        case CHERRY_DAWN->forest("cherry",0xFFFFFF,0x666594,0xF1CFBC,0xD6B8C4,0xFFDEAC,false);
        case CHERRY_DUSK->forest("cherry",0xDDD5FF,0x171F46,0x807AA9,0x9B9DC8,0xC7D8FF,true);
        case LANTERN_GARDEN->forest("cherry",0xFFE3CF,0x22263C,0xB493A4,0xAEA7AD,0xFFC173,true);
        case AUTUMN_RIVER->forest("oak",0xD99B47,0x725D82,0xE8BB93,0xC6B39B,0xFFE0A6,false);
        case WINTER_CABIN,MINECRAFT_SNOW,ORIGINAL->new BlockScenery("snow","snow","stone","spruce_log","spruce_leaves",0x9CAEB1,0x11182E,0x869FF3,0xA3B5EB,0xCDDFFF,true,Kind.SNOW);
        case AURORA_LAKE->new BlockScenery("snow","snow","packed_ice","spruce_log","spruce_leaves",0x839C9D,0x102D40,0x608CA3,0x92BAC2,0x69E6C9,true,Kind.SNOW);
        case MISTY_PINES->forest("spruce",0x799D88,0x435C70,0xCBD6CA,0xB4CCC5,0xF6E5BD,false);
        case ENCHANTED_FOREST->forest("oak",0x6CBBAC,0x23254B,0x6A729C,0x839AB1,0x9BF0D3,true);
        case ALPINE_SUNRISE->forest("spruce",0x9BB37C,0x87709C,0xF4C3A2,0xDCC1BA,0xFFE4A8,false);
        case OCEAN_SUNSET->open("sand","sandstone","stone",0x654D88,0xF8B486,0xDBC0B8,0xFFE6A0,false,Kind.OCEAN);
        case TROPICAL_LAGOON->new BlockScenery("sand","sandstone","stone","oak_log","oak_leaves",0x71C58E,0x488AB7,0xC0E6E2,0xB7DBDC,0xFFF4BF,false,Kind.OCEAN);
        case DESERT_DUNES->open("sand","sandstone","sandstone",0x75588A,0xF2BD94,0xDEBFA7,0xFFE5A5,false,Kind.DESERT);
        case LAVENDER_FIELDS->forest("cherry",0xB9A2EF,0x71618D,0xEDD1CD,0xC9C0D7,0xFFE5BA,false);
        case STARRY_MOUNTAINS->open("snow","snow","stone",0x101735,0x7187BA,0x8C9DBF,0xD3DFFF,true,Kind.SNOW);
        case RAINY_CITY->open("deepslate_tiles","deepslate_tiles","stone",0x252743,0x77758E,0x9E9EB1,0xC7B3EB,true,Kind.CITY);
        case SPACE_NEBULA->open("deepslate_tiles","deepslate_tiles","blue_ice",0x15132F,0x6D5E9A,0x9A86BB,0xE6B6FC,true,Kind.SPACE);
        case FLOATING_ISLANDS->new BlockScenery("grass_block_top","grass_block_side","stone","oak_log","oak_leaves",0xA3BC81,0x5689B7,0xD9E2D4,0xC6D7D8,0xFFF2BD,false,Kind.ISLANDS);
        case BAMBOO_FOREST->new BlockScenery("moss_block","moss_block","stone","bamboo_block","oak_leaves",0x9EC16F,0x4E6F79,0xDCE0B5,0xB9C9AB,0xFFF1B9,false,Kind.BAMBOO);
        default->forest("oak",0x8AB67C,0x7188A0,0xE1DAD0,0xBBCBC2,0xFFE8B7,false);
    };}
    private static BlockScenery forest(String tree,int leaves,int sky,int horizon,int fog,int glow,boolean night){return new BlockScenery("grass_block_top","grass_block_side","stone",tree+"_log",tree+"_leaves",leaves,sky,horizon,fog,glow,night,Kind.FOREST);}
    private static BlockScenery open(String top,String side,String rock,int sky,int horizon,int fog,int glow,boolean night,Kind kind){return new BlockScenery(top,side,rock,"oak_log","oak_leaves",0x95B78A,sky,horizon,fog,glow,night,kind);}
}
