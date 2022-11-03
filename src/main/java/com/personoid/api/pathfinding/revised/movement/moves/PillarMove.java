package com.personoid.api.pathfinding.revised.movement.moves;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.calc.PathFinder;
import com.personoid.api.pathfinding.revised.movement.Move;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.movement.movements.PillarMovement;

public class PillarMove extends Move {
	
	public PillarMove(int x, int y, int z) {
		super(x, y, z);
	}
	
	@Override
	public Movement apply(Node n, PathFinder finder) {
		Node destination = finder.getAdjacentNode(n, getDeltaX(), getDeltaY(), getDeltaZ());
		return new PillarMovement(n, destination);
	}
	
}
