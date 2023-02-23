package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.Cost;
import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;
import com.personoid.api.utils.types.BlockTags;
import org.bukkit.block.Block;

public class WalkNodeEvaluator extends NodeEvaluator {
    @Override
    public Node apply(Node from, BlockPos to, NodeContext context) {
        if (context.isWalkable(to)) {
            boolean diagonal = context.isDiagonal(from.getPos(), to);
            if (diagonal) {
                BlockPos higherFrom = from.getPos().above();
                BlockPos higherTo = to.above().above();
                int xDelta = higherTo.getX() - higherFrom.getX();
                int zDelta = higherTo.getZ() - higherFrom.getZ();
                Block block = higherFrom.add(xDelta, 0, 0).toBlock(context.getWorld());
                if (BlockTags.SOLID.is(block)) return null;
                block = higherFrom.add(0, 0, zDelta).toBlock(context.getWorld());
                if (BlockTags.SOLID.is(block)) return null;
            }
            return createNode(to, diagonal ? Cost.DIAGONAL : Cost.STRAIGHT);
        }
        return null;
    }
}
