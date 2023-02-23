package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

public class ParkourNodeEvaluator extends NodeEvaluator {
    @Override
    public Node apply(Node from, BlockPos to, NodeContext context) {
        int maxParkourDistance = 4;
        int maxParkourHeight = 1;

        int parkourDistance = 0;
        int parkourHeight = 0;
        BlockPos parkour = to;
        while (parkourDistance < maxParkourDistance && parkourHeight < maxParkourHeight) {
            if (!context.isWalkable(parkour.above()) || !context.isWalkable(parkour.above(2))) {
                break;
            }
            if (!context.isWalkable(parkour)) {
                return createNode(parkour, parkourDistance);
            }
            parkourDistance++;
            parkourHeight++;
            parkour = parkour.above();
        }
        return null;
    }
}
