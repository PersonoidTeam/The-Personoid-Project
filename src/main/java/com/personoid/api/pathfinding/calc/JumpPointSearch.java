package com.personoid.api.pathfinding.calc;

import com.personoid.api.pathfinding.calc.goal.Goal;
import com.personoid.api.pathfinding.calc.node.Node;
import com.personoid.api.pathfinding.calc.node.evaluator.NodeEvaluator;
import com.personoid.api.pathfinding.calc.utils.BlockPos;

import java.util.*;

public class JumpPointSearch {
    private final NodeContext context;
    private final Map<String, Boolean> cache = new HashMap<>();

    public JumpPointSearch(NodeContext context) {
        this.context = context;
    }

    public Node[] getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        for (Node neighbor : findNeighbors(node)) {
            neighbors.add(neighbor);
/*            Node jumpNode = jump(neighbor, context.getGoal(), context);
            if (jumpNode != null) {
                Bukkit.broadcastMessage("Found jump node " + jumpNode.getPos() + " for " + node.getPos());
                neighbors.add(jumpNode);
            } else {
                Bukkit.broadcastMessage("No jump node found for " + node.getPos());
                neighbors.add(neighbor);
            }*/
        }
        return neighbors.toArray(new Node[0]);
    }

    private Set<Node> findNeighbors(Node node) {
        Set<Node> neighbors = new HashSet<>();
        //Bukkit.broadcastMessage("Finding neighbors for " + node.getPosition());
        for (NodeEvaluator evaluator : context.getEvaluators()) {
            if (cache.containsKey(evaluator.getClass().getSimpleName())) {
                return null;
            }
            int xRange = Math.max(1, evaluator.getXRange());
            int yRange = Math.max(1, evaluator.getYRange());
            int zRange = Math.max(1, evaluator.getZRange());
            for (int x = -xRange; x <= xRange; x++) {
                for (int z = -zRange; z <= zRange; z++) {
                    for (int y = -yRange; y <= yRange; y++) {
                        int x1 = x;
                        int z1 = z;
                        int y1 = y;
                        if (evaluator.getXRange() == 0) x1 = 0;
                        if (evaluator.getZRange() == 0) z1 = 0;
                        if (evaluator.getYRange() == 0) y1 = 0;
                        if (x1 == 0 && y1 == 0 && z1 == 0) continue;
                        Node neighbor = findNeighbor(node, evaluator, x1, y1, z1);
                        if (neighbor != null) {
                            neighbors.add(neighbor);
                        }
                    }
                }
            }
        }
        return neighbors;
    }

    private Node findNeighbor(Node node, NodeEvaluator evaluator, int x, int y, int z) {
        BlockPos to = node.getPos().add(x, y, z);
        Node neighbor = apply(evaluator, node, to, x, y, z);
        cache.put(evaluator.getClass().getSimpleName(), neighbor != null);
        return neighbor;
    }

    private Node apply(NodeEvaluator evaluator, Node node, BlockPos to, int dX, int dY, int dZ) {
        for (NodeEvaluator dependency : evaluator.getDependencies()) {
            String className = dependency.getClass().getSimpleName();
            boolean neighbor;
            if (cache.containsKey(className)) {
                neighbor = cache.get(className);
            } else {
                neighbor = apply(dependency, node, to, dX, dY, dZ) != null;
                cache.put(className, neighbor);
            }
            if (!neighbor) {
                return null;
            }
        }
        evaluator.context(context);
        return evaluator.apply(node, to, dX, dY, dZ);
    }

/*    public Node jump(Node current, Goal goal, NodeContext context) {
        BlockPos currentPos = current.getPos();
        int dx = Integer.compare(goal.getX(), currentPos.getX());
        int dy = Integer.compare(goal.getY(), currentPos.getY());
        int dz = Integer.compare(goal.getZ(), currentPos.getZ());
        if (dx == 0 && dy == 0 && dz == 0) {
            return current;
        }
        if (dx != 0 && dy != 0) {
            Node jumpNode = getJumpNode(current, currentPos.add(dx, 0, 0), goal, context);
            if (jumpNode != null) {
                return jump(jumpNode, goal, context);
            }
            jumpNode = getJumpNode(current, currentPos.add(0, 0, dz), goal, context);
            if (jumpNode != null) {
                return jump(jumpNode, goal,
                        context);
            }
            jumpNode = getJumpNode(current, currentPos.add(dx, 0, dz), goal, context);
            if (jumpNode != null) {
                return jump(jumpNode, goal, context);
            }
        } else if (dx != 0) {
            Node jumpNode = getJumpNode(current, currentPos.add(dx, 0, 0), goal, context);
            if (jumpNode != null) {
                return jump(jumpNode, goal, context);
            }
        } else {
            Node jumpNode = getJumpNode(current, currentPos.add(0, 0, dz), goal, context);
            if (jumpNode != null) {
                return jump(jumpNode, goal, context);
            }
        }
        return null;
    }*/

    public Node getJumpNode(Node current, BlockPos next, Goal goal, NodeContext context) {
        // Check if the next node is blocked or out of bounds
        if (!context.isWalkable(next) || !context.isInBounds(next)) {
            return null;
        }

        Node nextNode = context.getNode(next);

        // Check if the next node is the goal
        if (goal.isFinalNode(nextNode)) {
            return nextNode;
        }

        BlockPos currentPos = current.getPos();
        int dx = Integer.compare(next.getX(), currentPos.getX());
        int dz = Integer.compare(next.getZ(), currentPos.getZ());

        // Check for forced neighbors
        if (dx != 0 && dz != 0) {
            BlockPos nextX = next.add(-dx, 0, 0);
            BlockPos nextZ = next.add(0, 0, -dz);

            boolean hasForcedX = context.isInBounds(nextX) && !context.isWalkable(nextX) && context.isWalkable(nextX.add(0, 1, 0));
            boolean hasForcedZ = context.isInBounds(nextZ) && !context.isWalkable(nextZ) && context.isWalkable(nextZ.add(0, 1, 0));

            if (hasForcedX || hasForcedZ) {
                return nextNode;
            }
        }

        // Check for diagonal movement
        if (dx != 0 && dz != 0) {
            Node jumpNodeX = getJumpNode(current, next.add(dx, 0, 0), goal, context);
            Node jumpNodeZ = getJumpNode(current, next.add(0, 0, dz), goal, context);

            if (jumpNodeX != null || jumpNodeZ != null) {
                return nextNode;
            }
        }

        // Check for horizontal movement
        if (dx != 0) {
            Node jumpNodeX = getJumpNode(current, next.add(dx, 0, 0), goal, context);

            if (jumpNodeX != null) {
                return jumpNodeX;
            }
        }

        // Check for vertical movement
        if (dz != 0) {
            return getJumpNode(current, next.add(0, 0, dz), goal, context);
        }

        return null;
    }
}
