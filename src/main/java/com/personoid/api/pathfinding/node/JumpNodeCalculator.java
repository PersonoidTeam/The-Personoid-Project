package com.personoid.api.pathfinding.node;

import com.personoid.api.pathfinding.NodeContext;

public class JumpNodeCalculator extends NodeCalculator {
    @Override
    public Node[] getNeighbors(Node node, NodeContext context) {
        return new Node[0];
    }
}
