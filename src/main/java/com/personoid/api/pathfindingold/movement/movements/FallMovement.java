package com.personoid.api.pathfindingold.movement.movements;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.movement.helpers.FallHelper;

public class FallMovement extends Movement {
    private final int fallDistance;
    private final FallHelper fallHelper;

    public FallMovement(NPC npc, Node source, Node dest, int fallDistance) {
        super(npc, source, dest);
        this.fallDistance = fallDistance;
        this.fallHelper = new FallHelper(npc, this);
    }

    @Override
    public void updateHelpers() {
        int x = getDestination().getX();
        int y = getSource().getY();
        int z = getDestination().getZ();

/*        getBreakHelper().collectBlocks(x, y, z, -1, 3);

        getInteractHelper().collectDefaultBlocks();
        getInteractHelper().collectBlocks(x, y, z, -1, 3);*/
    }

    @Override
    public double getCost() {
        double cost = Cost.fall(fallDistance);

        cost += getBreakHelper().getCost();
        cost += getInteractHelper().getCost();

        cost += fallHelper.getCost();

        return cost + super.getCost();
    }

    @Override
    public void tick() {
        // check if currently interacting with something
        if (getBreakHelper().tick() || getInteractHelper().tick()) return;
        fallHelper.tick();

        Node dest = getDestination();
        getNPC().face(dest.toLocation(getNPC().getWorld())); // TODO: second param: look down (true)

        BlockPos pos = getNPC().getBlockPos();
        boolean aboveDestination = pos.getX() == dest.getX() && pos.getZ() == dest.getZ();

        getNPC().getMoveController().moveForward(!aboveDestination || getNPC().isOnGround());
        getNPC().setSprinting(true);
    }

    public int getFallDistance() {
        return fallDistance;
    }
}
