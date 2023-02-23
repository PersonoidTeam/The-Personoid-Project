package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class JumpNodeEvaluator extends NodeEvaluator {
    @Override
    public Node apply(Node from, BlockPos to, NodeContext context) {
        if (context.isWalkable(to.above()) && !context.isWalkable(to.above(2)) && !context.isWalkable(to.above(3))) {
            return createNode(to.above(), Cost.JUMP);
        }
        return null;
    }

    @Override
    public List<NodeEvaluator> getDependencies() {
        List<NodeEvaluator> dependencies = new ArrayList<>();
        dependencies.add(new WalkNodeEvaluator());
        return dependencies;
    }
}
