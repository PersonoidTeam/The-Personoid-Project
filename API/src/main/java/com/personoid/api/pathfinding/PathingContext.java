package com.personoid.api.pathfinding;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class PathingContext {
    private final Node startNode;
    private Node endNode;
    private final List<Node> checkedNodes = new ArrayList<>();
    private final List<Node> uncheckedNodes = new ArrayList<>();
    private final Pathfinder.Options options;

    public PathingContext(Location startLocation, Location endLocation, Pathfinder.Options options) {
        this.startNode = new Node(startLocation, 0, null, this);
        this.endNode = new Node(endLocation, 0, null, this);
        this.options = options;
        uncheckedNodes.add(startNode);
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

    public List<Node> getCheckedNodes() {
        return checkedNodes;
    }

    public List<Node> getUncheckedNodes() {
        return uncheckedNodes;
    }

    public Pathfinder.Options getOptions() {
        return options;
    }

    public Node getNode(Location location) {
        for (Node node : checkedNodes) {
            if (node.getX() == location.getBlockX() && node.getY() == location.getBlockY() && node.getZ() == location.getBlockZ()) {
                return node;
            }
        }
        return new Node(location, 0, null, this);
    }
}
