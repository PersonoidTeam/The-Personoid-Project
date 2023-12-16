package com.personoid.api.pathfindingold.movement.movements;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import org.bukkit.Location;

public class DiagonalMovement extends Movement {
    public DiagonalMovement(NPC npc, Node source, Node dest) {
        super(npc, source, dest);
    }

    @Override
    public void updateHelpers() {
        getInteractHelper().collectDefaultBlocks();
    }

    @Override
    public double getCost() {
        double cost = getInteractHelper().getCost();
        return cost + super.getCost();
    }

    public void tick() {
        if (getInteractHelper().tick()) return;

        Location lookPos = getDestination().toLocation(getNPC().getWorld());
        getNPC().face(lookPos);

        getNPC().getMoveController().moveForward(true);
        getNPC().setSprinting(true);
    }
}
