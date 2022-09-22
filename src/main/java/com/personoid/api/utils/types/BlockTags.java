package com.personoid.api.utils.types;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public enum BlockTags {
    SOLID,
    LIQUID,
    CLIMBABLE;

    public boolean is(Location location) {
        return is(location.getBlock());
    }

    public boolean is(Block block) {
        return is(block.getType());
    }

    public boolean is(Material material) {
        String name = material.name().toLowerCase();
        switch (this) {
            case SOLID: return material.isSolid() || name.contains("leaves");
            case LIQUID: return material == Material.WATER || material == Material.LAVA;
            case CLIMBABLE: return material == Material.LADDER || name.contains("vine");
        }
        return false;
    }
}
