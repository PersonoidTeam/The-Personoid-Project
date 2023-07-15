package com.personoid.api.pathfinding.calc.goal;

import com.personoid.api.pathfinding.calc.node.Node;

public abstract class Goal {
    public abstract double heuristic(Node node);
    public abstract boolean isFinalNode(Node node);
    public abstract boolean equals(Goal other);
}
