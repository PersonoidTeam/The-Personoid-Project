package com.personoid.api.pathfinding;

public class Cost {
    public static final double STRAIGHT = 1;
    public static final double DIAGONAL = Math.sqrt(2);
    public static final double FALL = 3;
    public static final double CLIMB = 3;
    public static final double JUMP = 5;
    public static final double STAIR = 1.5;
    public static final double PARKOUR = 5;
}
