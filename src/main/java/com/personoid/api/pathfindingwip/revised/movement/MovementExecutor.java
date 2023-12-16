package com.personoid.api.pathfindingwip.revised.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.npc.Pose;
import com.personoid.api.pathfindingwip.revised.PathingExecutor;
import com.personoid.api.pathfindingwip.revised.PathingState;
import com.personoid.api.pathfindingwip.revised.PathingStatus;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.calc.Path;
import com.personoid.api.pathfindingwip.revised.calc.PathState;
import com.personoid.api.pathfinding.calc.utils.BlockPos;

public class MovementExecutor {
	private static final int MAX_DISTANCE_TO_PATH = 9;

	private final NPC npc;
	private Path path;
	private boolean paused;
	private boolean safeToPause;

	private final PathingExecutor executor;

	public MovementExecutor(NPC npc, PathingExecutor executor) {
		this.npc = npc;
		this.executor = executor;
	}
	
	public void followPath(Path path) {
		stop();
		this.path = path;
	}
	
	public void stop() {
		path = null;
	}
	
	public void recalculate() {
		path.clear();
	}
	
	public void onRenderTick() {
		if (path == null) return;

		boolean required = executor.isPathing();
		if (!required && isSafeToPause()) {
			stop();
			return;
		}

		Movement movement = path.getCurrentMovement();
		if (movement == null) {
			if (path.isFinished()) {
				if (required) {
					boolean atGoal = path.getState() == PathState.FOUND_GOAL;
					PathingStatus status = executor.getStatus();
					PathingState state = atGoal ? PathingState.AT_GOAL : PathingState.FAILED;
					status.setState(state);
				}
				stop();
			}
			return;
		}
		
		if (needsToRecalculate()) {
			recalculate();
			return;
		}

		movement.updateHelpers();
		
		if (!paused) {
			movement.onRenderTick();
			double dt = 1; // GameContext.lastFrameDuration()
			movement.tick(dt);
		}
		
		if (npc.isInWater()) {
			npc.getMoveController().jump();
			npc.setPose(Pose.STANDING);
		}
		
		safeToPause = false;
		MovementState state = movement.getState();
		
		if (state == MovementState.FAILED) {
			recalculate();
		} else if (state == MovementState.DONE) {
			path.next();
			safeToPause = true;
		}
	}
	
	private boolean needsToRecalculate() {
		Movement movement = path.getCurrentMovement();
		if (movement.ranOutOfTime()) return true;

		if (!npc.getMoveController().isFalling() && !npc.isInWater()) {
			BlockPos pos = npc.getBlockPos();
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
		return npc.getBlockPos(); // feet pos
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
	
	public void setPaused(boolean b) {
		paused = b;
	}
	
}
