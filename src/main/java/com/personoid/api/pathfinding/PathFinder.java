package com.personoid.api.pathfinding;

import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.utils.BlockPos;
import org.bukkit.World;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class PathFinder {
    private final PathingConfig config = new PathingConfig();

    public static Path findPath(BlockPos start, BlockPos end, World world) {
        NodeContext context = new NodeContext(start, end, world);
        Node startNode = new Node(start, null, context);
        Node endNode = new Node(end, null, context);
        context.setEndNode(endNode);

        Set<Node> openSet = new HashSet<>();
        Set<Node> closedSet = new HashSet<>();

        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.stream().min(Comparator.comparing(Node::getFinalExpense)).get();

            if (currentNode.getPosition().equals(endNode.getPosition())) {
                // Reconstruct the path
                Node[] nodes = new Node[currentNode.getPathLength()];
                Node node = currentNode;
                for (int i = nodes.length - 1; i >= 0; i--) {
                    nodes[i] = node;
                    node = node.getParent();
                }
                return new Path(nodes);
            }

            openSet.remove(currentNode);
            closedSet.add(currentNode);

            for (Node neighbor : currentNode.getNeighbors()) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double cost = (neighbor.isDiagonal() ? Cost.DIAGONAL : Cost.STRAIGHT);
                double tentativeExpense = currentNode.getExpense() + cost;

                if (tentativeExpense < neighbor.getExpense()) {
                    neighbor.setParent(currentNode);
                    neighbor.setFinalExpense(tentativeExpense + neighbor.getExpenseLeft());
                    openSet.add(neighbor);
                }
            }
        }

        // No path found
        return null;
    }

    public PathingConfig getConfig() {
        return config;
    }
}
