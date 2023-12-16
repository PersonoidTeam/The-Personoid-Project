package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.Node;

public class NearGoal extends Goal {
	private final BlockPos pos;
	private final float dis;
	
	public NearGoal(int x, int y, int z, float dist) {
		this(new BlockPos(x, y, z), dist);
	}
	
	public NearGoal(BlockPos pos, float dist) {
		this.pos = pos;
		this.dis = dist * dist;
	}
	
	@Override
	public double heuristic(Node node) {
		int dx = node.getX() - pos.getX();
		int dy = node.getY() - pos.getY();
		int dz = node.getZ() - pos.getZ();
		
		return BlockGoal.cost(dx, dy, dz);
	}
	
	@Override
	public boolean isFinalNode(Node node) {
		return node.squaredDistanceTo(pos) <= dis;
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof NearGoal)) return false;
		NearGoal goal = (NearGoal) other;
		
		BlockPos otherPos = goal.getPos();
		float otherDis = goal.getDistance();
		
		return otherPos.equals(pos) && otherDis == dis;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public float getDistance() {
		return dis;
	}
	
	@Override
	public String toString() {
		return String.format("NearGoal{pos=%s, dis=%s}", pos, dis);
	}
	
}
