package com.personoid.api.utils;

public class TimeUtil {
	public static long ticksToMS(double ticks) {
		return Math.round(ticks * 50);
	}
}
