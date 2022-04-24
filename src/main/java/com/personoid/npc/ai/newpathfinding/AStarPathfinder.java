package com.personoid.npc.ai.newpathfinding;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;

public class AStarPathfinder {
    final int maxNodeTests;
    boolean canClimbLadders;
    double maxFallDistance;

    final ArrayList<Node> checkedNodes = new ArrayList<>();
    final ArrayList<Node> uncheckedNodes = new ArrayList<>();
    Node startNode;
    Location endLocation;

    public AStarPathfinder(int maxNodeTests, boolean canClimbLadders, double maxFallDistance) {
        this.maxNodeTests = maxNodeTests;
        this.canClimbLadders = canClimbLadders;
        this.maxFallDistance = maxFallDistance;
    }

    public Path getPath(Location start, Location end) {
        checkedNodes.clear();
        uncheckedNodes.clear();
        endLocation = end;
        boolean pathFound = false;

        startNode = new Node(this, start, 0, null);
        Node endNode = new Node(this, end, 0, null);

        // check if player could stand at start and endpoint, if not return empty path
        //if (!(canStandAt(start) && canStandAt(end))) return null;

        // time for benchmark
        long nsStart = System.nanoTime();

        uncheckedNodes.add(startNode);

        // cycle through untested nodes until an exit condition is fulfilled
        while (checkedNodes.size() < maxNodeTests && uncheckedNodes.size() > 0) {
            Node n = uncheckedNodes.get(0);
            for (Node nt : uncheckedNodes) {
                if (nt.getEstimatedFinalExpense() < n.getEstimatedFinalExpense()) {
                    n = nt;
                }
            }

            if (n.estimatedExpenseLeft < 1) {
                pathFound = true;
                endNode = n;
                // print information about last node
                Bukkit.broadcastMessage(uncheckedNodes.size() + "uc " + checkedNodes.size() + "c " + round(n.expense) + "cne " + round(n.getEstimatedFinalExpense()) + "cnee ");
                break;
            }

            n.getReachableLocations();
            uncheckedNodes.remove(n);
            checkedNodes.add(n);
        }

        // returning if no path has been found
        if (!pathFound) {
            float duration = (System.nanoTime() - nsStart) / 1000000f;
            Bukkit.broadcastMessage("A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) + duration + "ms" + ChatColor.WHITE + " to not find a path.");
            return null;
        }

        // get length of path to create array, 1 because of start
        int length = 1;
        Node n = endNode;
        while (n.origin != null) {
            n = n.origin;
            length++;
        }

        Node[] nodes = new Node[length];

        //fill Array
        n = endNode;
        for (int i = length - 1; i > 0; i--) {
            nodes[i] = n;
            n = n.origin;
        }
        nodes[0] = startNode;

        // outputting benchmark result
        float duration = (System.nanoTime() - nsStart) / 1000000f;
        Bukkit.broadcastMessage("A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) + duration + "ms" + ChatColor.WHITE + " to find a path.");
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

    public boolean isObstructed(Location loc) {
        return loc.getBlock().getType().isSolid();
    }

    public boolean canStandAt(Location loc) {
        return !(isObstructed(loc) || isObstructed(loc.clone().add(0, 1, 0)) || !isObstructed(loc.clone().add(0, -1, 0)));
    }

    public double round(double d) {
        return ((int) (d * 100)) / 100D;
    }

    public double distanceTo(Location loc1, Location loc2)
    {
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
}
