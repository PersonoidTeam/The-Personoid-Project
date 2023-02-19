package com.personoid.api.pathfinding;

import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class PathingContext {
    private final World world;
    private final PathingNode startNode;
    private PathingNode endNode;
    private final Set<PathingNode> closedSet = new HashSet<>();
    private final HeapOpenSet openSet = new HeapOpenSet(1024);
    private final PathingConfig config;

    public PathingContext(BlockPos startLocation, BlockPos endLocation, World world, PathingConfig config) {
        this.startNode = new PathingNode(startLocation, 0, null, this);
        this.endNode = new PathingNode(endLocation, 0, null, this);
        this.world = world;
        this.config = config;
        openSet.add(startNode);
    }

    public BlockPos getStartLocation() {
        return startNode.getBlockPos();
    }

    public BlockPos getEndLocation() {
        return endNode.getBlockPos();
    }

    public PathingNode getStartNode() {
        return startNode;
    }

    public PathingNode getEndNode() {
        return endNode;
    }

    public void setEndNode(PathingNode endNode) {
        this.endNode = endNode;
    }

    public Set<PathingNode> getClosedSet() {
        return closedSet;
    }

    public HeapOpenSet getOpenSet() {
        return openSet;
    }

    public PathingConfig getConfig() {
        return config;
    }

    public World getWorld() {
        return world;
    }

    public PathingNode getNode(BlockPos location) {
        for (PathingNode node : closedSet) {
            if (node.getX() == location.getX() && node.getY() == location.getY() && node.getZ() == location.getZ()) {
                return node;
            }
        }
        return new PathingNode(location, 0, null, this);
    }
}
