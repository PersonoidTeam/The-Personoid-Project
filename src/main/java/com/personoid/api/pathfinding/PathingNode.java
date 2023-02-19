package com.personoid.api.pathfinding;

import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.types.BlockTags;
import org.jetbrains.annotations.NotNull;

public class PathingNode extends Node implements Comparable<PathingNode> {
    final PathingContext context;
    PathingNode origin;
    double expense;
    double expenseLeft = -1;
    int heapIndex;

    public PathingNode(BlockPos loc, double expense, PathingNode origin, PathingContext context) {
        super(loc);
        this.origin = origin;
        this.expense = expense;
        this.context = context;
    }

    public double getFinalExpense() {
        if (expenseLeft == -1) expenseLeft = LocationUtils.euclideanDistance(getBlockPos(), context.getEndLocation());
        return expense + 1.1 * expenseLeft;
    }

    public void getReachableLocations() {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // check is current node location or diagonal
                if (x == 0 && z == 0 || (!context.getConfig().canUseDiagonalMovement() && x * z != 0)) continue;
                BlockPos loc = getBlockPos().add(x, 0, z);

                // check if outside of pathfinder chunk
                int chunkSize = context.getConfig().getChunkSize();
                boolean outsideRange = context.getStartLocation().distance(loc) > chunkSize;
                if (outsideRange && context.getConfig().canUseChunking()) {
                    continue;
                }

                // movement
                if (LocationUtils.canStandAt(loc.toLocation(context.getWorld()))) {
                    if (x * z != 0) { // if diagonal movement
                        int xDelta = loc.getX() - this.x;
                        int zDelta = loc.getZ() - this.z;
                        BlockPos potentialWall = new BlockPos(xDelta + x, this.y, this.z);
                        if (LocationUtils.isSolid(potentialWall, context.getWorld())) continue;
                        potentialWall = new BlockPos(this.x, this.y, zDelta + z);
                        if (LocationUtils.isSolid(potentialWall, context.getWorld())) continue;
                        addNode(loc, expense + context.getConfig().getDiagonalMovementCost());
                    } else {
                        addNode(loc, expense + context.getConfig().getForwardMovementCost());
                    }
                }

                // jumping
                if (!BlockTags.SOLID.is(loc.add(-x, 2, -z), context.getWorld())) {
                    BlockPos upLoc = loc.above();
                    if (LocationUtils.canStandAt(upLoc, context.getWorld())) {
                        if (upLoc.toLocation(context.getWorld()).getBlock().getType().name().contains("STAIRS")) {
                            addNode(upLoc, expense + context.getConfig().getStairsCost());
                        } else {
                            addNode(upLoc, expense + context.getConfig().getJumpingCost());
                        }
                    }
                }

                // falling
                if (!BlockTags.SOLID.is(loc.add(0, 1, 0), context.getWorld())) { // block above possible new tile
                    BlockPos nLoc = loc.add(0, -1, 0);
                    if (LocationUtils.canStandAt(nLoc, context.getWorld())) {
                        addNode(nLoc, expense + context.getConfig().getFallingCost()); // one block down
                    } else if (!BlockTags.SOLID.is(nLoc, context.getWorld()) && !BlockTags.SOLID.is(nLoc.clone().add(0, 1, 0), context.getWorld())) { // fall
                        int drop = 1;
                        while (drop <= context.getConfig().getMaxFallDistance() && !BlockTags.SOLID.is(loc.clone().add(0, -drop, 0), context.getWorld())) {
                            BlockPos dropLoc = loc.clone().add(0, -drop, 0);
                            if (LocationUtils.canStandAt(dropLoc, context.getWorld())) {
                                PathingNode fallNode = createNode(loc, expense + 1);
                                fallNode.addNode(dropLoc, expense + (drop * context.getConfig().getFallingCost()));
                            }
                            drop++;
                        }
                    }
                }

                // climbing
                if (context.getConfig().canUseClimbing()) {
                    if (BlockTags.CLIMBABLE.is(loc.add(-x, 0, -z), context.getWorld())) {
                        BlockPos nLoc = loc.add(-x, 0, -z);
                        int up = 1;
                        while (BlockTags.CLIMBABLE.is(nLoc.clone().add(0, up, 0), context.getWorld())) up++;
                        addNode(nLoc.clone().add(0, up, 0), expense + (up * context.getConfig().getClimbingCost()));
                    }
                }

                // parkour
                // furthest jump possible: 4 blocks long and 1 block higher
/*                if (context.getConfig().canUseParkour()) {
                    BlockPos nLoc = loc.above();
                    if (LocationUtils.canStandAt(nLoc, context.getWorld())) {
                        int jumpLength = 1;
                        while (jumpLength <= context.getConfig().getMaxParkourLength() && !BlockTags.SOLID.is(nLoc.add(x, 0, z), context.getWorld())) {
                            nLoc = nLoc.clone().add(x, 0, z);
                            jumpLength++;
                        }
                        if (jumpLength > 1) {
                            addNode(nLoc, expense + (jumpLength * context.getConfig().getParkourCost()));
                        }
                    }
                }*/
            }
        }
    }

    public void addNode(BlockPos locThere, double expenseThere) {
        PathingNode node = context.getNode(locThere);
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

    public PathingNode createNode(BlockPos loc, double expense) {
        return new PathingNode(loc, expense, this, context);
    }

    public PathingNode getOrigin() {
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
    public int compareTo(@NotNull PathingNode other) {
        int compare = Double.compare(getFinalExpense(), other.getFinalExpense());
        if (compare == 0) compare = Double.compare(getExpense(), other.getExpense());
        return -compare;
    }
}
