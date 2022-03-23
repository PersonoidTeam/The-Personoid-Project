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

    public static Block getAirInDir(Location location, BlockFace direction) {
        while (true) {
            location = location.getBlock().getRelative(direction).getLocation();
            if (location.getBlock().getType().isAir()) {
                return location.getBlock();
            }
        }
    }

    public static Location getNear(Location from, Location to, float distance) {
        float x = (float) Math.abs(to.getX() - from.getX());
        float y = (float) Math.abs(to.getY() - from.getY());
        float z = (float) Math.abs(to.getZ() - from.getZ());
        float dist = (float) Math.sqrt(x * x + y * y + z * z);
        if (dist < distance) {
            return to;
        }
        x = x / dist * distance;
        y = y / dist * distance;
        z = z / dist * distance;
        return new Location(from.getWorld(), from.getX() + x, from.getY() + y, from.getZ() + z);
    }
}
