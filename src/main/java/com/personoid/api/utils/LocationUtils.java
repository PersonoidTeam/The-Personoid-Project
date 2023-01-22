package com.personoid.api.utils;

import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.math.Range;
import com.personoid.api.utils.types.BlockTags;
import org.bukkit.Bukkit;
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
    private static final List<BlockFace> relativeBlockFaces = new ArrayList<>();

    static {
        relativeBlockFaces.add(BlockFace.NORTH);
        relativeBlockFaces.add(BlockFace.SOUTH);
        relativeBlockFaces.add(BlockFace.EAST);
        relativeBlockFaces.add(BlockFace.WEST);
    }

    private static Vector deltaDistance(Location loc1, Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) return new Vector(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        double deltaX = Math.abs(loc1.getX() - loc2.getX());
        double deltaY = Math.abs(loc1.getY() - loc2.getY());
        double deltaZ = Math.abs(loc1.getZ() - loc2.getZ());
        return new Vector(deltaX, deltaY, deltaZ);
    }

    public static double euclideanDistance(Location loc1, Location loc2) {
        Vector delta = deltaDistance(loc1, loc2);
        double distance2d = Math.sqrt(delta.getX() * delta.getX() + delta.getZ() * delta.getZ());
        return Math.sqrt(distance2d * distance2d + delta.getY() * delta.getY());
    }

    public static double manhattanDistance(Location loc1, Location loc2) {
        Vector delta = deltaDistance(loc1, loc2);
        return Math.sqrt(delta.getX() + delta.getY() + delta.getZ());
    }

    public static boolean canStandAt(Location location) {
        return !isSolid(location) && !isSolid(location.clone().add(0, 1, 0)) &&
                isSolid(location.clone().add(0, -1, 0));
    }

    public static boolean isSolid(Location location) {
        return location.getBlock().getType().isSolid() && !location.getBlock().getType().name().contains("TRAPDOOR");
    }

    public static Vector atBottomCenterOf(Vector vector) {
        return new Vector(vector.getX() + 0.5, vector.getY(), vector.getZ() + 0.5);
    }

    public static boolean closerThan(Vector vector1, Vector vector2, double distance) {
        return distanceToSqr(vector1, vector2) < distance * distance;
    }

    public static double distanceToSqr(Vector vector1, Vector vector2) {
        double x = vector1.getX() - vector2.getX();
        double y = vector1.getY() - vector2.getY();
        double z = vector1.getZ() - vector2.getZ();
        return x * x + y * y + z * z;
    }

    public static Block getBlockInDir(Location location, BlockFace direction) {
        while (true) {
            if (BlockTags.SOLID.is(location)) return location.getBlock();
            location = location.getBlock().getRelative(direction).getLocation();
        }
    }

    public static Location validRandom(Location from, Range range, float directionBias) {
        Location loc = from.clone();
        int x = MathUtils.random(range.getMin(), range.getMax());
        int z = MathUtils.random(range.getMin(), range.getMax());
        x *= Math.random() < directionBias ? 1 : -1;
        z *= Math.random() < directionBias ? 1 : -1;
        loc.add(x, 0, z).setY(320);
        return getBlockInDir(loc, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
    }

    public static Location fromString(String string) {
        String[] split = string.split(",");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
    }

    public static String toString(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getZ();
    }

    public static Block rayTraceBlocks(Location from, Location to, int maxDistance, boolean stopOnLiquid){
        Block block = null;
        Vector vector = to.toVector().subtract(from.toVector()).normalize();
        vector = vector.normalize();
        for (int i = 1; i <= maxDistance; i++){
            Location loc = from.clone().add(vector.clone().multiply(i));
            Block b1 = loc.getBlock();
            loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 5, new Particle.DustOptions(Color.BLUE, 1));
            if (BlockTags.SOLID.is(b1)){
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

    public static boolean canReach(Location to, Location from){
        return from.clone().add(0, 2, 0).distance(to) < 5;
    }

    public static Block getAirInDir(Location location, BlockFace direction) {
        while (true) {
            location = location.getBlock().getRelative(direction).getLocation();
            if (location.getBlock().getType().isAir()) {
                return location.getBlock();
            }
        }
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

    public static boolean solidBoundsAt(Location loc) {
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

    public static Vector toLocalSpace(Vector vector, double yaw) {
        double angle = Math.toRadians(yaw);
        double x = vector.getX() * Math.cos(angle) + vector.getZ() * Math.sin(angle);
        double z = vector.getZ() * Math.cos(angle) - vector.getX() * Math.sin(angle);
        return new Vector(x, vector.getY(), z);
    }
}
