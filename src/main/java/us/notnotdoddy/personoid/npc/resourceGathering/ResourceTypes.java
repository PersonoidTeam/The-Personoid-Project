package us.notnotdoddy.personoid.npc.resourceGathering;

import org.bukkit.Material;

public enum ResourceTypes {
    ORES("_ore"),
    DIAMOND_ORE("diamond"),
    IRON_ORE(true, "iron"),
    GOLD_ORE(true,"gold"),
    LAPIS_ORE("lapis"),
    BLOCK("_block"),
    REDSTONE_ORE("redstone_ore"),
    SMELTED_ORE("_ingot", "redstone", "diamond", "gold", "lapis"),
    LOG("_log"),
    STONE("stone"),
    PLANKS("_planks"),

    // lol
    COAL("coal")
    ;

    private final String[] materialStringComparison;
    public boolean shouldBeSmelted = false;

    ResourceTypes(String... materialStrings){
        materialStringComparison = materialStrings;
    }
    ResourceTypes(boolean shouldBeSmelted ,String... materialStrings){
        this.shouldBeSmelted = shouldBeSmelted;
        materialStringComparison = materialStrings;
    }

    public boolean contains(Material material){
        for (String string : materialStringComparison){
            if (material.toString().toLowerCase().contains(string.toLowerCase())){
                return true;
            }
        }
        return false;
    }
}
