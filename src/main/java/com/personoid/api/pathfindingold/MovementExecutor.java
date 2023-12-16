package com.personoid.api.pathfindingold;

import com.personoid.api.pathfindingold.movement.Movement;

public class MovementExecutor {
    private static final int MAX_DISTANCE_TO_PATH = 9;

    private Path path;
    private boolean paused;
    private boolean safeToPause;

    public void followPath(Path path) {
        stop();
        this.path = path;
    }

    public void stop() {
        path = null;
    }

    public void recalc() {
        path.clear();
    }

    public void tick() {
        if (path == null) return;
        boolean required = PathingExecutor.isPathing();

        if (!required && isSafeToPause()) {
            stop();
            return;
        }

        Movement movement = path.getCurrentMovement();

        if (movement == null) {
            if (path.isFinished()) {
                if (required) {
                    boolean atGoal = path.getState() == PathState.FOUND_GOAL;
                    PathingStatus status = PathingExecutor.getStatus();
                    PathingState state = atGoal ? PathingState.AT_GOAL : PathingState.FAILED;
                    status.setState(state);
                }
                stop();
            }
            return;
        }

        if (needsToRecalc()) {
            recalc();
            return;
        }

        movement.updateHelpers();

        if (!paused) {
            movement.tick();
            double dt = GameContext.lastFrameDuration();
            movement.tick(dt);
        }

        if (PlayerContext.isInWater()) {
            InputController.setPressed(InputAction.JUMP, true);
            InputController.setPressed(InputAction.SNEAK, false);
        }

        safeToPause = false;

        MovementState state = movement.getState();

        if (state == MovementState.FAILED) {
            recalc();

        } else if (state == MovementState.DONE) {
            path.next();
            safeToPause = true;
        }
    }

    private boolean needsToRecalc() {
        Movement movement = path.getCurrentMovement();
        if (movement.ranOutOfTime()) return true;

        if (!PlayerContext.isFalling() && !PlayerContext.isInWater()) {
            BlockPos pos = PlayerContext.blockPosition();
            double dis = path.distanceSqr(pos);
            if (dis > MAX_DISTANCE_TO_PATH) return true;
        }

        return path.isImpossible();
    }

    public BlockPos getDestination() {
        if (isMoving()) {
            Node node = path.lastNode();
            return node.blockPos();
        }

        return PlayerContext.feetPosition();
    }

    public boolean isSafeToPause() {
        if (paused || !isMoving()) return true;
        return safeToPause;
    }

    public boolean isMoving() {
        if (!hasPath()) return false;
        return path.getCurrentMovement() != null;
    }

    public boolean hasPath() {
        return path != null;
    }

    public Path getPath() {
        return path;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        MovementExecutor.paused = paused;
    }
}
