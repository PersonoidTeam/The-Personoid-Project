package com.personoid.api.pathfinding.revised.calc.goal;

import com.personoid.api.pathfinding.revised.calc.Node;

public class InvertedGoal extends Goal {
	private final Goal goal;
	
	public InvertedGoal(Goal goal) {
		this.goal = goal;
	}
	
	@Override
	public double heuristic(Node n) {
		return -goal.heuristic(n);
	}
	
	@Override
	public boolean isFinalNode(Node n) {
		return false;
	}
	
	@Override
	public boolean equals(Goal other) {
		if(!(other instanceof InvertedGoal)) return false;
		
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
