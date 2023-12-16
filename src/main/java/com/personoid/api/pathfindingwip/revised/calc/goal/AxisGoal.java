package com.personoid.api.pathfindingwip.revised.calc.goal;

import com.personoid.api.pathfindingwip.revised.calc.Cost;
import com.personoid.api.pathfindingwip.revised.calc.Node;

public class AxisGoal extends Goal {
	@Override
	public double heuristic(Node n) {
		int x = Math.abs(n.getX());
		int z = Math.abs(n.getZ());
		
		int majorDist = Math.min(x, z);
		int minorDist = Math.abs(x - z) / 2;
		
		return Math.min(majorDist * Cost.SPRINT_STRAIGHT, minorDist * Cost.SPRINT_DIAGONALLY);
	}
	
	@Override
	public boolean isFinalNode(Node n) {
		int x = n.getX();
		int z = n.getZ();
		
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
