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
        return switch (this) {
            case SOLID -> material.isSolid() || name.contains("leaves");
            case LIQUID -> material == Material.WATER || material == Material.LAVA;
            case CLIMBABLE -> material == Material.LADDER || name.contains("vine");
        };
    }
}
