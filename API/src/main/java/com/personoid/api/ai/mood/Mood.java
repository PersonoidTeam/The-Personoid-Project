package com.personoid.api.ai.mood;

public class Mood {
    private final SelfMoodType moodType;
    private float value;

    public Mood(SelfMoodType moodType, float initialValue) {
        this.moodType = moodType;
        this.value = initialValue;
    }

    public SelfMoodType getMoodType() {
        return moodType;
    }

    public float getValue() {
        return value;
    }

    public void increase(float amount) {
        this.value += amount;
    }

    public void decrease(float amount) {
        this.value -= amount;
    }
}
