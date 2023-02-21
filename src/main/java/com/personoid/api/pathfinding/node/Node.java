package com.personoid.api.pathfinding.node;

import com.personoid.api.pathfinding.*;
import com.personoid.api.pathfinding.utils.BlockPos;
import com.personoid.api.pathfinding.utils.Heuristics;
import com.personoid.api.pathfinding.utils.PathUtils;

public class Node implements Comparable<Node> {
    private final BlockPos position;
    private Node parent;

    private final double expense;
    private final double expenseLeft;
    private double finalExpense;

    private final NodeContext context;

    private final int dx;
    private final int dz;
    private final Node[] neighbors;

    private final double distanceFromStart;
    private final double distanceToEnd;
    private final double heuristicCost;
    private final boolean isWalkable;

    public Node(BlockPos position, Node parent, NodeContext context) {
        this.position = position;
        this.parent = parent;
        this.context = context;
        this.distanceFromStart = Heuristics.euclidean(position, context.getStartPos());
        this.distanceToEnd = Heuristics.euclidean(position, context.getEndPos());
        this.heuristicCost = distanceFromStart + distanceToEnd;
        this.isWalkable = PathUtils.isWalkable(position, context);

        this.expense = Double.POSITIVE_INFINITY;
        this.expenseLeft = Heuristics.euclidean(position, context.getEndPos());
        this.finalExpense = Double.POSITIVE_INFINITY;

        if (parent != null) {
            this.dx = position.getX() - parent.position.getX();
            this.dz = position.getZ() - parent.position.getZ();
            this.neighbors = JumpPointSearch.getNeighbors(this, context);
        } else {
            this.dx = 0;
            this.dz = 0;
            this.neighbors = new Node[0];
        }
    }

    public BlockPos getPosition() {
        return position;
    }

    public Node getParent() {
        return parent;
    }

    public double getExpense() {
        return expense;
    }

    public double getExpenseLeft() {
        return expenseLeft;
    }

    public double getFinalExpense() {
        return finalExpense;
    }

    public void setFinalExpense(double finalExpense) {
        this.finalExpense = finalExpense;
    }

    public Node[] getNeighbors() {
        return neighbors;
    }

    public NodeContext getContext() {
        return context;
    }

    public int getDx() {
        return dx;
    }

    public int getDz() {
        return dz;
    }

    public boolean isDiagonal() {
        return dx * dz != 0;
    }

    public double getDistanceFromStart() {
        return distanceFromStart;
    }

    public double getDistanceToEnd() {
        return distanceToEnd;
    }

    public double getHeuristicCost() {
        return heuristicCost;
    }

    public boolean isWalkable() {
        return isWalkable;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int getPathLength() {
        int length = 0;
        Node node = this;
        while (node.getParent() != null) {
            length++;
            node = node.getParent();
        }
        return length;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(finalExpense, other.finalExpense);
    }

    @Override
    public String toString() {
        return String.format("Node{x=%s, y=%s, z=%s}", position.getX(), position.getY(), position.getZ());
    }
}
