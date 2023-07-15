package com.personoid.api.pathfinding.calc.goal;

import com.personoid.api.pathfinding.calc.Cost;
import com.personoid.api.pathfinding.calc.node.Node;

public class XZGoal extends Goal {
    private final int x;
    private final int z;

    public XZGoal(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public double heuristic(Node node) {
        int dx = node.getPos().getX() - x;
        int dz = node.getPos().getZ() - z;
        return cost(dx, dz);
    }

    @Override
    public boolean isFinalNode(Node node) {
        return node.getPos().getX() == x && node.getPos().getZ() == z;
    }

    @Override
    public boolean equals(Goal other) {
        if (other instanceof XZGoal) {
            XZGoal goal = (XZGoal) other;
            return goal.x == x && goal.z == z;
        }
        return false;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public static double cost(int dx, int dz) {
        int x = Math.abs(dx);
        int z = Math.abs(dz);
        int straight;
        int diagonal;

        if (x < z) {
            straight = z - x;
            diagonal = x;
        } else {
            straight = x - z;
            diagonal = z;
        }
        return Cost.STRAIGHT * straight + Cost.DIAGONAL * diagonal;
    }

    @Override
    public String toString() {
        return String.format("XZGoal{x=%s, z=%s}", x, z);
    }
}
