package com.personoid.api.ai.movement;

public enum MovementType {
    WALKING(1F),
    SPRINTING(1F),
    SPRINT_JUMPING(1F),
    FLYING(1F),
    SWIMMING(1F)
    ;

    private final float speed;

    MovementType(float speed) {
        this.speed = 0.2F;
    }

    public float getSpeed() {
        return speed;
    }
}
