package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

public class WalkNodeEvaluator extends NodeEvaluator {
    @Override
    public Node apply(Node from, BlockPos to, int dX, int dY, int dZ) {
        if (context.isWalkable(to) && !context.isWallInWay(from.getPos(), to, dX, dZ)) {
            boolean diagonal = context.isDiagonal(from.getPos(), to);
            return createNode(to, diagonal ? Cost.DIAGONAL : Cost.STRAIGHT);
        }
        return null;
    }
}
