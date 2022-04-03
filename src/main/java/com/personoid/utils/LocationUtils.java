package com.personoid.utils;

import com.personoid.handlers.NPCHandler;
import com.personoid.npc.NPC;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils {

    static List<BlockFace> relativeBlockFaces = new ArrayList<>();

    static {
        relativeBlockFaces.add(BlockFace.NORTH);
        relativeBlockFaces.add(BlockFace.SOUTH);
        relativeBlockFaces.add(BlockFace.EAST);
        relativeBlockFaces.add(BlockFace.WEST);
    }

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

    public static boolean canReach(Location to, Location from){
        return from.clone().add(0, 2, 0).distance(to) < 5;
    }

    public static Location getPathableLocation(Location to, Location from){
        Location validLocation = to.clone();
        Location justDownwards = null;
        for (int i = 1; i <= 10; i++){
            if (!to.clone().subtract(0,i,0).getBlock().getType().isSolid()){
                Location above = to.clone().subtract(0,i,0);
                above.add(0,1,0);
                if (!above.getBlock().getType().isSolid()){
                    justDownwards = above.clone().subtract(0,1,0);
                }
            }
        }
        if (justDownwards != null){
            return justDownwards;
        }
        else {
            for (BlockFace blockFace : relativeBlockFaces){
                Block adjacent = to.getBlock().getRelative(blockFace);
                Location location = validDownwardsSearch(adjacent.getLocation());
                if (location != null){
                    if (validLocation == to){
                        validLocation = location;
                    }
                    else {
                        if (location.distance(from) < validLocation.distance(from)){
                            validLocation = location;
                        }
                    }
                }

            }
        }
        return validLocation;
    }

    private static Location validDownwardsSearch(Location location){
        Location valid = null;

        for (int i = 0; i <= 10; i++){
            Location checkedUnder = location.clone().subtract(0,i,0);
            if (!checkedUnder.clone().add(0,1,0).getBlock().getType().isSolid()){
                if (!checkedUnder.getBlock().getType().isSolid()){
                    valid = checkedUnder;
                    break;
                }
            }
        }

        return valid;
    }

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

/*    public static Location getNear(Location from, Location to, float distance) {
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
    }*/

    public static boolean isSolid(Block block) {
        return block.getType().isSolid() || block.getType().toString().contains("LEAVES");
    }

    // ray trace blocks from location a to location b and return hit block

    public static Block rayTraceBlocks(Location from, Location to, int maxDistance, boolean stopOnLiquid){
        Block block = null;
        Vector vector = to.toVector().subtract(from.toVector()).normalize();

        vector = vector.normalize();

        for (int i = 1; i <= maxDistance; i++){
            Location loc = from.clone().add(vector.clone().multiply(i));
            Block b1 = loc.getBlock();
            loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc
                    , 5, new Particle.DustTransition(Color.BLUE, Color.AQUA, 1));
            if (LocationUtils.isSolid(b1)){
                block = b1;
                break;
            }
            else if (stopOnLiquid && b1.isLiquid()){
                block = b1;
                break;
            }
        }
        return block;
    }

    public static Block getBlockInFront(Location location, int distance) {
        BlockIterator blocks = new BlockIterator(location, 1, distance);
        Block lastNonSolidBlock = null;
        while (blocks.hasNext()) {
            Block block = blocks.next();
            if (block.getType().isSolid()) {
                return lastNonSolidBlock;
            } else {
                lastNonSolidBlock = block;
            }
        }
        return null;
    }
}
