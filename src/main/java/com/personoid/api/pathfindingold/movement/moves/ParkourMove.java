package com.personoid.api.pathfindingold.movement.moves;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.CacheManager;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.Pathfinder;
import com.personoid.api.pathfindingold.movement.Move;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.movement.helpers.ParkourHelper;
import com.personoid.api.pathfindingold.movement.movements.ParkourMovement;

public class ParkourMove extends Move {
    private static final int MAX_DISTANCE = 4;

    public ParkourMove(int x, int y, int z) {
        super(x, y, z);
    }

    @Override
    public Movement apply(NPC npc, Node node, Pathfinder finder) {
        if (ParkourHelper.isObstructed(npc.getWorld(), node, 2, 1)) return null;

        int dx = getDeltaX();
        int dz = getDeltaZ();

        int dy = 0;
        int i = 1;

        while (i <= MAX_DISTANCE) {
            int ox = dx * i;
            int oz = dz * i;

            Node destination = finder.getAdjacentNode(node, ox, dy, oz);

            if (ParkourHelper.isObstructed(npc.getWorld(), destination, 3)) {
                if (dy == 1) return null;
                dy++;
                continue;
            }

            int x = destination.getX();
            int y = destination.getY() - 1;
            int z = destination.getZ();

            BlockType type = CacheManager.get(npc.getWorld()).getBlockType(new BlockPos(x, y, z));

            if (!type.isPassable()) {
                if (i == 1) return null;
                return new ParkourMovement(npc, node, destination, dx, dy, dz, i);
            }

            i++;
        }

        return null;
    }
}
