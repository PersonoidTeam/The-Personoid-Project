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

    private double diagonalMovementCost = 1;
    private double fallingCost = 0.7;
    private double climbingCost = 1.4;
    private double jumpingCost = 1.1;
    private double stairsCost = 0.8;
    private double parkourCost = 0.7;

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

    // costs

    public double getDiagonalMovementCost() {
        return diagonalMovementCost;
    }

    public void setDiagonalMovementCost(double diagonalMovementCost) {
        this.diagonalMovementCost = diagonalMovementCost;
    }

    public double getFallingCost() {
        return fallingCost;
    }

    public void setFallingCost(double fallingCost) {
        this.fallingCost = fallingCost;
    }

    public double getClimbingCost() {
        return climbingCost;
    }

    public void setClimbingCost(double climbingCost) {
        this.climbingCost = climbingCost;
    }

    public double getJumpingCost() {
        return jumpingCost;
    }

    public void setJumpingCost(double jumpingCost) {
        this.jumpingCost = jumpingCost;
    }

    public double getStairsCost() {
        return stairsCost;
    }

    public void setStairsCost(double stairsCost) {
        this.stairsCost = stairsCost;
    }

    public double getParkourCost() {
        return parkourCost;
    }

    public void setParkourCost(double parkourCost) {
        this.parkourCost = parkourCost;
    }

    //endregion
}
