package com.personoid.utils.values;

import org.bukkit.Material;

public class BlockTypes {
    public static boolean isClimbable(Material material) {
        return material == Material.LADDER || material == Material.VINE || material == Material.WATER || material == Material.WEEPING_VINES ||
                material == Material.TWISTING_VINES;
    }
}
