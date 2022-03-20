package com.personoid.npc.ai;

public enum Priority {
    HIGHEST(1F),
    HIGH(0.75F),
    NORMAL(0.5F),
    LOW(0.25F),
    LOWEST(0F);

    final float value;

    Priority(float value) {
        this.value = value;
    }

    public float getValue(){
        return value;
    }

    public boolean isHigherThan(Priority priority){
        return priority.getValue() < getValue();
    }
}
