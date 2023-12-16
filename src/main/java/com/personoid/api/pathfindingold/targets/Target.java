package com.personoid.api.pathfindingold.targets;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import org.bukkit.World;

public class Target {
    private final NPC npc;
    private final BlockPos pos;

    public Target(NPC npc, BlockPos pos) {
        this.npc = npc;
        this.pos = pos;
    }

    public NPC getNPC() {
        return npc;
    }

    public BlockPos getPos() {
        return pos;
    }

    public World getWorld() {
        return npc.getWorld();
    }

    public boolean isSelected(boolean lookAt) {
        // is currently looking at the target
        return true;
    }
}
