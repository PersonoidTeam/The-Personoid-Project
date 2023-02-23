package com.personoid.api.pathfinding.utils;

public class Heuristics {
    public static int manhattan(BlockPos start, BlockPos end) {
        return Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY()) + Math.abs(start.getZ() - end.getZ());
    }

    public static double euclidean(BlockPos start, BlockPos end) {
        return Math.sqrt(squaredEuclidean(start, end));
    }

    public static int squaredEuclidean(BlockPos start, BlockPos end) {
        int dx = start.getX() - end.getX();
        int dy = start.getY() - end.getY();
        int dz = start.getZ() - end.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
