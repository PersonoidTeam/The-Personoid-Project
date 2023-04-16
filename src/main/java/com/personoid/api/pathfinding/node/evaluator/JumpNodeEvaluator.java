package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

public class JumpNodeEvaluator extends NodeEvaluator {
    public JumpNodeEvaluator() {
        super(1, 1, 1);
    }

    @Override
    public Node apply(Node from, BlockPos to, int dX, int dY, int dZ) {
        if (dY <= 0) return null;
        if (context.isWallInWay(from.getPos(), to, dX, dZ)) return null;
        boolean toPassable = context.isWalkable(to);
        boolean fromPassable = !context.isSolid(from.getPos().above(3));
        if (toPassable && fromPassable) {
            return createNode(to, Cost.JUMP);
        }
        return null;
    }
}
