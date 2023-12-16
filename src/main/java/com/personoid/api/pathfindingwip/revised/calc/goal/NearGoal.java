package com.personoid.api.pathfindingwip.revised.calc.goal;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfinding.calc.utils.BlockPos;

public class NearGoal extends Goal {
	private final BlockPos pos;
	private final float dis;
	
	public NearGoal(int x, int y, int z, float dis) {
		this(new BlockPos(x, y, z), dis);
	}
	
	public NearGoal(BlockPos pos, float dis) {
		this.pos = pos;
		this.dis = dis * dis;
	}
	
	@Override
	public double heuristic(Node n) {
		int dx = n.getX() - pos.getX();
		int dy = n.getY() - pos.getY();
		int dz = n.getZ() - pos.getZ();
		
		return BlockGoal.cost(dx, dy, dz);
	}
	
	@Override
	public boolean isFinalNode(Node n) {
		return n.squaredDistanceTo(pos) <= dis;
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof NearGoal)) return false;
		NearGoal g = (NearGoal) other;
		
		BlockPos otherPos = g.getPos();
		float otherDis = g.getDistance();
		
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
