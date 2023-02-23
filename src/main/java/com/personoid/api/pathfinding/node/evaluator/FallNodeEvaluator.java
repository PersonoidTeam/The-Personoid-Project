package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

public class FallNodeEvaluator extends NodeEvaluator {
    @Override
    public Node apply(Node from, BlockPos to, NodeContext context) {
        int maxFallDistance = 3;
        int fallDistance = 0;
        BlockPos drop = to;
        while (fallDistance < maxFallDistance) {
            if (context.isWalkable(drop)) {
                return createNode(drop, Cost.FALL * fallDistance);
            }
            fallDistance++;
            drop = drop.below();
        }
        return null;
    }
}
