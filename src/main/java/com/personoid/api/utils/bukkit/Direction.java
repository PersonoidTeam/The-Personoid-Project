package com.personoid.api.utils.bukkit;

public enum Direction {
    NORTH(0, 1, 0, -1, 0, 0),
    SOUTH(0, 1, 0, 1, 0, 0),
    WEST(0, 1, 0, 0, 0, -1),
    EAST(0, 1, 0, 0, 0, 1),
    UP(0, 0, 1, 0, -1, 0),
    DOWN(0, 0, -1, 0, 1, 0);

    private final int x;
    private final int y;
    private final int z;
    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;

    Direction(int x, int y, int z, int offsetX, int offsetY, int offsetZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
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

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getOffsetZ() {
        return offsetZ;
    }
}
