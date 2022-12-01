package com.personoid.api.utils.bukkit;

import com.personoid.api.utils.math.MathUtils;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockPos {
    private final int x;
    private final int y;
    private final int z;

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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

/*    public BlockPos above() {
        return this.relative(Direction.UP);
    }

    public BlockPos above(int mod) {
        return this.relative(Direction.UP, mod);
    }

    public BlockPos below() {
        return this.relative(Direction.DOWN);
    }

    public BlockPos below(int mod) {
        return this.relative(Direction.DOWN, mod);
    }

    public BlockPos north() {
        return this.relative(Direction.NORTH);
    }

    public BlockPos north(int mod) {
        return this.relative(Direction.NORTH, mod);
    }

    public BlockPos south() {
        return this.relative(Direction.SOUTH);
    }

    public BlockPos south(int mod) {
        return this.relative(Direction.SOUTH, mod);
    }

    public BlockPos west() {
        return this.relative(Direction.WEST);
    }

    public BlockPos west(int mod) {
        return this.relative(Direction.WEST, mod);
    }

    public BlockPos east() {
        return this.relative(Direction.EAST);
    }

    public BlockPos east(int mod) {
        return this.relative(Direction.EAST, mod);
    }*/
}
