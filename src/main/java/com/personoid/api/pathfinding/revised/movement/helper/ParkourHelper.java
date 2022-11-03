package com.personoid.api.pathfinding.revised.movement.helper;

import com.personoid.api.pathfinding.revised.calc.Cost;
import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.movement.movements.ParkourMovement;
import de.stylextv.maple.cache.CacheManager;
import de.stylextv.maple.cache.block.BlockType;

public class ParkourHelper extends MovementHelper<ParkourMovement> {
	
	public ParkourHelper(ParkourMovement m) {
		super(m);
	}
	
	@Override
	public double cost() {
		ParkourMovement movement = getMovement();
		int dis = movement.getDistance();
		
		Node source = getSource();
		Node destination = getDestination();
		
		if (isObstructed(destination, 2)) return Cost.INFINITY;
		
		int startX = source.getX();
		int startY = source.getY();
		int startZ = source.getZ();
		
		int dx = movement.getDeltaX();
		int dz = movement.getDeltaZ();
		
		int height = 3 + movement.getDeltaY();
		
		for (int i = 0; i < dis; i++) {
			int x = startX + dx * i;
			int z = startZ + dz * i;
			if (isObstructed(x, startY, z, height)) return Cost.INFINITY;
		}
		
		boolean sprint = shouldSprint();
		double cost = sprint ? Cost.SPRINT_STRAIGHT : Cost.WALK_STRAIGHT;
		return cost * dis;
	}
	
	public boolean shouldSprint() {
		ParkourMovement movement = getMovement();
		double dis = movement.getDistance() + movement.getDeltaY();
		return dis > 3;
	}
	
	public static boolean isObstructed(Node n, int height) {
		return isObstructed(n, 0, height);
	}
	
	public static boolean isObstructed(Node n, int offset, int height) {
		int x = n.getX();
		int y = n.getY();
		int z = n.getZ();
		return isObstructed(x, y, z, offset, height);
	}
	
	public static boolean isObstructed(int x, int y, int z, int height) {
		return isObstructed(x, y, z, 0, height);
	}
	
	public static boolean isObstructed(int x, int y, int z, int offset, int height) {
		for (int i = 0; i < height; i++) {
			if (isObstructed(x, y + offset + i, z)) return true;
		}
		return false;
	}
	
	public static boolean isObstructed(int x, int y, int z) {
		BlockType type = CacheManager.getBlockType(x, y, z);
		if (!type.isAir()) return true;

		float f = StepHelper.getSpeedMultiplier(x, y, z);
		return f != 1;
	}
	
}
