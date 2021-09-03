package us.notnotdoddy.personoid.npc.resourceGathering;

import org.bukkit.Material;

public enum ResourceTypes {
    ORES("_ore"),
    DIAMOND_ORE("diamond_ore"),
    IRON_ORE(true, "iron_ore"),
    GOLD_ORE(true,"gold_ore"),
    LAPIS_ORE("lapis_ore"),
    REDSTONE_ORE("redstone_ore"),
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
