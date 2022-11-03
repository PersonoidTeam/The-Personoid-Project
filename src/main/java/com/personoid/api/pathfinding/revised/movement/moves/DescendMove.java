package com.personoid.api.pathfinding.revised.movement.moves;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.calc.PathFinder;
import com.personoid.api.pathfinding.revised.movement.Move;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.movement.movements.DescendMovement;
import com.personoid.api.pathfinding.revised.movement.movements.FallMovement;
import de.stylextv.maple.cache.block.BlockType;

public class DescendMove extends Move {
	
	public DescendMove(int x, int y, int z) {
		super(x, y, z);
	}
	
	@Override
	public Movement apply(Node node, PathFinder finder) {
		int dx = getDeltaX();
		int dy = getDeltaY();
		int dz = getDeltaZ();
		Node destination;
		
		while (true) {
			destination = finder.getAdjacentNode(node, dx, dy, dz);
			if (destination.getY() <= 0 || destination.getType() == BlockType.WATER) break;

			Node below = finder.getAdjacentNode(node, dx, dy - 1, dz);
			BlockType type = below.getType();
			if (!type.isPassable()) break;

			dy--;
		}
		
		int fallDistance = node.getY() - destination.getY();
		if (fallDistance == 1) return new DescendMovement(node, destination);
		
		return new FallMovement(node, destination, fallDistance);
	}
	
}
