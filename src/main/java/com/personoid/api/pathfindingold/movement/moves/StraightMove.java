package com.personoid.api.pathfindingold.movement.moves;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.Pathfinder;
import com.personoid.api.pathfindingold.movement.Move;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.movement.movements.StraightMovement;

public class StraightMove extends Move {
    public StraightMove(int dx, int dy, int dz) {
        super(dx, dy, dz);
    }

    @Override
    public Movement apply(NPC npc, Node node, Pathfinder finder) {
        Node dest = finder.getAdjacentNode(node, getDeltaX(), getDeltaY(), getDeltaZ());
        return new StraightMovement(npc, node, dest);
    }
}
