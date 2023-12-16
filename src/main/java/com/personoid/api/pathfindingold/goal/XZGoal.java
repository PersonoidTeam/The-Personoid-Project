package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;
import org.bukkit.util.Vector;

public class XZGoal extends Goal {
	private final int x;
	private final int z;
	
	public XZGoal(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	@Override
	public double heuristic(Node node) {
		int dx = node.getX() - x;
		int dz = node.getZ() - z;
		return cost(dx, dz);
	}
	
	@Override
	public boolean isFinalNode(Node node) {
		return node.getX() == x && node.getZ() == z;
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof XZGoal)) return false;
		
		XZGoal goal = (XZGoal) other;
		
		int otherX = goal.getX();
		int otherZ = goal.getZ();
		
		return otherX == x && otherZ == z;
	}
	
	@Override
	public String toString() {
		return String.format("XZGoal{x=%s, z=%s}", x, z);
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	public static double cost(int dx, int dz) {
		int x = Math.abs(dx);
		int z = Math.abs(dz);
		
		int straight;
		int diagonal;
		
		if (x < z) {
			straight = z - x;
			diagonal = x;
		} else {
			straight = x - z;
			diagonal = z;
		}
		
		return Cost.SPRINT_STRAIGHT * straight + Cost.SPRINT_DIAGONALLY * diagonal;
	}
	
	public static XZGoal inDirection(BlockPos pos, float yaw, double dis) {
		Vector vector = new Vector(Math.cos(yaw), 0, Math.sin(yaw));
		vector.multiply(dis).add(pos.toVector());

		int x = (int) Math.round(vector.getX());
		int z = (int) Math.round(vector.getZ());
		
		return new XZGoal(x, z);
	}
}
