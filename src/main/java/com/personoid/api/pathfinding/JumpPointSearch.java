package com.personoid.api.pathfinding;

import com.personoid.api.pathfinding.goal.Goal;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.node.evaluator.NodeEvaluator;
import com.personoid.api.pathfinding.utils.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JumpPointSearch {
    public static Node[] getNeighbors(Node node, NodeContext context) {
        List<Node> neighbors = new ArrayList<>();
        for (Node neighbor : findNeighbors(node, context)) {
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

    private static Set<Node> findNeighbors(Node node, NodeContext context) {
        Set<Node> neighbors = new HashSet<>();
        //Bukkit.broadcastMessage("Finding neighbors for " + node.getPosition());
        for (int x = -1 ; x <= 1 ; x++) {
            for (int z = -1 ; z <= 1 ; z++) {
                BlockPos to = node.getPos().add(x, 0, z);
                if (x == 0 && z == 0) {
                    continue;
                }
                for (NodeEvaluator evaluator : context.getEvaluators()) {
                    evaluator.context(context);
                    Node neighbor = evaluator.apply(node, to, context);
                    if (neighbor != null) {
                        neighbors.add(neighbor);
                    }
                }
            }
        }
        return neighbors;
    }

/*    public static Node jump(Node current, Goal goal, NodeContext context) {
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

    public static Node getJumpNode(Node current, BlockPos next, Goal goal, NodeContext context) {
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
