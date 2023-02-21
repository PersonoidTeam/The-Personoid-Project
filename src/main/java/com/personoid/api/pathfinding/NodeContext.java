package com.personoid.api.pathfinding;

import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;
import org.bukkit.World;

public class NodeContext {
    private final BlockPos startPos;
    private final BlockPos endPos;
    private Node endNode;
    private final World world;

    public NodeContext(BlockPos startPos, BlockPos endPos, World world) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.world = world;
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public BlockPos getEndPos() {
        return endPos;
    }

    public World getWorld() {
        return world;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }
}
