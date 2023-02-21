package com.personoid.api.pathfinding.utils;

import com.personoid.api.pathfinding.NodeContext;

public class PathUtils {
    public static boolean isDiagonal(BlockPos start, BlockPos end) {
        return start.getX() != end.getX() && start.getZ() != end.getZ();
    }

    public static boolean isStraight(BlockPos start, BlockPos end) {
        return start.getX() == end.getX() || start.getZ() == end.getZ();
    }

    public static boolean isWalkable(BlockPos pos, NodeContext context) {
        return context.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getType().isSolid();
    }
}
