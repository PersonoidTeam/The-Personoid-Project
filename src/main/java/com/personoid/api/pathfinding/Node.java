package com.personoid.api.pathfinding;

public class Node {
    protected final int x;
    protected final int y;
    protected final int z;

    public Node(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Node(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

    public boolean equals(Node node) {
        return equals(node.x, node.y, node.z);
    }

    public boolean equals(BlockPos pos) {
        return equals(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean equals(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    @Override
    public String toString() {
        return String.format("Node{x=%s, y=%s, z=%s}", x, y, z);
    }
}
