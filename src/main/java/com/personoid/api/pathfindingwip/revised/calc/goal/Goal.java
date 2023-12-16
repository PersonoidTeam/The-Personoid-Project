package com.personoid.api.pathfindingwip.revised.calc.goal;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.utils.ArgumentList;

public abstract class Goal {
	
	public abstract double heuristic(Node n);
	
	public abstract boolean isFinalNode(Node n);
	
	/*public void render(RenderWorldEvent event) {}*/
	
	public abstract boolean equals(Goal other);
	
	public static Goal fromArgs(ArgumentList args) {
		if (args.hasAtLeast(3)) {
			int x = args.getCoordinate(0);
			int y = args.getCoordinate(1);
			int z = args.getCoordinate(2);
			
			if (args.hasAtLeast(4)) {
				float dis = args.getFloat(3);
				
				return new NearGoal(x, y, z, dis);
			}
			
			return new BlockGoal(x, y, z);
		}
		
		if (args.hasAtLeast(2)) {
			int x = args.getCoordinate(0);
			int z = args.getCoordinate(1, 2);
			
			return new XZGoal(x, z);
		}
		
		int y = args.getCoordinate(0, 1);
		
		return new YLevelGoal(y);
	}
	
}
