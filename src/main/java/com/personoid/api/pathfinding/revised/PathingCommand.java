package com.personoid.api.pathfinding.revised;

import com.personoid.api.pathfinding.revised.calc.goal.Goal;

public class PathingCommand {
	public static final PathingCommand PAUSE = new PathingCommand(PathingCommandType.PAUSE);
	public static final PathingCommand CANCEL = new PathingCommand(PathingCommandType.CANCEL);
	public static final PathingCommand DEFER = new PathingCommand(PathingCommandType.DEFER);
	
	private final PathingCommandType type;
	private final Goal goal;
	
	public PathingCommand(PathingCommandType type) {
		this(type, null);
	}
	
	public PathingCommand(PathingCommandType type, Goal goal) {
		this.type = type;
		this.goal = goal;
	}
	
	public PathingCommandType getType() {
		return type;
	}
	
	public Goal getGoal() {
		return goal;
	}
	
}
