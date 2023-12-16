package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.targets.OpenableTarget;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;

public class InteractHelper extends TargetHelper<OpenableTarget> {
    public InteractHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    public void collectDefaultBlocks() {
        Node source = getSource();
        Node destination = getDestination();

        //collectBlocks(source, 2);
        //collectBlocks(destination, 2);
    }

    public void collectBlock(int x, int y, int z) {
        BlockData data = new BlockPos(x, y, z).toBlock(getWorld()).getBlockData();

        if (!(data instanceof Openable)) return;
        Openable openable = (Openable) data;

        OpenableTarget target = getTarget(x, y, z);

        if (target == null) {
            target = new OpenableTarget(getNPC(), x, y, z, openable);
            addTarget(target);
            return;
        }

        target.updateOpenable(openable);
    }

    @Override
    public double getCost() {
        Movement movement = getMovement();

        for (OpenableTarget target : getTargets()) {
            boolean locked = target.isLocked();
            if (locked && !target.isOpen(movement)) return Cost.INFINITY;
        }

        return 0;
    }

    public boolean tick() {
        if (!hasTargets()) return false;
        Movement movement = getMovement();

        for (OpenableTarget target : getTargets()) {
            if (target.isOpen(movement)) {
                removeTarget(target);
                continue;
            }
            if (target.continueOpening()) return true;
        }

        return false;
    }
}
