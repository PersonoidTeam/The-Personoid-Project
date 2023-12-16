package com.personoid.api.pathfindingwip.revised.calc.goal;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfinding.calc.utils.BlockPos;
import com.personoid.api.utils.bukkit.Direction;

public class StrictDirectionGoal extends Goal {
	private static final int DEVIATION_COST = 1000;
	private static final int PROGRESS_BONUS = 100;
	
	private final BlockPos pos;
	private final Direction dir;
	
	public StrictDirectionGoal(int x, int y, int z, Direction dir) {
		this(new BlockPos(x, y, z), dir);
	}
	
	public StrictDirectionGoal(BlockPos pos, Direction dir) {
		this.pos = pos;
		this.dir = dir;
	}
	
	@Override
	public double heuristic(Node n) {
		int dx = n.getX() - pos.getX();
		int dy = n.getY() - pos.getY();
		int dz = n.getZ() - pos.getZ();
		
		int dirX = dir.getOffsetX();
		int dirY = dir.getOffsetY();
		int dirZ = dir.getOffsetZ();
		
		int dis = dx * dirX + dy * dirY + dz * dirZ;
		
		int devX = dx * (Math.abs(dirX) ^ 1);
		int devY = dy * (Math.abs(dirY) ^ 1);
		int devZ = dz * (Math.abs(dirZ) ^ 1);
		
		int dev = Math.abs(devX) + Math.abs(devY) + Math.abs(devZ);
		
		return dev * DEVIATION_COST - dis * PROGRESS_BONUS;
	}
	
	@Override
	public boolean isFinalNode(Node n) {
		return false;
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof StrictDirectionGoal)) return false;
		
		StrictDirectionGoal g = (StrictDirectionGoal) other;
		
		BlockPos otherPos = g.getPos();
		Direction otherDir = g.getDirection();
		
		return otherPos.equals(pos) && otherDir == dir;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public Direction getDirection() {
		return dir;
	}
	
	@Override
	public String toString() {
		return String.format("StrictDirectionGoal{pos=%s, dir=%s}", pos, dir);
	}
	
}
