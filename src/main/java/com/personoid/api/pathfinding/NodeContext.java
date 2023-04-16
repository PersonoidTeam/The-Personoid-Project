package com.personoid.api.pathfinding;

import com.personoid.api.pathfinding.goal.Goal;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.node.evaluator.NodeEvaluator;
import com.personoid.api.pathfinding.utils.BlockPos;
import com.personoid.api.utils.types.BlockTags;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.World;

import java.util.List;

public class NodeContext {
    private final Long2ObjectOpenHashMap<Node> map;
    private final BlockPos startPos;
    private final Goal goal;
    private final World world;
    private final List<NodeEvaluator> evaluators;
    private final int chunkRadius;

    public NodeContext(BlockPos startPos, Goal goal, World world, List<NodeEvaluator> evaluators, int chunkRadius) {
        this.map = new Long2ObjectOpenHashMap<>(1024, 0.75F);
        this.startPos = startPos;
        this.goal = goal;
        this.world = world;
        this.evaluators = evaluators;
        this.chunkRadius = chunkRadius;
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

    public int getChunkRadius() {
        return chunkRadius;
    }

    public boolean isWalkable(BlockPos pos) {
        return BlockTags.SOLID.is(pos.below().toBlock(world)) &&
                !BlockTags.SOLID.is(pos.toBlock(world)) &&
                !BlockTags.SOLID.is(pos.above().toBlock(world));
    }

    public boolean isSolid(BlockPos pos) {
        return BlockTags.SOLID.is(pos.toBlock(world));
    }

    public boolean isDiagonal(BlockPos from, BlockPos to) {
        return from.getX() != to.getX() && from.getZ() != to.getZ();
    }

    public boolean isInBounds(BlockPos pos) {
        if (!isLoaded(pos)) return false;
        return pos.getY() >= world.getMinHeight() && pos.getY() < world.getMaxHeight();
    }

    public boolean isLoaded(BlockPos pos) {
        return world.getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getChunk().isLoaded();
    }

    public boolean isWallInWay(BlockPos from, BlockPos to, int dX, int dZ) {
        boolean diagonal = isDiagonal(from, to);
        if (diagonal) {
            if (!isWalkable(to.add(dX, 0, 0)) || !isWalkable(to.add(0, 0, dZ))) {
                return true;
            }
            return !isWalkable(from.add(dX, 0, 0)) || !isWalkable(from.add(0, 0, dZ));
        } else {
            return isSolid(to.add(0, 2, 0));
        }
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
