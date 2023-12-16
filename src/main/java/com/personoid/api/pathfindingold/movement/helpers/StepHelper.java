package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;

public class StepHelper extends Helper {
    private static final HashMap<Material, Float> SPEED_MULTIPLIERS = new HashMap<>();

    static {
        SPEED_MULTIPLIERS.put(Material.WATER, 0.5F);
        SPEED_MULTIPLIERS.put(Material.COBWEB, 0.25F);
        SPEED_MULTIPLIERS.put(Material.SWEET_BERRY_BUSH, 0.8F);
    }

    public StepHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    // TODO slowness effect from diagonally adjacent blocks
    @Override
    public double getCost() {
        Movement movement = getMovement();
        Node destination = getDestination();

        boolean inWater = destination.getType(getWorld()) == BlockType.WATER;

        boolean isDiagonal = movement.isDiagonal();

        boolean needsSupport = isDiagonal || movement.isDownwards();

        int x = destination.getX();
        int y = destination.getY();
        int z = destination.getZ();

        if (needsSupport && !inWater) {
            BlockType type = getCacheManager().getBlockType(new BlockPos(x, y - 1, z));
            if (!type.isSolid()) return Cost.INFINITY;
        }

        if (movement.isVerticalOnly()) return 0;
        float speed = 1;

        if (!inWater) {
            Block block = new BlockPos(x, y - 1, z).toBlock(getWorld());
            speed = block.getType().getSlipperiness();
/*            BlockState state = BlockInterface.getState(x, y - 1, z);
            Block block = state.getBlock();
            speed = block.getVelocityMultiplier();*/
        }

        float m1 = getSpeedMultiplier(getWorld(), x, y, z);
        float m2 = getSpeedMultiplier(getWorld(), x, y + 1, z);
        speed *= Math.min(m1, m2);

        double cost = isDiagonal ? Cost.SPRINT_DIAGONALLY : Cost.SPRINT_STRAIGHT;
        return cost / speed;
    }

    public static float getSpeedMultiplier(World world, int x, int y, int z) {
        Block block = new BlockPos(x, y - 1, z).toBlock(world);
        return SPEED_MULTIPLIERS.getOrDefault(block.getType(), 1f);
    }
}
