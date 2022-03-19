package com.notnotdoddy.personoid.utils.other;

import com.notnotdoddy.personoid.handlers.NPCHandler;
import com.notnotdoddy.personoid.npc.NPC;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LocationUtils {
    public static NPC getClosestNPC(Location location) {
        NPC closestNPC = null;
        for (NPC npc : NPCHandler.getNpcs()) {
            if (closestNPC == null || npc.getLocation().distance(location) < closestNPC.getLocation().distance(location)) {
                closestNPC = npc;
            }
        }
        return closestNPC;
    }

    public static Block getBlockInDir(Location location, BlockFace direction) {
        Block block = location.getBlock();
        do block = block.getRelative(direction);
        while (!block.getRelative(direction).getType().isSolid());
        return block;
    }
}
