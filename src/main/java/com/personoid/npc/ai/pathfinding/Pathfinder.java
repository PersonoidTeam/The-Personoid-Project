package com.personoid.npc.ai.pathfinding;

import com.personoid.utils.debug.Profiler;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;

public class Pathfinder {
    final Options options;
    final int maxNodeTests;
    final ArrayList<Node> checkedNodes = new ArrayList<>();
    final ArrayList<Node> uncheckedNodes = new ArrayList<>();
    Node startNode;
    Location endLocation;

    public Pathfinder(int maxNodeTests, Options options) {
        this.maxNodeTests = maxNodeTests;
        this.options = options;
    }

    public Path getPath(Location start, Location end) {
        checkedNodes.clear();
        uncheckedNodes.clear();
        endLocation = end;
        boolean pathFound = options.canUseChunking();

        startNode = new Node(this, start, 0, null);
        Node endNode = new Node(this, end, 0, null);

        // check if player could stand at start and endpoint, if not return empty path
        //if (!(canStandAt(start) && canStandAt(end))) return null;

        // time for benchmark
        long nsStart = System.nanoTime();
        uncheckedNodes.add(startNode);
        double bestExpense = Double.MAX_VALUE;

        // cycle through untested nodes until an exit condition is fulfilled
        while (checkedNodes.size() < maxNodeTests && uncheckedNodes.size() > 0) {
            Node best = uncheckedNodes.get(0);
            for (Node node : uncheckedNodes) {
                if (node.getEstimatedFinalExpense() < best.getEstimatedFinalExpense()) {
                    best = node;
                }
            }

            if (options.canUseChunking() && best.estimatedExpenseLeft < bestExpense) {
                endNode = best;
                bestExpense = best.estimatedExpenseLeft;
            }

            if (best.estimatedExpenseLeft < 1) {
                pathFound = true;
                endNode = best;
                // print information about last node
/*                Bukkit.broadcastMessage(uncheckedNodes.size() + "uc " + checkedNodes.size() + "c " + round(best.expense) + "cne " +
                        round(best.getEstimatedFinalExpense()) + "cnee ");*/
                break;
            }

            best.getReachableLocations();
            uncheckedNodes.remove(best);
            checkedNodes.add(best);
        }

        // returning if no path has been found
        if (!pathFound) {
            float duration = (System.nanoTime() - nsStart) / 1000000f;
            Profiler.push(Profiler.Type.A_STAR, "A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) + duration + "ms" +
                    ChatColor.WHITE + " to not find a path.");
            return null;
        }

        // get length of path to create array, 1 because of start
        int length = 1;
        Node node = endNode;
        while (node.origin != null) {
            node = node.origin;
            length++;
        }

        Node[] nodes = new Node[length];

        //fill Array
        node = endNode;
        for (int i = length - 1; i > 0; i--) {
            nodes[i] = node;
            node = node.origin;
        }
        nodes[0] = startNode;

        // outputting benchmark result
        float duration = (System.nanoTime() - nsStart) / 1000000f;
        Profiler.push(Profiler.Type.A_STAR, "A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) + duration + "ms" +
                ChatColor.WHITE + " to find a path.");
        return new Path(nodes);
    }

    public Node getNode(Location loc) {
        Node test = new Node(this, loc, 0, null);
        for (Node n : checkedNodes) {
            if (n.x == test.x && n.y == test.y && n.z == test.z) {
                return n;
            }
        }
        return test;
    }

    public double round(double d) {
        return ((int) (d * 100)) / 100D;
    }

    public double distanceTo(Location loc1, Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) return Double.MAX_VALUE;
        double deltaX = Math.abs(loc1.getX() - loc2.getX());
        double deltaY = Math.abs(loc1.getY() - loc2.getY());
        double deltaZ = Math.abs(loc1.getZ() - loc2.getZ());

        // euclidean distance
        double distance2d = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        return Math.sqrt(distance2d * distance2d + deltaY * deltaY);

        // manhattan distance
        //return deltaX + deltaY + deltaZ;
    }

    public static class Options {
        private int maxFallDistance;
        private boolean useClimbing;
        private boolean useBlockPlacement;
        private boolean useDiagonalMovement = true;
        private boolean useChunking = true;

        private double diagonalMovementCost = 1;
        private double fallingCost = 0.7;
        private double climbingCost = 1.4;
        private double jumpingCost = 1.05;
        private double stairsCost = 0.8;
        private int chunkingRadius = 10;

        public Options(int maxFallDistance, boolean allowClimbing, boolean allowBlockPlacement) {
            this.maxFallDistance = maxFallDistance;
            this.useClimbing = allowClimbing;
            this.useBlockPlacement = allowBlockPlacement;
        }

        //region toggle getters and setters

        public int getMaxFallDistance() {
            return maxFallDistance;
        }

        public void setMaxFallDistance(int maxFallDistance) {
            this.maxFallDistance = maxFallDistance;
        }

        public boolean canUseClimbing() {
            return useClimbing;
        }

        public void useClimbing(boolean useClimbing) {
            this.useClimbing = useClimbing;
        }

        public boolean canUseBlockPlacement() {
            return useBlockPlacement;
        }

        public void useBlockPlacement(boolean useBlockPlacement) {
            this.useBlockPlacement = useBlockPlacement;
        }

        public boolean canUseDiagonalMovement() {
            return useDiagonalMovement;
        }

        public void useDiagonalMovement(boolean useDiagonalMovement) {
            this.useDiagonalMovement = useDiagonalMovement;
        }

        public boolean canUseChunking() {
            return useChunking;
        }

        public void setUseChunking(boolean useChunking) {
            this.useChunking = useChunking;
        }

        //endregion

        //region value getters and setters

        public double getDiagonalMovementCost() {
            return diagonalMovementCost;
        }

        public void setDiagonalMovementCost(double diagonalMovementCost) {
            this.diagonalMovementCost = diagonalMovementCost;
        }

        public double getFallingCost() {
            return fallingCost;
        }

        public void setFallingCost(double fallingCost) {
            this.fallingCost = fallingCost;
        }

        public double getClimbingCost() {
            return climbingCost;
        }

        public void setClimbingCost(double climbingCost) {
            this.climbingCost = climbingCost;
        }

        public double getJumpingCost() {
            return jumpingCost;
        }

        public void setJumpingCost(double jumpingCost) {
            this.jumpingCost = jumpingCost;
        }

        public double getStairsCost() {
            return stairsCost;
        }

        public void setStairsCost(double stairsCost) {
            this.stairsCost = stairsCost;
        }

        public int getChunkSize() {
            return chunkingRadius;
        }

        public void setChunkSize(int chunkingRadius) {
            this.chunkingRadius = chunkingRadius;
        }

        //endregion
    }
}
