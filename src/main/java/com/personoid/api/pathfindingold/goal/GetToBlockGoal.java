package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.Node;

public class GetToBlockGoal extends Goal {
	private final BlockPos pos;
	
	public GetToBlockGoal(int x, int y, int z) {
		this(new BlockPos(x, y, z));
	}
	
	public GetToBlockGoal(BlockPos pos) {
		this.pos = pos;
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
		int disX = Math.abs(node.getX() - pos.getX());
		int disY = Math.abs(node.getY() - pos.getY());
		int disZ = Math.abs(node.getZ() - pos.getZ());

		int dis = disX + disY + disZ;
		return dis < 2;
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof GetToBlockGoal)) return false;
		BlockPos otherPos = ((GetToBlockGoal) other).getPos();
		
		return otherPos.equals(pos);
	}
	
	@Override
	public String toString() {
		return String.format("GetToBlockGoal{pos=%s}", pos);
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
}
