package com.personoid.api.pathfindingold.targets;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;

public class BreakableTarget extends Target {
    private boolean broken;

    public BreakableTarget(NPC npc, BlockPos pos) {
        super(npc, pos);
    }

    public boolean isBroken() {
        return broken;
    }
}
