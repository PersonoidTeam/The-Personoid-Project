package com.personoid.api.pathfinding;

public class PathingConfig {
    private boolean useChunking = true;

    private boolean useDiagonalMovement = true;
    private boolean useClimbing = true;
    private boolean useParkour = true;

    private int maxNodeTests = 1000;
    private int chunkSize = 25;

    private int maxFallDistance = 3;
    private int maxParkourLength = 4;

    //region toggle getters and setters

    public boolean canUseChunking() {
        return useChunking;
    }

    public void setUseChunking(boolean useChunking) {
        this.useChunking = useChunking;
    }

    // movement types

    public boolean canUseDiagonalMovement() {
        return useDiagonalMovement;
    }

    public void useDiagonalMovement(boolean useDiagonalMovement) {
        this.useDiagonalMovement = useDiagonalMovement;
    }

    public boolean canUseClimbing() {
        return useClimbing;
    }

    public void useClimbing(boolean useClimbing) {
        this.useClimbing = useClimbing;
    }

    public boolean canUseParkour() {
        return useParkour;
    }

    public void useParkour(boolean useParkour) {
        this.useParkour = useParkour;
    }

    //endregion

    //region value getters and setters

    public int getMaxNodeTests() {
        return maxNodeTests;
    }

    public void setMaxNodeTests(int maxNodeTests) {
        this.maxNodeTests = maxNodeTests;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    // misc values

    public int getMaxFallDistance() {
        return maxFallDistance;
    }

    public void setMaxFallDistance(int maxFallDistance) {
        this.maxFallDistance = maxFallDistance;
    }

    public int getMaxParkourLength() {
        return maxParkourLength;
    }

    public void setMaxParkourLength(int maxParkourLength) {
        this.maxParkourLength = maxParkourLength;
    }

    //endregion
}
