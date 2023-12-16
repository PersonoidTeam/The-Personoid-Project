package com.personoid.api.pathfindingold.movement.movements;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.MovementState;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;

public class AscendMovement extends Movement {
    public AscendMovement(NPC npc, Node source, Node dest) {
        super(npc, source, dest);
    }

    @Override
    public void updateHelpers() {
        //getBreakHelper().collectBlocks(getDestination(), 2);
        //getBreakHelper().collectBlocks(getSource(), 2, 1);

        Node dest = getDestination();
        getPlaceHelper().collectBlock(dest.getX(), dest.getY() - 1, dest.getZ());

        getInteractHelper().collectDefaultBlocks();
    }

    @Override
    public double getCost() {
        double cost = Cost.JUMP + getBreakHelper().getCost();

        cost += getPlaceHelper().getCost();
        cost += getInteractHelper().getCost();

        return cost + super.getCost();
    }

    @Override
    public void tick() {
        // is interacting with something
        if (getBreakHelper().tick() || getInteractHelper().tick()) return;

        if (!getPlaceHelper().tick(true)) {
            getNPC().face(getDestination().toLocation(getWorld()));

            getNPC().getMoveController().moveForward(true);
            getNPC().setSprinting(true);

            boolean jump = getJumpHelper().shouldJump();
            if (jump) getNPC().getMoveController().jump();
        }
    }

    @Override
    public MovementState getState() {
        if (getPlaceHelper().hasTargets()) return MovementState.PROCEEDING;
        return super.getState();
    }
}
