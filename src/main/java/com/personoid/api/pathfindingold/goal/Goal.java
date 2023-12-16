package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.Node;

public abstract class Goal {
	public abstract double heuristic(Node node);
	public abstract boolean isFinalNode(Node node);
	public abstract boolean equals(Goal other);
}
