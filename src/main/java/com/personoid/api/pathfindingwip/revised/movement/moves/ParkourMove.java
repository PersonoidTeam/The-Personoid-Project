package com.personoid.api.pathfindingwip.revised.movement.moves;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.calc.PathFinder;
import com.personoid.api.pathfindingwip.revised.movement.Move;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.helper.ParkourHelper;
import com.personoid.api.pathfindingwip.revised.movement.movements.ParkourMovement;
import de.stylextv.maple.cache.CacheManager;
import de.stylextv.maple.cache.block.BlockType;

public class ParkourMove extends Move {
	
	private static final int MAX_DISTANCE = 4;
	
	public ParkourMove(int x, int y, int z) {
		super(x, y, z);
	}
	
	@Override
	public Movement apply(NPC npc, Node node, PathFinder finder) {
		if (ParkourHelper.isObstructed(node, 2, 1)) return null;
		
		int dx = getDeltaX();
		int dy = 0;
		int dz = getDeltaZ();

		int i = 1;
		while (i <= MAX_DISTANCE) {
			int ox = dx * i;
			int oz = dz * i;
			
			Node destination = finder.getAdjacentNode(node, ox, dy, oz);
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
				return new ParkourMovement(npc, node, destination, dx, dy, dz, i);
			}

			i++;
		}
		return null;
	}
	
}
