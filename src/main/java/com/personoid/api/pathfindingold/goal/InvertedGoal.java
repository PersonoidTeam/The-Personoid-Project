package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.Node;

public class InvertedGoal extends Goal {
	private final Goal goal;
	
	public InvertedGoal(Goal goal) {
		this.goal = goal;
	}
	
	@Override
	public double heuristic(Node node) {
		return -goal.heuristic(node);
	}
	
	@Override
	public boolean isFinalNode(Node node) {
		return false;
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof InvertedGoal)) return false;
		Goal otherGoal = ((InvertedGoal) other).getGoal();
		return otherGoal.equals(goal);
	}
	
	@Override
	public String toString() {
		return String.format("InvertedGoal{goal=%s}", goal);
	}
	
	public Goal getGoal() {
		return goal;
	}
	
}
