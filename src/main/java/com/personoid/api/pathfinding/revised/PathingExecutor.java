package com.personoid.api.pathfinding.revised;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.calc.Path;
import com.personoid.api.pathfinding.revised.calc.PathSegment;
import com.personoid.api.pathfinding.revised.calc.SearchExecutor;
import com.personoid.api.pathfinding.revised.calc.goal.Goal;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.movement.MovementExecutor;

public class PathingExecutor {
	private final NPC npc;
	private final PathingStatus status = new PathingStatus();

	public PathingExecutor(NPC npc) {
		this.npc = npc;
	}
	
	public void startPathing(Goal goal) {
		if (!MovementExecutor.isSafeToPause()) return;
		status.setGoal(goal);
		SearchExecutor.startSearch(goal);
	}
	
	public void stopPathing() {
		SearchExecutor.stopSearch();
		status.clear();
	}
	
	public void processCommand(PathingCommand command) {
		if (command == null) {
			stopPathing();
			return;
		}
		
		PathingCommandType type = command.getType();
		
		if (type == PathingCommandType.PAUSE) {
			boolean safe = MovementExecutor.isSafeToPause();
			if (safe) MovementExecutor.setPaused(true);
			return;
		}
		
		boolean pause = shouldPause();
		MovementExecutor.setPaused(pause);
		
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
		if (!MovementExecutor.hasPath()) return false;
		Path path = MovementExecutor.getPath();
		Movement movement = path.getCurrentMovement();
		PathSegment s = SearchExecutor.getBestSoFar();
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
