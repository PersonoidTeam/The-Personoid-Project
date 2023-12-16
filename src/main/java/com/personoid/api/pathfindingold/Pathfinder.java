package com.personoid.api.pathfindingold;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.favouring.Favouring;
import com.personoid.api.pathfindingold.goal.Goal;
import com.personoid.api.pathfindingold.movement.Move;
import com.personoid.api.pathfindingold.movement.Movement;

import java.util.*;

public class Pathfinder {
    private final NPC npc;
    private final Favouring favouring;
    private final Map<BlockPos, Node> map = new HashMap<>();
    private Goal goal;

    private boolean stop;

    public Pathfinder(NPC npc, Favouring favouring) {
        this.npc = npc;
        this.favouring = favouring;
    }

    public PathSegment findPath(BlockPos start, Goal goal) {
        this.goal = goal;
        // initialize open and closed sets
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();

        // add the start node to the open set
        Node startNode = new Node(start.getX(), start.getY(), start.getZ());
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            if (stop) return null;
            // get the node with the lowest F cost from the open set
            Node current = openSet.poll();

            if (goal.isFinalNode(current)) {
                return reconstructPath(current); // path found
            }

            closedSet.add(current);

            // for each move of the current node
            for (Move move : Move.getMoves()) {
                Movement movement = move.apply(npc, current, this);
                if (movement == null) continue;

                Node dest = movement.getDestination();
                if (closedSet.contains(dest)) continue;

                double cost = movement.favoredCost(favouring);
                if (cost >= Cost.INFINITY) continue;

                dest.setParent(current, movement, cost);
                openSet.add(dest);
            }
        }

        return null; // no path found
    }

    private PathSegment reconstructPath(Node node) {
        // reconstruct the path from the target node to the start node
        List<Node> path = new ArrayList<>();
        Node current = node;
        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }
        return new PathSegment(path.toArray(new Node[0]));
    }

    public void stop() {
        stop = true;
    }

    public Favouring getFavouring() {
        return favouring;
    }

    public Goal getGoal() {
        return goal;
    }

    public Node getAdjacentNode(Node node, int dx, int dy, int dz) {
        int x = node.getX() + dx;
        int y = node.getY() + dy;
        int z = node.getZ() + dz;
        return getNode(x, y, z);
    }

    private Node getNode(int x, int y, int z) {
        BlockPos blockPos = new BlockPos(x, y, z);
        Node node = map.get(blockPos);
        if (node != null) return node;
        node = new Node(x, y, z);
        node.updateHeuristic(goal);
        map.put(blockPos, node);
        return node;
    }
}
