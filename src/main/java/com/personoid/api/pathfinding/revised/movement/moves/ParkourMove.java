package com.personoid.api.pathfinding.revised.movement.moves;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.calc.PathFinder;
import com.personoid.api.pathfinding.revised.movement.Move;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.movement.helper.ParkourHelper;
import com.personoid.api.pathfinding.revised.movement.movements.ParkourMovement;
import de.stylextv.maple.cache.CacheManager;
import de.stylextv.maple.cache.block.BlockType;

public class ParkourMove extends Move {
	
	private static final int MAX_DISTANCE = 4;
	
	public ParkourMove(int x, int y, int z) {
		super(x, y, z);
	}
	
	@Override
	public Movement apply(Node n, PathFinder finder) {
		if (ParkourHelper.isObstructed(n, 2, 1)) return null;
		
		int dx = getDeltaX();
		int dy = 0;
		int dz = getDeltaZ();

		int i = 1;
		while (i <= MAX_DISTANCE) {
			int ox = dx * i;
			int oz = dz * i;
			
			Node destination = finder.getAdjacentNode(n, ox, dy, oz);
			if (ParkourHelper.isObstructed(destination, 3)) {
				if (dy == 1) return null;
				dy++;
				continue;
			}
			
			int x = destination.getX();
			int y = destination.getY() - 1;
			int z = destination.getZ();
			
			BlockType type = CacheManager.getBlockType(x, y, z);
			if (!type.isPassable()) {
				if (i == 1) return null;
				return new ParkourMovement(n, destination, dx, dy, dz, i);
			}

			i++;
		}
		return null;
	}
	
}
