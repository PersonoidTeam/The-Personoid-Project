package com.personoid.api.pathfindingold;

import org.bukkit.Material;

public class BlockType {
    public static final BlockType AIR = new BlockType("air", Material.AIR).passable(true).air(true);
    public static final BlockType SOLID = new BlockType("solid", Material.STONE).solid(true).breakable(true);
    public static final BlockType UNBREAKABLE = new BlockType("unbreakable", Material.BEDROCK).solid(true);
    public static final BlockType WATER = new BlockType("water", Material.WATER).passable(true);
    public static final BlockType DANGER = new BlockType("danger", Material.LAVA);

    private final String name;
    private final Material material;
    private boolean passable;
    private boolean solid;
    private boolean breakable;
    private boolean air;

    private BlockType(String name, Material material) {
        this.name = name;
        this.material = material;
    }

    private BlockType passable(boolean passable) {
        this.passable = passable;
        return this;
    }

    private BlockType solid(boolean solid) {
        this.solid = solid;
        return this;
    }

    private BlockType breakable(boolean breakable) {
        this.breakable = breakable;
        return this;
    }

    private BlockType air(boolean air) {
        this.air = air;
        return this;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isPassable() {
        return passable;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public boolean isAir() {
        return air;
    }
}
