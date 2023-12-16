package com.personoid.api.pathfindingold;

public class Cost {
    public static final double INFINITY = 100000000;
    public static final double WALK_STRAIGHT = 4.6328;
    public static final double WALK_DIAGONALLY = 6.5518;
    public static final double SPRINT_STRAIGHT = 3.5638;
    public static final double SPRINT_DIAGONALLY = 5.04;
    public static final double JUMP = fall(1.25) - fall(0.25);
    public static final double BUMP_INTO_CORNER = 3.2;
    public static final int PLACE_BLOCK = 20;

    public static double fall(double distance) {
        if (distance == 0) return 0;
        return Math.sqrt(distance * 25.5102);
    }
}
