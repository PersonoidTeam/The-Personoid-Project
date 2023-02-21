package com.personoid.api.pathfinding.utils;

public class Heuristics {
    public static double manhattan(BlockPos start, BlockPos end) {
        return Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY()) + Math.abs(start.getZ() - end.getZ());
    }

    public static double euclidean(BlockPos start, BlockPos end) {
        double dx = start.getX() - end.getX();
        double dy = start.getY() - end.getY();
        double dz = start.getZ() - end.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
