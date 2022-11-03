package com.personoid.api.pathfinding.revised.movement.helper;

import com.personoid.api.pathfinding.revised.calc.Cost;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.utils.BreakableTarget;
import de.stylextv.maple.cache.CacheManager;
import de.stylextv.maple.cache.block.BlockType;
import de.stylextv.maple.input.controller.AwarenessController;
import net.minecraft.util.math.BlockPos;

public class BreakHelper extends TargetHelper<BreakableTarget> {
	
	public BreakHelper(Movement m) {
		super(m);
	}
	
	@Override
	public void collectBlock(int x, int y, int z) {
		BlockType type = CacheManager.getBlockType(x, y, z);
		
		if(type.isPassable()) return;
		
		if(!hasTarget(x, y, z)) {
			
			BreakableTarget target = new BreakableTarget(x, y, z);
			
			addTarget(target);
		}
	}
	
	@Override
	public double cost() {
		int sum = 0;
		
		for(BreakableTarget target : getTargets()) {
			
			BlockPos pos = target.getPos();
			
			while(true) {
				
				sum += costOfBlock(pos);
				
				pos = pos.up();
				
				if(hasTarget(pos)) break;
				
				boolean falls = AwarenessController.isFallingBlock(pos);
				
				if(!falls) break;
			}
		}
		
		return sum;
	}
	
	private double costOfBlock(BlockPos pos) {
		BlockType type = CacheManager.getBlockType(pos);
		
		boolean unbreakable = type.isUnbreakable();
		
		if(unbreakable) return Cost.INFINITY;
		
		BlockPos from = sourcePos();
		
		boolean safe = AwarenessController.isSafeToBreak(pos, from, true, false);
		
		if(!safe) return Cost.INFINITY;
		
		return Cost.breakCost(pos);
	}
	
	public boolean onRenderTick() {
		if(!hasTargets()) return false;
		
		for(BreakableTarget target : getTargets()) {
			
			if(target.isBroken()) {
				
				BlockPos pos = target.getPos();
				
				boolean falling = AwarenessController.awaitsFallingBlock(pos);
				
				if(falling) return true;
				
				removeTarget(target);
				
				continue;
			}
			
			if(target.continueBreaking(false)) return true;
		}
		
		return false;
	}
	
}
