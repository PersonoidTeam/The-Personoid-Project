package com.personoid.api.pathfinding;

public class Cost {
    public static final double STRAIGHT = 4.6328;
    public static final double DIAGONAL = 6.5518;
    public static final double CLIMB = 1.3;
    public static final double JUMP = fall(1.25) - fall(0.25);
    public static final double STAIR = 1.1;
    public static final double PARKOUR = 1;

    public static double fall(double height) {
        if (height <= 0) return 0;
        return Math.sqrt(height * 25.5102);
    }
}
