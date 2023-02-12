package com.personoid.api.pathfinding;

import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class PathingContext {
    private World world;
    private final PathingNode startNode;
    private PathingNode endNode;
    private final Set<PathingNode> closedSet = new HashSet<>();
    private final OpenSetHeap openSet = new OpenSetHeap(1024);
    private final PathingConfig config;

    public PathingContext(BlockPos startLocation, BlockPos endLocation, World world, PathingConfig config) {
        this.startNode = new PathingNode(startLocation, 0, null, this);
        this.endNode = new PathingNode(endLocation, 0, null, this);
        this.world = world;
        this.config = config;
        openSet.add(startNode);
    }

    public BlockPos getStartLocation() {
        return startNode.location;
    }

    public BlockPos getEndLocation() {
        return endNode.location;
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

    public OpenSetHeap getOpenSet() {
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
