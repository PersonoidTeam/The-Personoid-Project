package com.personoid.api.pathfindingold.movement.movements;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.MovementState;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;

public class PillarMovement extends Movement {
    public PillarMovement(NPC npc, Node source, Node dest) {
        super(npc, source, dest);
    }

    @Override
    public void updateHelpers() {
        //getBreakHelper().collectBlocks(getSource(), 2, 1);
        //getPlaceHelper().collectBlock(getSource());

        //getInteractHelper().collectDefaultBlocks();
    }

    @Override
    public double getCost() {
        if (getPlaceHelper().getCost() >= Cost.INFINITY) return Cost.INFINITY;

        double cost = Cost.JUMP;
        cost += getBreakHelper().getCost();
        cost += getInteractHelper().getCost();
        cost += Cost.PLACE_BLOCK;

        return cost + super.getCost();
    }

    @Override
    public void tick() {
        // interacting with something
        if (getBreakHelper().tick() || getInteractHelper().tick()) return;

        // at center of source
        if (!getPositionHelper().centerOnSource()) return;

        getPlaceHelper().tick(false);

        boolean jump = getJumpHelper().canJump();
        if (jump) getNPC().getMoveController().jump();
    }

    @Override
    public MovementState getState() {
        if (!getNPC().isOnGround()) return MovementState.PROCEEDING;
        return super.getState();
    }
}
