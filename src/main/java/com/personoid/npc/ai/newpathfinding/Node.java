package com.personoid.npc.ai.newpathfinding;

import org.bukkit.Location;
import org.bukkit.Material;

public class Node {
    private final AStarPathfinder pathfinder;
    private final Location location;
    public int x;
    public int y;
    public int z;

    public Node origin;

    public double expense;
    double estimatedExpenseLeft = -1;

    // ---
    // CONSTRUCTORS
    // ---

    public Node(AStarPathfinder pathfinder, Location loc, double expense, Node origin) {
        this.pathfinder = pathfinder;
        location = loc;
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        this.origin = origin;
        this.expense = expense;
    }

    // ---
    // GETTERS
    // ---

    public Location getLocation() {
        return location;
    }

    public double getEstimatedFinalExpense() {
        if (estimatedExpenseLeft == -1) estimatedExpenseLeft = pathfinder.distanceTo(location, pathfinder.endLocation);
        return expense + 1.1 * estimatedExpenseLeft;
    }

    // ---
    // PATHFINDING
    // ---

    public void getReachableLocations() {
        //trying to get all possibly walkable blocks
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (!(x == 0 && z == 0) && x * z == 0) {
                    Location loc = new Location(location.getWorld(), location.getBlockX() + x, location.getBlockY(), location.getBlockZ() + z);

                    // usual unchanged y
                    if (pathfinder.canStandAt(loc)) reachNode(loc, expense + 1);

                    // one block up
                    if (!pathfinder.isObstructed(loc.clone().add(-x, 2, -z))) { // block above current tile, thats why subtracting x and z
                        Location nLoc = loc.clone().add(0, 1, 0);
                        if (pathfinder.canStandAt(nLoc)) reachNode(nLoc, expense + 1.4142);
                    }

                    // one block down or falling multiple blocks down
                    if (!pathfinder.isObstructed(loc.clone().add(0, 1, 0))) { // block above possible new tile
                        Location nLoc = loc.clone().add(0, -1, 0);
                        if (pathfinder.canStandAt(nLoc)) reachNode(nLoc, expense + 1.4142); // one block down
                        else if (!pathfinder.isObstructed(nLoc) && !pathfinder.isObstructed(nLoc.clone().add(0, 1, 0))) { // fall
                            int drop = 1;
                            while (drop <= pathfinder.maxFallDistance && !pathfinder.isObstructed(loc.clone().add(0, -drop, 0))) {
                                Location locF = loc.clone().add(0, -drop, 0);
                                if (pathfinder.canStandAt(locF)) {
                                    Node fallNode = addFallNode(loc, expense + 1);
                                    fallNode.reachNode(locF, expense + drop * 2);
                                }
                                drop++;
                            }
                        }
                    }

                    //ladder
                    if (pathfinder.canClimbLadders) {
                        if (loc.clone().add(-x, 0, -z).getBlock().getType() == Material.LADDER) {
                            Location nLoc = loc.clone().add(-x, 0, -z);
                            int up = 1;
                            while (nLoc.clone().add(0, up, 0).getBlock().getType() == Material.LADDER) up++;
                            reachNode(nLoc.clone().add(0, up, 0), expense + up * 2);
                        }
                    }

                }
            }
        }
    }

    public void reachNode(Location locThere, double expenseThere) {
        Node nt = pathfinder.getNode(locThere);
        if (nt.origin == null && nt != pathfinder.startNode) { // new node
            nt.expense = expenseThere;
            nt.origin = this;
            pathfinder.uncheckedNodes.add(nt);
            return;
        }

        // no new node
        if (nt.expense > expenseThere) { // this way is faster to go there
            nt.expense = expenseThere;
            nt.origin = this;
        }
    }

    public Node addFallNode(Location loc, double expense) {
        return new Node(pathfinder, loc, expense, this);
    }

}
