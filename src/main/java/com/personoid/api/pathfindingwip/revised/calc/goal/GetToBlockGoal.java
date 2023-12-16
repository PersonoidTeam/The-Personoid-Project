package com.personoid.api.pathfindingwip.revised.calc.goal;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfinding.calc.utils.BlockPos;

public class GetToBlockGoal extends Goal {
	private final BlockPos pos;
	
	public GetToBlockGoal(int x, int y, int z) {
		this(new BlockPos(x, y, z));
	}
	
	public GetToBlockGoal(BlockPos pos) {
		this.pos = pos;
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
		int disX = Math.abs(n.getX() - pos.getX());
		int disY = Math.abs(n.getY() - pos.getY());
		int disZ = Math.abs(n.getZ() - pos.getZ());

		int dis = disX + disY + disZ;
		return dis < 2;
	}
	
/*	@Override
	public void render(RenderWorldEvent event) {
		ShapeRenderer.drawBox(event, pos, Colors.GOAL, 2);
	}*/
	
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
