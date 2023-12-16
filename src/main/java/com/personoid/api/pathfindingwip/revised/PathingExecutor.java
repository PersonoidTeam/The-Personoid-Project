package com.personoid.api.pathfindingwip.revised;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.calc.Path;
import com.personoid.api.pathfindingwip.revised.calc.PathSegment;
import com.personoid.api.pathfindingwip.revised.calc.SearchExecutor;
import com.personoid.api.pathfindingwip.revised.calc.goal.Goal;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.MovementExecutor;

public class PathingExecutor {
	private final PathingStatus status = new PathingStatus();
	private final MovementExecutor movementExecutor;
	private final SearchExecutor searchExecutor;

	public PathingExecutor(MovementExecutor movementExecutor, SearchExecutor searchExecutor) {
		this.movementExecutor = movementExecutor;
		this.searchExecutor = searchExecutor;
	}
	
	public void startPathing(Goal goal) {
		if (!movementExecutor.isSafeToPause()) return;
		status.setGoal(goal);
		searchExecutor.startSearch(goal);
	}
	
	public void stopPathing() {
		searchExecutor.stopSearch();
		status.clear();
	}
	
	public void processCommand(PathingCommand command) {
		if (command == null) {
			stopPathing();
			return;
		}
		
		PathingCommandType type = command.getType();
		
		if (type == PathingCommandType.PAUSE) {
			boolean safe = movementExecutor.isSafeToPause();
			if (safe) movementExecutor.setPaused(true);
			return;
		}
		
		boolean pause = shouldPause();
		movementExecutor.setPaused(pause);
		
		if (type == PathingCommandType.CANCEL) {
			stopPathing();
			return;
		}
		
		if (type == PathingCommandType.DEFER) return;
		
		Goal goal = command.getGoal();
		boolean idle = status.isIdle();
		boolean forceStart = idle || type == PathingCommandType.PATH_TO_GOAL;
		
		if (forceStart) {
			startPathing(goal);
			return;
		}

		boolean destinationValid = status.destinationMatches(goal);
		
		if (type == PathingCommandType.REVALIDATE_GOAL) {
			boolean foundGoal = status.hasFoundGoal();
			boolean invalid = foundGoal && !destinationValid;
			if (invalid) startPathing(goal);
			return;
		}
		
		if (type == PathingCommandType.FORCE_REVALIDATE_GOAL) {
			boolean invalid = !destinationValid && !status.goalMatches(goal);
			if (invalid) startPathing(goal);
		}
	}
	
	private boolean shouldPause() {
		if (!movementExecutor.hasPath()) return false;
		Path path = movementExecutor.getPath();
		Movement movement = path.getCurrentMovement();
		PathSegment s = searchExecutor.getBestSoFar();
		if (movement == null || s == null) return false;
		Node source = movement.getSource();
		return s.contains(source);
	}
	
	public Goal getGoal() {
		return status.getGoal();
	}
	
	public boolean isPathing() {
		return status.isPathing();
	}
	
	public PathingStatus getStatus() {
		return status;
	}

}
