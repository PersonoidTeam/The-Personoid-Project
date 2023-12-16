package com.personoid.api.pathfindingwip.revised.movement.helper;

import com.personoid.api.pathfindingwip.revised.calc.Cost;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.utils.CacheManager;
import org.bukkit.Material;

import java.util.HashMap;

public class StepHelper extends MovementHelper<Movement> {
	
	private static final HashMap<Material, Float> SPEED_MULTIPLIERS = new HashMap<>();
	
	static {
		SPEED_MULTIPLIERS.put(Material.WATER, 0.5f);
		SPEED_MULTIPLIERS.put(Material.COBWEB, 0.25f);
		SPEED_MULTIPLIERS.put(Material.SWEET_BERRY_BUSH, 0.8f);
	}
	
	public StepHelper(Movement m) {
		super(m);
	}
	
	// TODO slowness effect from diagonally adjacent blocks
	@Override
	public double cost() {
		Movement movement = getMovement();
		Node destination = getDestination();
		
		boolean inWater = destination.getType() == Material.WATER;
		boolean isDiagonal = movement.isDiagonal();
		boolean needsSupport = isDiagonal || movement.isDownwards();
		
		int x = destination.getX();
		int y = destination.getY();
		int z = destination.getZ();
		
		if (needsSupport && !inWater) {
			Material type = CacheManager.getBlockType(x, y - 1, z);
			if (!type.isSolid()) return Cost.INFINITY;
		}
		
		if (movement.isVerticalOnly()) return 0;
		
		float f = 1;
		
		if (!inWater) {
			BlockState state = BlockInterface.getState(x, y - 1, z);
			Material block = state.getBlock();
			f = block.getVelocityMultiplier();
		}
		
		float m1 = getSpeedMultiplier(x, y, z);
		float m2 = getSpeedMultiplier(x, y + 1, z);
		
		f *= Math.min(m1, m2);
		
		double cost = isDiagonal ? Cost.SPRINT_DIAGONALLY : Cost.SPRINT_STRAIGHT;
		return cost / f;
	}
	
	public static float getSpeedMultiplier(int x, int y, int z) {
		BlockState state = BlockInterface.getState(x, y, z);
		Material block = state.getBlock();
		return SPEED_MULTIPLIERS.getOrDefault(block, 1f);
	}
	
}
