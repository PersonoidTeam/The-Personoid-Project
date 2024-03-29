package com.personoid.api.utils.math;

import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {
    private MathUtils() {}

    public static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static double round(double value) {
        return round(value, 0);
    }

    public static double remap(double value, double maxValue, double minDesired, double maxDesired){
        double inOne = value/maxValue;
        double difference = maxDesired - minDesired;
        double inBetweenUnfinished = difference * inOne;
        return inBetweenUnfinished + minDesired;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        if (places == 0) return Math.round(value);
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(Math.min(value, max), min);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }

    public static double difference(double x1, double x2) {
        return Math.abs(x2 - x1);
    }

    public static void clean(Vector vector) {
        if (!NumberConversions.isFinite(vector.getX())) vector.setX(0);
        if (!NumberConversions.isFinite(vector.getY())) vector.setY(0);
        if (!NumberConversions.isFinite(vector.getZ())) vector.setZ(0);
    }

    public static boolean isNotFinite(Vector vector) {
        return !NumberConversions.isFinite(vector.getX()) || !NumberConversions.isFinite(vector.getY()) ||
                !NumberConversions.isFinite(vector.getZ());
    }

    public static boolean random() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public static Vector lerpVector(Vector v1, Vector v2, float t) {
        return v1.clone().add(v2.clone().subtract(v1).multiply(t));
    }

    public static float lerpYaw(float v1, float v2, float t) {
        // take into account angles
        float diff = v2 - v1;
        if (diff > 180) {
            v2 -= 360;
        } else if (diff < -180) {
            v2 += 360;
        }
        return v1 + (v2 - v1) * t;
    }

    public static float lerpRotation(float f1, float f2, float t) {
        float f3 = wrapDegrees(f2 - f1);
        if (f3 > t) f3 = t;
        if (f3 < -t) f3 = -t;

        float f4 = f1 + f3;
        if (f4 < 0F) f4 += 360F;
        else if (f4 > 360F) f4 -= 360F;
        return f4;
    }

    public static float wrapDegrees(float value) {
        float wrapped = value % 360F;
        if (wrapped >= 180F) wrapped -= 360.0F;
        if (wrapped < -180F) wrapped += 360.0F;
        return wrapped;
    }

    public static Vector getDirection(Location loc1, Location loc2) {
        return loc2.clone().subtract(loc1).toVector();
    }

    public static int floor(double value) {
        int intValue = (int) value;
        return value < (double) intValue ? intValue - 1 : intValue;
    }
}
