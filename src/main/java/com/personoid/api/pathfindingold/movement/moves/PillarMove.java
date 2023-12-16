package com.personoid.api.pathfindingold.movement.moves;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.Pathfinder;
import com.personoid.api.pathfindingold.movement.Move;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.movement.movements.PillarMovement;

public class PillarMove extends Move {
    public PillarMove(int x, int y, int z) {
        super(x, y, z);
    }

    @Override
    public Movement apply(NPC npc, Node node, Pathfinder finder) {
        Node destination = finder.getAdjacentNode(node, getDeltaX(), getDeltaY(), getDeltaZ());
        return new PillarMovement(npc, node, destination);
    }
}
