package com.personoid.api.utils.types;

import com.personoid.api.pathfinding.calc.utils.BlockPos;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public enum BlockTags {
    SOLID,
    LIQUID,
    CLIMBABLE;

    public boolean is(BlockPos blockPos, World world) {
        return is(blockPos.toLocation(world));
    }

    public boolean is(Location location) {
        return is(location.getBlock());
    }

    public boolean is(Block block) {
        return is(block.getType());
    }

    public boolean is(Material material) {
        switch (this) {
            case SOLID: return isSolid(material);
            case LIQUID: return isLiquid(material);
            case CLIMBABLE: return isClimbable(material);
        }
        return false;
    }

    private boolean isSolid(Material material) {
        String name = material.name().toLowerCase();
        return material.isSolid() || name.contains("leaves");
    }

    private boolean isLiquid(Material material) {
        return material == Material.WATER || material == Material.LAVA;
    }

    private boolean isClimbable(Material material) {
        String name = material.name().toLowerCase();
        return material == Material.LADDER || name.contains("vine");
    }
}
