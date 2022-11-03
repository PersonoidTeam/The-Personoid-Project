package com.personoid.api.pathfinding.revised.calc.goal;

import com.personoid.api.pathfinding.revised.calc.Cost;
import com.personoid.api.pathfinding.revised.calc.Node;

public class AxisGoal extends Goal {
	@Override
	public double heuristic(Node n) {
		int x = Math.abs(n.getX());
		int z = Math.abs(n.getZ());
		
		int majorDis = Math.min(x, z);
		
		int minorDis = Math.abs(x - z) / 2;
		
		return Math.min(majorDis * Cost.SPRINT_STRAIGHT, minorDis * Cost.SPRINT_DIAGONALLY);
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
