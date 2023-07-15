package com.personoid.api.pathfinding.calc.utils;

import com.personoid.api.utils.math.MathUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockPos implements Cloneable {
    private int x;
    private int y;
    private int z;

    public static BlockPos fromLocation(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(double x, double y, double z) {
        this.x = MathUtils.floor(x);
        this.y = MathUtils.floor(y);
        this.z = MathUtils.floor(z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public Block toBlock(World world) {
        return world.getBlockAt(x, y, z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPos add(int x, int y, int z) {
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    public BlockPos add(BlockPos pos) {
        return new BlockPos(x + pos.x, y + pos.y, z + pos.z);
    }

    public BlockPos subtract(BlockPos pos) {
        return new BlockPos(x - pos.x, y - pos.y, z - pos.z);
    }

    public BlockPos multiply(BlockPos pos) {
        return new BlockPos(x * pos.x, y * pos.y, z * pos.z);
    }

    public BlockPos divide(BlockPos pos) {
        return new BlockPos(x / pos.x, y / pos.y, z / pos.z);
    }

    public BlockPos above() {
        return new BlockPos(x, y + 1, z);
    }

    public BlockPos above(int mod) {
        return new BlockPos(x, y + mod, z);
    }

    public BlockPos below() {
        return new BlockPos(x, y - 1, z);
    }

    public BlockPos below(int mod) {
        return new BlockPos(x, y - mod, z);
    }

    public BlockPos north() {
        return new BlockPos(x, y, z - 1);
    }

    public BlockPos north(int mod) {
        return new BlockPos(x, y, z - mod);
    }

    public BlockPos south() {
        return new BlockPos(x, y, z + 1);
    }

    public BlockPos south(int mod) {
        return new BlockPos(x, y, z + mod);
    }

    public BlockPos west() {
        return new BlockPos(x - 1, y, z);
    }

    public BlockPos west(int mod) {
        return new BlockPos(x - mod, y, z);
    }

    public BlockPos east() {
        return new BlockPos(x + 1, y, z);
    }

    public BlockPos east(int mod) {
        return new BlockPos(x + mod, y, z);
    }

    public double distance(BlockPos pos) {
        return Math.sqrt(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y, 2) + Math.pow(z - pos.z, 2));
    }

    public long asLong() {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) y & 0xFFF) << 26 | ((long) z & 0x3FFFFFF);
    }

    public boolean equals(BlockPos pos) {
        return x == pos.x && y == pos.y && z == pos.z;
    }

    public static BlockPos fromLong(long pos) {
        int x = (int) (pos << 64 - 38 - 26 >> 64 - 26);
        int y = (int) (pos << 64 - 12 >> 64 - 12);
        int z = (int) (pos << 38 >> 38);
        return new BlockPos(x, y, z);
    }

    @Override
    public BlockPos clone() {
        try {
            BlockPos clone = (BlockPos) super.clone();
            clone.x = this.x;
            clone.y = this.y;
            clone.z = this.z;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return "BlockPos{" + "x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
