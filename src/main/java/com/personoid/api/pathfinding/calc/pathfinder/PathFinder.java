package com.personoid.api.pathfinding.calc.pathfinder;

import com.personoid.api.pathfinding.calc.NodeContext;
import com.personoid.api.pathfinding.calc.Path;
import com.personoid.api.pathfinding.calc.goal.Goal;
import com.personoid.api.pathfinding.calc.node.Node;
import com.personoid.api.pathfinding.calc.node.evaluator.NodeEvaluator;
import com.personoid.api.pathfinding.calc.utils.BlockPos;
import com.personoid.api.pathfinding.calc.utils.HeapOpenSet;
import com.personoid.api.utils.debug.Profiler;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PathFinder {
    private static final float[] PS_COEFFICIENTS = { 1.5F, 2, 2.5F, 3, 4, 5, 10 };
    private static final int PS_MIN_DISTANCE = 25;

    protected final List<NodeEvaluator> evaluators = new ArrayList<>();
    private final HeapOpenSet openSet;
    private final Set<Node> closedSet;
    private Node[] partialSolutions;
    private Node startNode;
    private Node lastConsideration;
    private NodeContext context;
    private boolean foundApproximation;

    private boolean stop;

    public PathFinder() {
        this.openSet = new HeapOpenSet();
        this.closedSet = new HashSet<>();
        registerEvaluators();
    }

    public Path findPath(BlockPos start, Goal goal, World world) {
        int chunkRadius = getChunkingRadius();
        openSet.clear();
        closedSet.clear();
        this.partialSolutions = new Node[PS_COEFFICIENTS.length];

        context = new NodeContext(start, goal, world, evaluators, chunkRadius);
        startNode = context.getNode(start);
        openSet.add(startNode);

        long startTime = System.currentTimeMillis();
        foundApproximation = false;

        while (!openSet.isEmpty()) {
            if (stop) return null;
            Node current = openSet.poll();
            lastConsideration = current;
            closedSet.add(current);
            updatePartialSolutions(current);
            if (goal.isFinalNode(current)) {
                long time = (System.currentTimeMillis() - startTime);
                String timeStr = ((time > 50) ? ChatColor.RED : ChatColor.GREEN).toString() + time + "ms";
                Profiler.PATHFINDING.push("Found path in " + timeStr);
                return reconstructPath(current);
            }
            addAdjacentNodes(current);
            int distance = startNode.squaredDistanceTo(current);
            if (chunkRadius > 0 && distance > (chunkRadius * chunkRadius)) {
                return bestSoFar();
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            boolean ranOutOfTime = elapsedTime > getTimeout() || (foundApproximation && elapsedTime > getTimeout() / 2);
            if (ranOutOfTime) break;
        }
        return shouldSoftFail() ? bestSoFar() : null;
    }

    public void stop() {
        stop = true;
    }

    public Path bestSoFar() {
        if (startNode == null) return null;
        for (Node node : partialSolutions) {
            if (node == null) continue;
            int distance = startNode.squaredDistanceTo(node);
            if (distance > PS_MIN_DISTANCE) {
                return reconstructPath(node);
            }
        }
        return null;
    }

    public Path lastConsideredPath() {
        if (lastConsideration == null) return null;
        return reconstructPath(lastConsideration);
    }

    private void updatePartialSolutions(Node node) {
        for (int i = 0; i < PS_COEFFICIENTS.length; i++) {
            Node closest = partialSolutions[i];
            boolean closer = true;
            if (closest != null) {
                float distance = PS_COEFFICIENTS[i];
                closer = node.getPartialCost(distance) < closest.getPartialCost(distance);
            }
            if (closer) {
                partialSolutions[i] = node;
                int distance = startNode.squaredDistanceTo(node);
                if (distance > PS_MIN_DISTANCE) {
                    foundApproximation = true; // We found a good enough solution
                }
            }
        }
    }

    private void addAdjacentNodes(Node node) {
        for (Node neighbor : node.getNeighbors()) {
            addAdjacentNode(node, neighbor);
        }
    }

    private void addAdjacentNode(Node from, Node to) {
        if (!context.isInBounds(to.getPos()) || closedSet.contains(to)) return;
        double cost = from.getCost();
        if (to.isOpen()) {
            if (to.updateParent(from, cost)) {
                openSet.update(to);
            }
        } else {
            to.setParent(from, cost);
            openSet.add(to);
        }
    }

    private Path reconstructPath(Node node) {
        List<Node> nodes = new ArrayList<>();
        while (node.getParent() != null) {
            nodes.add(0, node);
            node = node.getParent();
        }
        nodes.add(0, startNode);
        return new Path(nodes.toArray(new Node[0]));
    }

    protected abstract void registerEvaluators();
    protected abstract int getTimeout();
    protected abstract int getChunkingRadius();
    protected abstract boolean shouldSoftFail();
}
