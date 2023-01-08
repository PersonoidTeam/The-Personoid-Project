package com.personoid.api.npc;

public enum Pose {
    /**The default NPC pose (standing).*/
    STANDING(1F),
    /**Sneaking/crouching.*/
    SNEAKING(0.5F),
    /**Lying down on back.*/
    SLEEPING(0F),
    /**Lying down on front - breaststroke animation.*/
    SWIMMING(1F),
    /**Lying down on front.*/
    FLYING(1F),
    /**Trident spinning animation.*/
    SPINNING(1F),
    /**Fall over sideways pose (red overlay).*/
    DYING(0F);

    private final float speedModifier;

    Pose(float speedModifier) {
        this.speedModifier = speedModifier;
    }

    public float getSpeedModifier() {
        return speedModifier;
    }
}
