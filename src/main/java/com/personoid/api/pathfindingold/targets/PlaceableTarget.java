package com.personoid.api.pathfindingold.targets;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;

public class PlaceableTarget extends Target {
    private boolean placed;

    public PlaceableTarget(NPC npc, BlockPos pos) {
        super(npc, pos);
    }

    public boolean isPlaced() {
        return placed;
    }
}
