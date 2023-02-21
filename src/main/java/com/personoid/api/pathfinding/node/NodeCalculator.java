package com.personoid.api.pathfinding.node;

import com.personoid.api.pathfinding.NodeContext;

public abstract class NodeCalculator {
    public abstract Node[] getNeighbors(Node node, NodeContext context);
}
