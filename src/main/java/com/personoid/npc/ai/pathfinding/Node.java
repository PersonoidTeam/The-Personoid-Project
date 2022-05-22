package com.personoid.npc.ai.pathfinding;

import com.personoid.utils.LocationUtils;
import com.personoid.utils.values.BlockTypes;
import org.bukkit.Location;

public class Node {
    final Pathfinder pathfinder;
    final Location location;
    int x, y, z;
    Node origin;
    double expense;
    double estimatedExpenseLeft = -1;

    public Node(Pathfinder pathfinder, Location loc, double expense, Node origin) {
        this.pathfinder = pathfinder;
        location = loc;
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        this.origin = origin;
        this.expense = expense;
    }

    public Location getLocation() {
        return location;
    }

    public double getEstimatedFinalExpense() {
        if (estimatedExpenseLeft == -1) estimatedExpenseLeft = pathfinder.distanceTo(location, pathfinder.endLocation);
        return expense + 1.1 * estimatedExpenseLeft;
    }

    public void getReachableLocations() {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // check is current node location or diagonal
                if (x == 0 && z == 0 || (!pathfinder.options.canUseDiagonalMovement() && x * z != 0)) continue;

                Location loc = new Location(location.getWorld(), location.getBlockX() + x, location.getBlockY(), location.getBlockZ() + z);

                // check if outside of pathfinder chunk
                if (pathfinder.options.canUseChunking() && pathfinder.startNode.getLocation().distance(loc) > pathfinder.options.getChunkSize()) continue;

                // movement
                if (LocationUtils.canStandAt(loc)) {
                    if (x * z != 0) { // if diagonal movement
                        reachNode(loc, expense + pathfinder.options.getDiagonalMovementCost());
                    } else {
                        reachNode(loc, expense + 1);
                    }
                }

                // jumping
                if (!LocationUtils.isSolid(loc.clone().add(-x, 2, -z))) {
                    Location upLoc = loc.clone().add(0, 1, 0);
                    if (LocationUtils.canStandAt(upLoc)) {
                        if (upLoc.getBlock().getType().name().contains("STAIRS")) {
                            reachNode(upLoc, expense + pathfinder.options.getStairsCost());
                        } else {
                            reachNode(upLoc, expense + pathfinder.options.getJumpingCost());
                        }
                    }
                }

                // falling
                if (!LocationUtils.isSolid(loc.clone().add(0, 1, 0))) { // block above possible new tile
                    Location nLoc = loc.clone().add(0, -1, 0);
                    if (LocationUtils.canStandAt(nLoc)) reachNode(nLoc, expense + pathfinder.options.getFallingCost()); // one block down
                    else if (!LocationUtils.isSolid(nLoc) && !LocationUtils.isSolid(nLoc.clone().add(0, 1, 0))) { // fall
                        int drop = 1;
                        while (drop <= pathfinder.options.getMaxFallDistance() && !LocationUtils.isSolid(loc.clone().add(0, -drop, 0))) {
                            Location dropLoc = loc.clone().add(0, -drop, 0);
                            if (LocationUtils.canStandAt(dropLoc)) {
                                Node fallNode = createNode(loc, expense + 1);
                                fallNode.reachNode(dropLoc, expense + (drop * pathfinder.options.getFallingCost()));
                            }
                            drop++;
                        }
                    }
                }

                // climbing
                if (pathfinder.options.canUseClimbing()) {
                    if (BlockTypes.isClimbable(loc.clone().add(-x, 0, -z).getBlock().getType())) {
                        Location nLoc = loc.clone().add(-x, 0, -z);
                        int up = 1;
                        while (BlockTypes.isClimbable(nLoc.clone().add(0, up, 0).getBlock().getType())) up++;
                        reachNode(nLoc.clone().add(0, up, 0), expense + (up * pathfinder.options.getClimbingCost()));
                    }
                }
            }
        }
    }

    public void reachNode(Location locThere, double expenseThere) {
        Node node = pathfinder.getNode(locThere);
        if (node.origin == null && node != pathfinder.startNode) { // new node
            node.expense = expenseThere;
            node.origin = this;
            pathfinder.uncheckedNodes.add(node);
            return;
        }
        if (node.expense > expenseThere) {
            node.expense = expenseThere;
            node.origin = this;
        }
    }

    public Node createNode(Location loc, double expense) {
        return new Node(pathfinder, loc, expense, this);
    }

    public Node cloneAndMove(int x, int y, int z) {
        return new Node(pathfinder, new Location(location.getWorld(), x, y, z), expense, origin);
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Node getOrigin() {
        return origin;
    }

    public double getExpense() {
        return expense;
    }

    public double getEstimatedExpenseLeft() {
        return estimatedExpenseLeft;
    }
}
