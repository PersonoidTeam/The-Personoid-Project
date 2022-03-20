package com.notnotdoddy.personoid.utils.npc;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class NPCUtils {
    public static boolean solidAt(Location loc) {
        Block block = loc.getBlock();
        BoundingBox box = block.getBoundingBox();
        Vector position = loc.toVector();

        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        double minX = box.getMinX();
        double minY = box.getMinY();
        double minZ = box.getMinZ();

        double maxX = box.getMaxX();
        double maxY = box.getMaxY();
        double maxZ = box.getMaxZ();

        return x > minX && x < maxX && y > minY && y < maxY && z > minZ && z < maxZ;
    }
}