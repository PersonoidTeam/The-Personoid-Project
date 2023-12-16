package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.BlockType;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.targets.BreakableTarget;
import org.bukkit.block.Block;

public class BreakHelper extends TargetHelper<BreakableTarget> {
    public BreakHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    public void collectBlock(int x, int y, int z) {
        BlockType type = getCacheManager().getBlockType(new BlockPos(x, y, z));
        if (type.isPassable()) return;

        if (!hasTarget(x, y, z)) {
            BreakableTarget target = new BreakableTarget(getNPC(), new BlockPos(x, y, z));
            addTarget(target);
        }
    }

    @Override
    public double getCost() {
        int sum = 0;
        for (BreakableTarget target : getTargets()) {
            BlockPos pos = target.getPos();
            while (true) {
                sum += (int) costOfBlock(pos);
                pos = pos.above();
                if (hasTarget(pos)) break;

                Block block = pos.toBlock(getNPC().getWorld());
                boolean falls = block.getType().hasGravity();
                if (!falls) break;
            }
        }

        return sum;
    }

    private double costOfBlock(BlockPos pos) {
        BlockType type = getCacheManager().getBlockType(pos);
        boolean unbreakable = !type.isBreakable();
        if (unbreakable) return Cost.INFINITY;

        BlockPos from = getSource().getPos();
        boolean safe = true; // AwarenessController.isSafeToBreak(pos, from, true, false);
        if (!safe) return Cost.INFINITY;

        return 30; // Cost.breakCost(pos);
    }

    public boolean tick() {
        if (!hasTargets()) return false;

        for (BreakableTarget target : getTargets()) {
            if (target.isBroken()) {
                BlockPos pos = target.getPos();

                boolean falling = false; // AwarenessController.awaitsFallingBlock(pos);
                if (falling) return true;

                removeTarget(target);
                continue;
            }

            // if (target.continueBreaking (false)) return true;
        }

        return false;
    }
}
