package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ParkourNodeEvaluator extends NodeEvaluator {
    @Override
    public Node apply(Node from, BlockPos to, NodeContext context) {
        int maxParkourDistance = 4;
        int maxParkourHeight = 1;

        BlockPos fromPos = from.getPos();

        // doesn't work because it doesn't check for closest
        for (int dx = -maxParkourDistance; dx <= maxParkourDistance; dx++) {
            for (int dz = -maxParkourDistance; dz <= maxParkourDistance; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                boolean diagonal = dx != 0 && dz != 0;
                BlockPos parkour = fromPos.add(dx, 0, dz);
                int parkourHeight = 0;
                while (parkourHeight < maxParkourHeight && parkour.getY() <= to.getY()) {
                    if (context.isWalkable(parkour)) {
                        double directionCost = (diagonal ? Cost.DIAGONAL * Cost.PARKOUR : Cost.STRAIGHT * Cost.PARKOUR);
                        return createNode(parkour, maxParkourDistance * parkourHeight * directionCost);
                    }
                    parkour = parkour.above();
                    parkourHeight++;
                }
            }
        }

        return null;
    }

    @Override
    public List<NodeEvaluator> getDependencies() {
        List<NodeEvaluator> dependencies = new ArrayList<>();
        //dependencies.add(new WalkNodeEvaluator());
        return dependencies;
    }
}
