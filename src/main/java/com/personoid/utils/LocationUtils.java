package com.personoid.utils;

import com.personoid.handlers.NPCHandler;
import com.personoid.npc.NPC;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LocationUtils {
    public static NPC getClosestNPC(Location location) {
        NPC closestNPC = null;
        for (NPC npc : NPCHandler.getNPCs()) {
            if (closestNPC == null || npc.getLocation().distance(location) < closestNPC.getLocation().distance(location)) {
                closestNPC = npc;
            }
        }
        return closestNPC;
    }

    public static Block getBlockInDir(Location location, BlockFace direction) {
        while (true) {
            location = location.getBlock().getRelative(direction).getLocation();
            if (location.getBlock().getType().isSolid()) {
                return location.getBlock();
            }
        }
    }
}
