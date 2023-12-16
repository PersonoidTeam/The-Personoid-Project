package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.Node;

public class BlockGoal extends Goal {
	private final BlockPos pos;
	
	public BlockGoal(int x, int y, int z) {
		this(new BlockPos(x, y, z));
	}
	
	public BlockGoal(BlockPos pos) {
		this.pos = pos;
	}
	
	@Override
	public double heuristic(Node node) {
		int dx = node.getX() - pos.getX();
		int dy = node.getY() - pos.getY();
		int dz = node.getZ() - pos.getZ();

		return cost(dx, dy, dz);
	}
	
	@Override
	public boolean isFinalNode(Node node) {
		int x = node.getX();
		int y = node.getY();
		int z = node.getZ();

		return x == pos.getX() && y == pos.getY() && z == pos.getZ();
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof BlockGoal)) return false;
		BlockPos otherPos = ((BlockGoal) other).getPos();
		return otherPos.equals(pos);
	}
	
	@Override
	public String toString() {
		return String.format("BlockGoal{pos=%s}", pos);
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public static double cost(int dx, int dy, int dz) {
		double cost = XZGoal.cost(dx, dz);
		cost += YLevelGoal.cost(dy, 0);
		return cost;
	}
}
