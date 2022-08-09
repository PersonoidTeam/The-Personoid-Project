package com.personoid.api.utils.types;

public enum Priority {
    HIGHEST(1F),
    HIGH(0.75F),
    NORMAL(0.5F),
    LOW(0.25F),
    LOWEST(0F),
    CUSTOM(0);

    float value;

    Priority(float value) {
        this.value = value;
    }

    public float getValue(){
        return value;
    }

    public boolean isHigherThan(Priority priority){
        return priority.getValue() < getValue();
    }

    public Priority value(float value) {
        this.value = value;
        if (this != CUSTOM) throw new RuntimeException("Cannot change priority value of a non-custom priority");
        return this;
    }
}
