package com.personoid.api.pathfinding;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class PathingContext {
    private final Node startNode;
    private Node endNode;
    private final Set<Node> closedSet = new HashSet<>();
    private final OpenSetHeap openSet = new OpenSetHeap(1024);
    private final Pathfinder.Options options;

    public PathingContext(Location startLocation, Location endLocation, Pathfinder.Options options) {
        this.startNode = new Node(startLocation, 0, null, this);
        this.endNode = new Node(endLocation, 0, null, this);
        this.options = options;
        openSet.add(startNode);
    }

    public Location getStartLocation() {
        return startNode.location;
    }

    public Location getEndLocation() {
        return endNode.location;
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public Set<Node> getClosedSet() {
        return closedSet;
    }

    public OpenSetHeap getOpenSet() {
        return openSet;
    }

    public Pathfinder.Options getOptions() {
        return options;
    }

    public Node getNode(Location location) {
        for (Node node : closedSet) {
            if (node.getX() == location.getBlockX() && node.getY() == location.getBlockY() && node.getZ() == location.getBlockZ()) {
                return node;
            }
        }
        return new Node(location, 0, null, this);
    }
}
