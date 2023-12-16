package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.CacheManager;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.movements.ParkourMovement;
import org.bukkit.World;

public class ParkourHelper extends Helper {
    public ParkourHelper(NPC npc, ParkourMovement movement) {
        super(npc, movement);
    }

    @Override
    public double getCost() {
        ParkourMovement movement = (ParkourMovement) getMovement();
        int dis = movement.getDistance();

        Node source = getSource();
        Node destination = getDestination();

        if (isObstructed(getWorld(), destination, 2)) return Cost.INFINITY;

        int startX = source.getX();
        int startY = source.getY();
        int startZ = source.getZ();

        int dx = movement.getDeltaX();
        int dz = movement.getDeltaZ();

        int height = 3 + movement.getDeltaY();

        for (int i = 0; i < dis; i++) {
            int x = startX + dx * i;
            int z = startZ + dz * i;
            if (isObstructed(getWorld(), x, startY, z, height)) return Cost.INFINITY;
        }

        boolean sprint = shouldSprint();
        double cost = sprint ? Cost.SPRINT_STRAIGHT : Cost.WALK_STRAIGHT;

        return cost * dis;
    }

    public boolean shouldSprint() {
        ParkourMovement movement = (ParkourMovement) getMovement();
        double dis = movement.getDistance() + movement.getDeltaY();
        return dis > 3;
    }

    public static boolean isObstructed(World world, Node n, int height) {
        return isObstructed(world, n, 0, height);
    }

    public static boolean isObstructed(World world, Node n, int offset, int height) {
        int x = n.getX();
        int y = n.getY();
        int z = n.getZ();
        return isObstructed(world, x, y, z, offset, height);
    }

    public static boolean isObstructed(World world, int x, int y, int z, int height) {
        return isObstructed(world, x, y, z, 0, height);
    }

    public static boolean isObstructed(World world, int x, int y, int z, int offset, int height) {
        for (int i = 0; i < height; i++) {
            if (isObstructed(world, x, y + offset + i, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isObstructed(World world, int x, int y, int z) {
        BlockType type = CacheManager.get(world).getBlockType(new BlockPos(x, y, z));
        if (!type.isAir()) return true;

        float speedMul = StepHelper.getSpeedMultiplier(world, x, y, z);
        return speedMul != 1;
    }
}
