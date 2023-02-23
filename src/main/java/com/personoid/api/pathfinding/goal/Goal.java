package com.personoid.api.pathfinding.goal;

import com.personoid.api.pathfinding.node.Node;

public abstract class Goal {
    public abstract double heuristic(Node node);
    public abstract boolean isFinalNode(Node node);
    public abstract boolean equals(Goal other);
}
