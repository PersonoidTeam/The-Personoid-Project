package com.personoid.api.pathfinding;

public class PathingConfig {
    private int maxFallDistance = 3;
    private boolean useClimbing = true;
    private boolean useBlockPlacement = true;
    private boolean useDiagonalMovement = true;
    private boolean useChunking = true;

    private double diagonalMovementCost = 1;
    private double fallingCost = 0.7;
    private double climbingCost = 1.4;
    private double jumpingCost = 0.95;
    private double stairsCost = 0.8;
    private int chunkSize = 25;
    private int chunkSizeYMod = 3;
    private int maxNodeTests = 1000;

    //region toggle getters and setters

    public int getMaxFallDistance() {
        return maxFallDistance;
    }

    public void setMaxFallDistance(int maxFallDistance) {
        this.maxFallDistance = maxFallDistance;
    }

    public boolean canUseClimbing() {
        return useClimbing;
    }

    public void useClimbing(boolean useClimbing) {
        this.useClimbing = useClimbing;
    }

    public boolean canUseBlockPlacement() {
        return useBlockPlacement;
    }

    public void useBlockPlacement(boolean useBlockPlacement) {
        this.useBlockPlacement = useBlockPlacement;
    }

    public boolean canUseDiagonalMovement() {
        return useDiagonalMovement;
    }

    public void useDiagonalMovement(boolean useDiagonalMovement) {
        this.useDiagonalMovement = useDiagonalMovement;
    }

    public boolean canUseChunking() {
        return useChunking;
    }

    public void setUseChunking(boolean useChunking) {
        this.useChunking = useChunking;
    }

    //endregion

    //region value getters and setters

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

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkSizeYMod() {
        return chunkSizeYMod;
    }

    public void setChunkSizeYMod(int chunkSizeYMod) {
        this.chunkSizeYMod = chunkSizeYMod;
    }

    public int getMaxNodeTests() {
        return maxNodeTests;
    }

    public void setMaxNodeTests(int maxNodeTests) {
        this.maxNodeTests = maxNodeTests;
    }

    //endregion
}
