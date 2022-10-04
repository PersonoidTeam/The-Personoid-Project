package com.personoid.api.ai.mood;

public class Mood {
    private final MoodType moodType;
    private float value;

    public Mood(MoodType moodType, float initialValue) {
        this.moodType = moodType;
        this.value = initialValue;
    }

    public MoodType getMoodType() {
        return moodType;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void increase(float amount) {
        this.value += amount;
    }

    public void decrease(float amount) {
        this.value -= amount;
    }
}
