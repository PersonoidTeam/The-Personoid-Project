package com.personoid.api.pathfinding;

import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.types.BlockTags;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class Node implements Comparable<Node> {
    final PathingContext context;
    final Location location;
    int x, y, z;
    Node origin;
    double expense;
    double expenseLeft = -1;
    int heapIndex;

    public Node(Location loc, double expense, Node origin, PathingContext context) {
        location = loc;
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        this.origin = origin;
        this.expense = expense;
        this.context = context;
    }

    public Location getLocation() {
        return location;
    }

    public double getFinalExpense() {
        if (expenseLeft == -1) expenseLeft = LocationUtils.euclideanDistance(location, context.getEndLocation());
        return expense + 1.1 * expenseLeft;
    }

    public void getReachableLocations() {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // check is current node location or diagonal
                if (x == 0 && z == 0 || (!context.getConfig().canUseDiagonalMovement() && x * z != 0)) continue;
                Location loc = new Location(location.getWorld(), this.x + x, this.y, this.z + z);

                // check if outside of pathfinder chunk
                Location sameY = new Location(loc.getWorld(), loc.getBlockX(), location.getBlockY(), loc.getBlockZ());
                // FIXME: chunkSizeYMod incorrect calculation
                int yDiff = Math.abs(loc.getBlockY() - context.getEndLocation().getBlockY());
                int chunkSize = context.getConfig().getChunkSize();
                boolean outsideRange = context.getStartLocation().distance(loc) > chunkSize;
                if (outsideRange && context.getConfig().canUseChunking()) {
                    continue;
/*                    if (yDiff > chunkSize) continue;
                    if (yDiff > chunkSize / 2) {
                        if (Math.abs(loc.getBlockY() - context.getStartLocation().getBlockY()) > chunkSize / 2) continue;
                    }*/
                }
                //if (context.getConfig().canUseChunking() && outsideRange) continue;

                // movement
                if (LocationUtils.canStandAt(loc)) {
                    if (x * z != 0) { // if diagonal movement
                        reachNode(loc, expense + context.getConfig().getDiagonalMovementCost());
                    } else {
                        reachNode(loc, expense + 1);
                    }
                }

                // jumping
                if (!BlockTags.SOLID.is(loc.clone().add(-x, 2, -z))) {
                    Location upLoc = loc.clone().add(0, 1, 0);
                    if (LocationUtils.canStandAt(upLoc)) {
                        if (upLoc.getBlock().getType().name().contains("STAIRS")) {
                            reachNode(upLoc, expense + context.getConfig().getStairsCost());
                        } else {
                            reachNode(upLoc, expense + context.getConfig().getJumpingCost());
                        }
                    }
                }

                // falling
                if (!BlockTags.SOLID.is(loc.clone().add(0, 1, 0))) { // block above possible new tile
                    Location nLoc = loc.clone().add(0, -1, 0);
                    if (LocationUtils.canStandAt(nLoc)) reachNode(nLoc, expense + context.getConfig().getFallingCost()); // one block down
                    else if (!BlockTags.SOLID.is(nLoc) && !BlockTags.SOLID.is(nLoc.clone().add(0, 1, 0))) { // fall
                        int drop = 1;
                        while (drop <= context.getConfig().getMaxFallDistance() && !BlockTags.SOLID.is(loc.clone().add(0, -drop, 0))) {
                            Location dropLoc = loc.clone().add(0, -drop, 0);
                            if (LocationUtils.canStandAt(dropLoc)) {
                                Node fallNode = createNode(loc, expense + 1);
                                fallNode.reachNode(dropLoc, expense + (drop * context.getConfig().getFallingCost()));
                            }
                            drop++;
                        }
                    }
                }

                // climbing
                if (context.getConfig().canUseClimbing()) {
                    if (BlockTags.CLIMBABLE.is(loc.clone().add(-x, 0, -z).getBlock().getType())) {
                        Location nLoc = loc.clone().add(-x, 0, -z);
                        int up = 1;
                        while (BlockTags.CLIMBABLE.is(nLoc.clone().add(0, up, 0).getBlock().getType())) up++;
                        reachNode(nLoc.clone().add(0, up, 0), expense + (up * context.getConfig().getClimbingCost()));
                    }
                }
            }
        }
    }

    public void reachNode(Location locThere, double expenseThere) {
        Node node = context.getNode(locThere);
        if (node.origin == null && node != context.getStartNode()) { // new node
            node.expense = expenseThere;
            node.origin = this;
            context.getOpenSet().add(node);
            return;
        }
        if (node.expense > expenseThere) {
            node.expense = expenseThere;
            node.origin = this;
        }
    }

    public Node createNode(Location loc, double expense) {
        return new Node(loc, expense, this, context);
    }

    public Node cloneAndMove(int x, int y, int z) {
        return new Node(new Location(location.getWorld(), x, y, z), expense, origin, context);
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

    public double getExpenseLeft() {
        return expenseLeft;
    }

    public int getHeapIndex() {
        return heapIndex;
    }

    public void setHeapIndex(int heapIndex) {
        this.heapIndex = heapIndex;
    }

    @Override
    public int compareTo(@NotNull Node other) {
        int compare = Double.compare(getFinalExpense(), other.getFinalExpense());
        if (compare == 0) compare = Double.compare(getExpense(), other.getExpense());
        return -compare;
    }
}
