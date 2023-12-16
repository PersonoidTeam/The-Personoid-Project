package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.Cost;
import com.personoid.api.pathfindingold.Node;

public class YLevelGoal extends Goal {
	private final int y;
	
	public YLevelGoal(int y) {
		this.y = y;
	}
	
	@Override
	public double heuristic(Node node) {
		return cost(node.getY(), y);
	}
	
	@Override
	public boolean isFinalNode(Node node) {
		return node.getY() == y;
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof YLevelGoal)) return false;

		int otherY = ((YLevelGoal) other).getY();
		return otherY == y;
	}
	
	@Override
	public String toString() {
		return String.format("YGoal{y=%s}", y);
	}
	
	public int getY() {
		return y;
	}
	
	public static double cost(int y, int goalY) {
		if (y < goalY) {
			int dis = goalY - y;
			return dis * Cost.JUMP;
		}
		if (y > goalY) {
			int dis = y - goalY;
			return Cost.fall(2) / 2 * dis;
		}
		return 0;
	}
}
