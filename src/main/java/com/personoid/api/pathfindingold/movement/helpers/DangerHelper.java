package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;

public class DangerHelper extends Helper {
    public DangerHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    @Override
    public double getCost() {
        Node source = getSource();
        Node destination = getDestination();

        if (isNearDanger(destination)) return Cost.INFINITY;

        int x = destination.getX();
        int y = destination.getY();
        int z = destination.getZ();

        BlockType type = getCacheManager().getBlockType(new BlockPos(x, y + 1, z));
        if (type == BlockType.WATER) return Cost.INFINITY;

        Movement movement = getMovement();
        if (!movement.isDiagonal()) return 0;

        int x2 = source.getX();
        int z2 = source.getZ();
        int y2 = Math.max(source.getY(), destination.getY());

        boolean nearDanger = isNearDanger(x, y2, z2) || isNearDanger(x2, y2, z);
        return nearDanger ? Cost.INFINITY : 0;
    }

    private boolean isNearDanger(Node node) {
        int x = node.getX();
        int y = node.getY();
        int z = node.getZ();
        return isNearDanger(x, y, z);
    }

    private boolean isNearDanger(int x, int y, int z) {
        // height offset
        for (int i = 0; i < 3; i++) {
            BlockType type = getCacheManager().getBlockType(new BlockPos(x, y, z));
            boolean isDangerous = type == BlockType.DANGER;
            if (isDangerous) return true;
        }
        return false;
    }
}
