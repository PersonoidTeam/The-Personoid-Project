package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.Node;

public class TwoBlocksGoal extends Goal {
	private final BlockPos pos;
	
	public TwoBlocksGoal(int x, int y, int z) {
		this(new BlockPos(x, y, z));
	}
	
	public TwoBlocksGoal(BlockPos pos) {
		this.pos = pos;
	}
	
	@Override
	public double heuristic(Node node) {
		int dx = node.getX() - pos.getX();
		int dy = node.getY() - pos.getY();
		int dz = node.getZ() - pos.getZ();

		if (dy < 0) dy++;
		return BlockGoal.cost(dx, dy, dz);
	}
	
	@Override
	public boolean isFinalNode(Node node) {
		int x = node.getX();
		int y = node.getY();
		int z = node.getZ();

		return x == pos.getX() && (y == pos.getY() || y == pos.getY() - 1) && z == pos.getZ();
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof TwoBlocksGoal)) return false;
		BlockPos otherPos = ((TwoBlocksGoal) other).getPos();
		return otherPos.equals(pos);
	}
	
	@Override
	public String toString() {
		return String.format("TwoBlocksGoal{pos=%s}", pos);
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
}
