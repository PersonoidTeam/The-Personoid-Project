package com.personoid.api.pathfindingold.movement.movements;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import org.bukkit.Location;

public class DescendMovement extends Movement {
    public DescendMovement(NPC npc, Node source, Node dest) {
        super(npc, source, dest);
    }

    @Override
    public void updateHelpers() {
        int height = 3;
        if (isVerticalOnly()) height = 1;

        //getBreakHelper().collectBlocks(getDestination(), height);
        //getInteractHelper().collectDefaultBlocks();
    }

    @Override
    public double getCost() {
        double cost = isVerticalOnly() ? Cost.fall(1) : 0;

        cost += getBreakHelper().getCost();
        cost += getInteractHelper().getCost();

        return cost + super.getCost();
    }

    public void tick() {
        // if interacting with something
        if (getBreakHelper().tick() || getInteractHelper().tick()) return;

        Location lookPos = getDestination().toLocation(getNPC().getWorld());
        getNPC().face(lookPos/*, isVerticalOnly()*/); // lookDown
        if (isVerticalOnly() && !getNPC().isOnGround()) return;

        getNPC().getMoveController().moveForward(true);
        getNPC().setSprinting(true);
    }
}
