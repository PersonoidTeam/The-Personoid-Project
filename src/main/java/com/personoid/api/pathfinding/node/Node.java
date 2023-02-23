package com.personoid.api.pathfinding.node;

import com.personoid.api.pathfinding.JumpPointSearch;
import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.goal.Goal;
import com.personoid.api.pathfinding.utils.BlockPos;
import com.personoid.api.pathfinding.utils.Heuristics;

public class Node implements Comparable<Node> {
    private static final double MIN_COST_IMPROVEMENT = 0.01;

    private final BlockPos position;
    private final NodeContext context;
    private Node parent;

    private double gCost;
    private double hCost;
    private double fCost;
    private double cost;

    private int heapIndex = -1;

    public Node(BlockPos position, NodeContext context) {
        this.position = position;
        this.context = context;
    }

    public BlockPos getPos() {
        return position;
    }

    public Node getParent() {
        return parent;
    }

    public double getGCost() {
        return gCost;
    }

    public double getHCost() {
        return hCost;
    }

    public double getFCost() {
        return fCost;
    }

    public void updateFinalCost() {
        this.fCost = gCost + hCost;
    }

    public double getPartialCost(float coefficient) {
        return hCost + gCost / coefficient;
    }

    public void updateHeuristic(Goal goal) {
        this.hCost = goal.heuristic(this);
        updateFinalCost();
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }

    public int squaredDistanceTo(Node node) {
        return Heuristics.squaredEuclidean(position, node.getPos());
    }

    public Node[] getNeighbors() {
        return new JumpPointSearch().getNeighbors(this, context);
    }

    public void setParent(Node parent, double cost) {
        this.parent = parent;
        gCost = parent.getGCost() + cost;
        updateFinalCost();
    }

    public boolean updateParent(Node parent, double cost) {
        double distance = parent.getGCost() + cost;
        double improvement = gCost - distance;
        if (improvement > MIN_COST_IMPROVEMENT) {
            setParent(parent, cost);
            return true;
        }
        return false;
    }

    public int getHeapIndex() {
        return heapIndex;
    }

    public void setHeapIndex(int heapIndex) {
        this.heapIndex = heapIndex;
    }

    public boolean isOpen() {
        return heapIndex != -1;
    }

    public double getCostTo(Node node) {
        return Heuristics.euclidean(position, node.position);
    }

    public NodeContext getContext() {
        return context;
    }

    public boolean equals(Node other) {
        return position.equals(other.position);
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(fCost, other.fCost);
    }

    @Override
    public String toString() {
        return String.format("Node{x=%s, y=%s, z=%s}", position.getX(), position.getY(), position.getZ());
    }
}
