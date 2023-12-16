package com.personoid.api.pathfindingwip.revised.movement.helper;

import com.personoid.api.pathfindingwip.revised.calc.Cost;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.utils.bukkit.BlockPos;
import de.stylextv.maple.cache.CacheManager;
import de.stylextv.maple.cache.block.BlockType;
import de.stylextv.maple.context.WorldContext;
import de.stylextv.maple.input.InputAction;
import de.stylextv.maple.input.controller.AwarenessController;
import de.stylextv.maple.input.controller.InputController;
import de.stylextv.maple.input.controller.PlaceController;
import de.stylextv.maple.input.target.targets.PlaceableTarget;

public class PlaceHelper extends TargetHelper<PlaceableTarget> {
	
	public PlaceHelper(Movement movement) {
		super(movement);
	}
	
	@Override
	public void collectBlock(int x, int y, int z) {
		BlockType type = CacheManager.getBlockType(x, y, z);
		if (type.isSolid()) return;
		
		if (!hasTarget(x, y, z)) {
			PlaceableTarget target = new PlaceableTarget(x, y, z);
			addTarget(target);
		}
	}
	
	@Override
	public double cost() {
		Movement movement = getMovement();
		boolean hasSupport = false;
		
		if (!movement.isDiagonal3D()) {
			Node source = getSource();
			BlockType type = source.getType();
			hasSupport = type != BlockType.WATER;
			Movement movement = source.getMovement();
			if (movement != null) hasSupport = movement.getPlaceHelper().hasTargets();
		}
		
		for (PlaceableTarget target : getTargets()) {
			BlockPos pos = target.getPos();
			if (WorldContext.isOutOfHeightLimit(pos)) return Cost.INFINITY;
			if (!hasSupport && !PlaceController.canPlaceAt(pos)) return Cost.INFINITY;
		}
		
		int l = getTargets().size();
		return l * Cost.placeCost();
	}
	
	public boolean onRenderTick(boolean moveIfBlocked) {
		if (!hasTargets()) return false;
		
		for (PlaceableTarget target : getTargets()) {
			if (target.isPlaced()) {
				removeTarget(target);
				continue;
			}
			
			BlockPos pos = target.getPos();

			boolean blocked = AwarenessController.isBlockingPos(pos);
			if (blocked && moveIfBlocked) {
				Movement movement = getMovement();
				movement.getPositionHelper().centerOnSource();
				return true;
			}
			
			if (target.continuePlacing()) {
				InputController.setPressed(InputAction.SNEAK, true);
				return true;
			}
		}
		
		return false;
	}
	
}
