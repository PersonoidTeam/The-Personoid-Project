package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;

public class AxisGoal extends Goal {
	@Override
	public double heuristic(Node node) {
		int x = Math.abs(node.getX());
		int z = Math.abs(node.getZ());
		
		int majorDist = Math.min(x, z);
		int minorDist = Math.abs(x - z) / 2;
		
		return Math.min(majorDist * Cost.SPRINT_STRAIGHT, minorDist * Cost.SPRINT_DIAGONALLY);
	}
	
	@Override
	public boolean isFinalNode(Node node) {
		int x = node.getX();
		int z = node.getZ();
		
		return x == 0 || z == 0 || Math.abs(x) == Math.abs(z);
	}
	
	@Override
	public boolean equals(Goal other) {
		return other instanceof AxisGoal;
	}
	
	@Override
	public String toString() {
		return "AxisGoal";
	}
	
}
