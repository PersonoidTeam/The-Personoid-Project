package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

public class MoveNodeEvaluator extends NodeEvaluator {
    @Override
    public Node apply(Node from, BlockPos to, NodeContext context) {
        //Bukkit.broadcastMessage("Walkable: " + context.isWalkable(to) + " from " + from.getPosition() + " to " + to);
        if (context.isWalkable(to)) {
            boolean diagonal = context.isDiagonal(from.getPos(), to);
            //Bukkit.broadcastMessage("Creating node: " + to + " diagonal: " + diagonal);
            return createNode(to, diagonal ? Cost.DIAGONAL : Cost.STRAIGHT);
        }
        return null;
    }
}
