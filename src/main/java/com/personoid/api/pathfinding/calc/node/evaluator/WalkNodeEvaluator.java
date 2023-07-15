package com.personoid.api.pathfinding.calc.node.evaluator;

import com.personoid.api.pathfinding.calc.Cost;
import com.personoid.api.pathfinding.calc.node.Node;
import com.personoid.api.pathfinding.calc.utils.BlockPos;

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
