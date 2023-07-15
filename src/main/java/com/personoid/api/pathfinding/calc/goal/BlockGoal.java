package com.personoid.api.pathfinding.calc.goal;

import com.personoid.api.pathfinding.calc.node.Node;
import com.personoid.api.pathfinding.calc.utils.BlockPos;

public class BlockGoal extends Goal {
    private final BlockPos pos;

    public BlockGoal(int x, int y, int z) {
        this(new BlockPos(x, y, z));
    }

    public BlockGoal(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public double heuristic(Node node) {
        int dx = node.getPos().getX() - pos.getX();
        int dy = node.getPos().getY() - pos.getY();
        int dz = node.getPos().getZ() - pos.getZ();
        return cost(dx, dy, dz);
    }

    @Override
    public boolean isFinalNode(Node node) {
        int x = node.getPos().getX();
        int y = node.getPos().getY();
        int z = node.getPos().getZ();
        return x == pos.getX() && y == pos.getY() && z == pos.getZ();
    }

    @Override
    public boolean equals(Goal other) {
        if (other instanceof BlockGoal) {
            BlockPos otherPos = ((BlockGoal) other).getPos();
            return otherPos.equals(pos);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("BlockGoal{pos=%s}", pos);
    }

    public BlockPos getPos() {
        return pos;
    }

    public static double cost(int dx, int dy, int dz) {
        double cost = XZGoal.cost(dx, dz);
        cost += YLevelGoal.cost(dy, 0);
        return cost;
    }
}
