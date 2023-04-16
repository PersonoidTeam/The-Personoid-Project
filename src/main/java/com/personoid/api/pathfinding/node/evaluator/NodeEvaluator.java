package com.personoid.api.pathfinding.node.evaluator;

import com.personoid.api.pathfinding.NodeContext;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeEvaluator {
    private final int xRange;
    private final int yRange;
    private final int zRange;

    protected NodeContext context;

    public NodeEvaluator() {
        this(1, 0, 1);
    }

    public NodeEvaluator(int yRange) {
        this(0, yRange, 0);
    }

    public NodeEvaluator(int xRange, int zRange) {
        this(xRange, 0, zRange);
    }

    public NodeEvaluator(int xRange, int yRange, int zRange) {
        this.xRange = xRange;
        this.yRange = yRange;
        this.zRange = zRange;
    }

    public abstract Node apply(Node from, BlockPos to, int dX, int dY, int dZ);

    protected Node createNode(BlockPos pos, double cost) {
        Node node = context.getNode(pos);
        node.setCost(cost);
        return node;
    }

    public void context(NodeContext context) {
        this.context = context;
    }

    public List<NodeEvaluator> getDependencies() {
        return new ArrayList<>();
    }

    public int getXRange() {
        return xRange;
    }

    public int getYRange() {
        return yRange;
    }

    public int getZRange() {
        return zRange;
    }
}
