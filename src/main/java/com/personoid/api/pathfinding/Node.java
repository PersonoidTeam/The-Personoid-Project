package com.personoid.api.pathfinding;

public class Node implements Cloneable {
    protected BlockPos location;
    protected int x, y, z;

    public Node(BlockPos blockPos) {
        this.location = blockPos;
        x = blockPos.getX();
        y = blockPos.getY();
        z = blockPos.getZ();
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
        return location;
    }

    public boolean matchLocation(Node node) {
        return node.x == x && node.y == y && node.z == z;
    }

    @Override
    public Node clone() {
        try {
            Node clone = (Node) super.clone();
            clone.location = new BlockPos(x, y, z);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
