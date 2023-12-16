package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.npc.Pose;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.targets.PlaceableTarget;

public class PlaceHelper extends TargetHelper<PlaceableTarget> {
    public PlaceHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    public void collectBlock(int x, int y, int z) {
        BlockType type = getCacheManager().getBlockType(new BlockPos(x, y, z));
        if (type.isSolid()) return;

        if (!hasTarget(x, y, z)) {
            PlaceableTarget target = new PlaceableTarget(getNPC(), new BlockPos(x, y, z));
            addTarget(target);
        }
    }

    @Override
    public double getCost() {
        Movement movement = getMovement();
        boolean hasSupport = false;

        if (!movement.isDiagonal3D()) {
            Node source = getSource();
            BlockType type = getCacheManager().getBlockType(source);
            hasSupport = type != BlockType.WATER;
            Movement sourceMovement = source.getMovement();
            if (sourceMovement != null) hasSupport = sourceMovement.getPlaceHelper().hasTargets();
        }

        for (PlaceableTarget target : getTargets()) {
            BlockPos pos = target.getPos();
            // outside height limit
            if (pos.getY() > getWorld().getMaxHeight()) return Cost.INFINITY;
            if (!hasSupport/* && !PlaceController.canPlaceAt(pos)*/) return Cost.INFINITY;
        }

        return Cost.PLACE_BLOCK * getTargets().size();
    }

    public boolean tick(boolean moveIfBlocked) {
        if (!hasTargets()) return false;

        for (PlaceableTarget target : getTargets()) {
            if (target.isPlaced()) {
                removeTarget(target);
                continue;
            }

            BlockPos pos = target.getPos();

            boolean blocked = false; //AwarenessController.isBlockingPos(pos);
            if (blocked && moveIfBlocked) {
                Movement movement = getMovement();
                movement.getPositionHelper().centerOnSource();
                return true;
            }

            if (!target.isPlaced()) { // continuePlacing();
                getNPC().setPose(Pose.SNEAKING);
                return true;
            }
        }

        return false;
    }
}
