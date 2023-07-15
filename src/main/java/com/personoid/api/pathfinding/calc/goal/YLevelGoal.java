package com.personoid.api.pathfinding.calc.goal;

import com.personoid.api.pathfinding.calc.Cost;
import com.personoid.api.pathfinding.calc.node.Node;

public class YLevelGoal extends Goal {
    private final int y;

    public YLevelGoal(int y) {
        this.y = y;
    }

    @Override
    public double heuristic(Node node) {
        return cost(node.getPos().getY(), y);
    }

    @Override
    public boolean isFinalNode(Node node) {
        return node.getPos().getY() == y;
    }

    @Override
    public boolean equals(Goal other) {
        if (other instanceof YLevelGoal) {
            int otherY = ((YLevelGoal) other).getY();
            return otherY == y;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("YGoal{y=%s}", y);
    }

    public int getY() {
        return y;
    }

    public static double cost(int y, int goalY) {
        if (y < goalY) {
            int dis = goalY - y;
            return dis * Cost.JUMP;
        }
        if (y > goalY) {
            int dis = y - goalY;
            return Cost.fall(2) / 2 * dis;
        }
        return 0;
    }
}
