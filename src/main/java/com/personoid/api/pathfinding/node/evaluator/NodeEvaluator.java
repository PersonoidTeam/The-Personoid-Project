package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeEvaluator {
    protected NodeContext context;

    public abstract Node apply(Node from, BlockPos to, NodeContext context);

    protected Node createNode(BlockPos pos, double cost) {
        Node node = context.getNode(pos);
        node.setCost(cost);
        return node;
    }

    public void context(NodeContext context) {
        this.context = context;
    }

    public List<NodeEvaluator> getDependencies() {
        return new ArrayList<>();
    }
}
