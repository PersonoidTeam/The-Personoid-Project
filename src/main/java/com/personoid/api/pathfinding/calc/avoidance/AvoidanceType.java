package com.personoid.api.pathfinding.calc.avoidance;

import com.personoid.api.utils.math.MathUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;

public class AvoidanceType {
    public static final AvoidanceType WEAK = new AvoidanceType(6, 1.2);
    public static final AvoidanceType NORMAL = new AvoidanceType(8, 1.5);
    public static final AvoidanceType STRONG = new AvoidanceType(10, 1.9);
    public static final AvoidanceType BEAST = new AvoidanceType(12, 2.5);

    private static final AvoidanceType[] TYPES = { WEAK, NORMAL, STRONG, BEAST };
    private static final double LOG_TWO_OF_TEN = MathUtils.log2(10);

    private final int radius;
    private final double coefficient;

    public AvoidanceType(int radius, double coefficient) {
        this.radius = radius;
        this.coefficient = coefficient;
    }

    public int getRadius() {
        return radius;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public static AvoidanceType fromEntity(Entity entity) {
        if (!(entity instanceof LivingEntity)) return null;
        LivingEntity livingEntity = (LivingEntity) entity;
        if (isAggressive(livingEntity)) {
            if (AvoidanceFilter.shouldIgnore(entity)) return null;
            return fromHealth(livingEntity.getHealth());
        }
        return null;
    }

    public static boolean isAggressive(LivingEntity entity) {
        return entity instanceof Monster;
    }

    public static AvoidanceType fromHealth(double health) {
        double d = MathUtils.log2(health);
        int i = (int) Math.round(d - LOG_TWO_OF_TEN);
        int max = TYPES.length - 1;
        i = MathUtils.clamp(i, 0, max);
        return TYPES[i];
    }
}
