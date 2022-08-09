package com.personoid.api.utils.math;

public class Percent {
    private final float min;
    private final float max;
    private float value;

    public Percent(float min, float max, float value) {
        this.min = min;
        this.max = max;
        this.value = value;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getPercent() {
        return (value - min) / (max - min);
    }

    @Override
    public String toString() {
        return MathUtils.round(getPercent(), 1) + "%";
    }
}
