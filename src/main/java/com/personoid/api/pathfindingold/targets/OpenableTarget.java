package com.personoid.api.pathfindingold.targets;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.movement.Movement;
import org.bukkit.block.Block;
import org.bukkit.block.Lockable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;

public class OpenableTarget extends Target {
    private Openable openable;

    public OpenableTarget(NPC npc, int x, int y, int z, Openable openable) {
        super(npc, new BlockPos(x, y, z));
        updateOpenable(openable);
    }

    public void updateOpenable(Openable openable) {
        this.openable = openable;
    }

    public boolean continueOpening() {
        if (isSelected(true)) {
            getNPC().interact();
            return true;
        }
        getNPC().face(getPos().toLocation(getWorld())); // return lookAt(false)
        return true;
    }

    public boolean isLocked() {
        Block block = getPos().toBlock(getWorld());
        Lockable lockable = (Lockable) block;
        return lockable.isLocked();
    }

    public boolean isOpen(Movement movement) {
        BlockData data = getPos().toBlock(getWorld()).getBlockData();
        Openable openable = (Openable) data;
        return openable.isOpen();
        // depends on movement?
    }

    public Openable getOpenable() {
        return openable;
    }
}
