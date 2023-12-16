package com.personoid.api.pathfindingold.movement.moves;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.Pathfinder;
import com.personoid.api.pathfindingold.movement.Move;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.movement.movements.DescendMovement;
import com.personoid.api.pathfindingold.movement.movements.FallMovement;

public class DescendMove extends Move {
    public DescendMove(int dx, int dy, int dz) {
        super(dx, dy, dz);
    }

    @Override
    public Movement apply(NPC npc, Node node, Pathfinder finder) {
        int dx = getDeltaX();
        int dy = getDeltaY();
        int dz = getDeltaZ();

        Node destination;

        while (true) {
            destination = finder.getAdjacentNode(node, dx, dy, dz);
            if (destination.getY() <= 0 || destination.getType(npc.getWorld()) == BlockType.WATER) break;

            Node below = finder.getAdjacentNode(node, dx, dy - 1, dz);
            BlockType type = below.getType(npc.getWorld());
            if (!type.isPassable()) break;

            dy--;
        }

        int fallDistance = node.getY() - destination.getY();
        if (fallDistance == 1) return new DescendMovement(npc, node, destination);

        return new FallMovement(npc, node, destination, fallDistance);
    }
}
