package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;

public class BumpHelper extends Helper {
    public BumpHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    @Override
    public double getCost() {
        if (!getMovement().isDiagonal()) return 0;

        Node source = getSource();
        Node dest = getDestination();

        if (isBlocked(dest)) return Cost.INFINITY;

        int y = Math.max(source.getY(), dest.getY());

        boolean blocked1 = isBlocked(source.getX(), y, dest.getZ());
        boolean blocked2 = isBlocked(dest.getX(), y, source.getZ());

        if (blocked1 && blocked2) return Cost.INFINITY;
        return blocked1 || blocked2 ? Cost.BUMP_INTO_CORNER : 0;
    }

    private boolean isBlocked(Node node) {
        return isBlocked(node.getX(), node.getY(), node.getZ());
    }

    private boolean isBlocked(int x, int y, int z) {
        // height offset
        for (int i = 0; i < 2; i++) {
            BlockType type = getCacheManager().getBlockType(new BlockPos(x, y + i, z));
            if (!type.isPassable()) return true;
        }
        return false;
    }
}
