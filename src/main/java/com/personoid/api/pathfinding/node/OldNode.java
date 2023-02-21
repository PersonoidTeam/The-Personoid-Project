package com.personoid.api.pathfinding.node;

import com.personoid.api.pathfinding.utils.BlockPos;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.types.BlockTags;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;

public class OldNode extends Node implements Comparable<OldNode> {
    final PathingContext context;
    OldNode origin;
    double expense;
    double expenseLeft = -1;
    int heapIndex;

    public OldNode(BlockPos loc, double expense, OldNode origin, PathingContext context) {
        super(loc);
        this.origin = origin;
        this.expense = expense;
        this.context = context;
    }

    public double getFinalExpense() {
        if (expenseLeft == -1) expenseLeft = LocationUtils.euclideanDistance(getPosition(), context.getEndLocation());
        return expense + 1.1 * expenseLeft;
    }

    public void getReachableLocations() {
        int chunkSize = context.getConfig().getChunkSize();
        boolean canUseDiagonalMovement = context.getConfig().canUseDiagonalMovement();
        boolean canUseChunking = context.getConfig().canUseChunking();
        boolean canUseClimbing = context.getConfig().canUseClimbing();
        boolean canUseParkour = context.getConfig().canUseParkour();
        int maxParkourLength = context.getConfig().getMaxParkourLength();
        int maxParkourHeight = 1; //context.getConfig().getMaxParkourHeight();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // check is current node location or diagonal
                if (x == 0 && z == 0 || (!canUseDiagonalMovement && x * z != 0)) continue;

                BlockPos loc = getPosition().add(x, 0, z);
                double distToStart = context.getStartLocation().distance(loc);

                // check if outside of pathfinder chunk
                if (distToStart > chunkSize && canUseChunking) continue;

                // movement
                Location locWorld = loc.toLocation(context.getWorld());
                if (LocationUtils.canStandAt(locWorld)) {
                    double movementCost = (x * z != 0)
                            ? context.getConfig().getDiagonalMovementCost()
                            : context.getConfig().getForwardMovementCost();
                    addNode(loc, expense + movementCost);
                }

                // jumping
                BlockPos locAbove = loc.add(-x, 2, -z);
                if (!BlockTags.SOLID.is(locAbove, context.getWorld())) {
                    BlockPos upLoc = loc.above();
                    Location upLocWorld = upLoc.toLocation(context.getWorld());
                    if (LocationUtils.canStandAt(upLocWorld)) {
                        double jumpingCost = context.getConfig().getJumpingCost();
                        Block upBlock = upLocWorld.getBlock();
                        if (upBlock.getState().getBlockData() instanceof Stairs) {
                            jumpingCost = context.getConfig().getStairsCost();
                        }
                        addNode(upLoc, expense + jumpingCost);
                    }
                }

                // falling
                if (!BlockTags.SOLID.is(loc.add(0, 1, 0), context.getWorld())) {
                    BlockPos nLoc = loc.add(0, -1, 0);
                    if (LocationUtils.canStandAt(nLoc, context.getWorld())) {
                        addNode(nLoc, expense + context.getConfig().getFallingCost());
                    } else {
                        boolean canFall = true;
                        for (int drop = 1; drop <= context.getConfig().getMaxFallDistance(); drop++) {
                            BlockPos dropLoc = loc.add(0, -drop, 0);
                            if (!BlockTags.SOLID.is(dropLoc, context.getWorld())) {
                                canFall = false;
                                break;
                            }
                            if (LocationUtils.canStandAt(dropLoc.below(), context.getWorld())) {
                                OldNode fallNode = createNode(loc, expense + 1);
                                fallNode.addNode(dropLoc, expense + (drop * context.getConfig().getFallingCost()));
                            }
                        }
                        if (canFall && distToStart <= chunkSize && canUseChunking) {
                            addNode(nLoc, expense + context.getConfig().getFallingCost());
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

                if (context.getConfig().canUseParkour()) {
                    for (int i = 1; i <= maxParkourLength; i++) {
                        BlockPos nLoc = loc.add(-x * i, 1, -z * i);
                        if (LocationUtils.canStandAt(nLoc, context.getWorld())) {
                            boolean canParkour = true;
                            for (int k = 1; k < i; k++) {
                                if (!BlockTags.SOLID.is(nLoc.add(x * k, 0, z * k), context.getWorld())) {
                                    canParkour = false;
                                    break;
                                }
                            }
                            if (canParkour) {
                                addNode(nLoc, expense + (i * context.getConfig().getParkourCost()));
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void addNode(BlockPos locThere, double expenseThere) {
        OldNode node = context.getNode(locThere);
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

    public OldNode createNode(BlockPos loc, double expense) {
        return new OldNode(loc, expense, this, context);
    }

    public OldNode getOrigin() {
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
    public int compareTo(@NotNull OldNode other) {
        int compare = Double.compare(getFinalExpense(), other.getFinalExpense());
        if (compare == 0) compare = Double.compare(getExpense(), other.getExpense());
        return -compare;
    }
}
