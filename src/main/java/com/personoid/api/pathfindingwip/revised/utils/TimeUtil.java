package com.personoid.api.pathfindingwip.revised.utils;

public class TimeUtil {
    public static long ticksToMS(double ticks) {
        return Math.round(ticks * 50);
    }
}
