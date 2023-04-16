package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

public class FallNodeEvaluator extends NodeEvaluator {
    public FallNodeEvaluator() {
        super(1, 0, 1);
    }

    @Override
    public Node apply(Node from, BlockPos to, int dX, int dY, int dZ) {
        if (dY > 0) return null;
        if (context.isWallInWay(from.getPos(), to, dX, dZ)) return null;
        if (context.isWalkable(to) || context.isWalkable(to.above()) || context.isWalkable(to.above(2))) {
            return null;
        }
        int maxFallDistance = 3;
        int fallDistance = 0;
        BlockPos drop = to;
        while (fallDistance < maxFallDistance) {
            if (context.isWalkable(drop)) {
                return createNode(drop, Cost.fall(fallDistance));
            }
            fallDistance++;
            drop = drop.below();
        }
        return null;
    }
}
