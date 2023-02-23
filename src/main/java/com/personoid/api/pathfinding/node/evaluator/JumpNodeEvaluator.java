package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

public class JumpNodeEvaluator extends NodeEvaluator {
    @Override
    public Node apply(Node from, BlockPos to, NodeContext context) {
        if (context.isWalkable(to.above())) {
            return createNode(to.above(), Cost.JUMP);
        }
        return null;
    }
}
