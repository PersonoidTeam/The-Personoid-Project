package com.personoid.api.pathfindingold.movement.movements;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.MovementState;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;

public class StraightMovement extends Movement {
    public StraightMovement(NPC npc, Node source, Node dest) {
        super(npc, source, dest);
    }

    @Override
    public void updateHelpers() {
        Node destination = getDestination();

        //getBreakHelper().collectBlocks(destination, 2);
        //BlockType type = destination.getType();
        //if (type != BlockType.WATER) getPlaceHelper().collectBlock(destination, -1);

        //getInteractHelper().collectDefaultBlocks();
    }

    @Override
    public double getCost() {
        double cost = getBreakHelper().getCost();

        cost += getPlaceHelper().getCost();
        cost += getInteractHelper().getCost();

        return cost + super.getCost();
    }

    @Override
    public void tick() {
        boolean interacting = getBreakHelper().tick() || getInteractHelper().tick();
        if (interacting) return;

        if (!getPlaceHelper().tick(false)) {
            getNPC().face(getDestination().toLocation(getWorld()));

            getNPC().getMoveController().moveForward(true);
            getNPC().setSprinting(true);
        }
    }

    @Override
    public MovementState getState() {
        if (getPlaceHelper().hasTargets()) return MovementState.PROCEEDING;
        return super.getState();
    }
}
