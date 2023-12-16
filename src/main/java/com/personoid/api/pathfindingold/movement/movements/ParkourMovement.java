package com.personoid.api.pathfindingold.movement.movements;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.movement.helpers.ParkourHelper;

public class ParkourMovement extends Movement {
    private final int dx;
    private final int dy;
    private final int dz;
    private final int distance;

    private final ParkourHelper parkourHelper;

    public ParkourMovement(NPC npc, Node source, Node dest, int dx, int dy, int dz, int dist) {
        super(npc, source, dest);
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.distance = dist;
        this.parkourHelper = new ParkourHelper(npc, this);
    }

    @Override
    public double getCost() {
        double cost = parkourHelper.getCost();
        return cost + super.getCost();
    }

    @Override
    public void tick() {
        boolean sprint = parkourHelper.shouldSprint();
        getNPC().setSprinting(sprint);

        boolean prepared = getPositionHelper().prepareParkourJump();
        if (!prepared) return;

        boolean jump = getJumpHelper().canJump();
        if (jump) getNPC().getMoveController().jump();
    }

    public int getDeltaX() {
        return dx;
    }

    public int getDeltaY() {
        return dy;
    }

    public int getDeltaZ() {
        return dz;
    }

    public int getDistance() {
        return distance;
    }
}
