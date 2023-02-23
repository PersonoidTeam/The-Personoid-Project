package com.personoid.api.pathfinding;

import com.personoid.api.pathfinding.goal.Goal;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.node.evaluator.NodeEvaluator;
import com.personoid.api.pathfinding.utils.BlockPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.World;

import java.util.List;

public class NodeContext {
    private final Long2ObjectOpenHashMap<Node> map;
    private final BlockPos startPos;
    private final Goal goal;
    private final World world;
    private final List<NodeEvaluator> evaluators;

    public NodeContext(BlockPos startPos, Goal goal, World world, List<NodeEvaluator> evaluators) {
        this.map = new Long2ObjectOpenHashMap<>(1024, 0.75F);
        this.startPos = startPos;
        this.goal = goal;
        this.world = world;
        this.evaluators = evaluators;
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public Goal getGoal() {
        return goal;
    }

    public World getWorld() {
        return world;
    }

    public List<NodeEvaluator> getEvaluators() {
        return evaluators;
    }

    public boolean isWalkable(BlockPos pos) {
        return world.getBlockAt(pos.getX(), pos.getY() - 1, pos.getZ()).getType().isSolid()
                && !world.getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getType().isSolid()
                && !world.getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getType().isSolid();
    }

    public boolean isDiagonal(BlockPos from, BlockPos to) {
        return from.getX() != to.getX() && from.getZ() != to.getZ();
    }

    public boolean isInBounds(BlockPos pos) {
        if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
            return false;
        }
        return pos.getY() >= world.getMinHeight() && pos.getY() < world.getMaxHeight();
    }

    public Node getNode(BlockPos pos) {
        long key = pos.asLong();
        Node node = map.get(key);
        if (node == null) {
            node = new Node(pos, this);
            map.put(key, node);
            node.updateHeuristic(goal);
        }
        return node;
    }
}
