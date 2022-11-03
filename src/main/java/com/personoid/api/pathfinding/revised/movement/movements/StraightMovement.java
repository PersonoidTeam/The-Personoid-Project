package com.personoid.api.pathfinding.revised.movement.movements;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.movement.MovementState;
import de.stylextv.maple.cache.block.BlockType;
import de.stylextv.maple.input.InputAction;

public class StraightMovement extends Movement {
	
	public StraightMovement(Node source, Node destination) {
		super(source, destination);
	}
	
	@Override
	public void updateHelpers() {
		Node destination = getDestination();
		getBreakHelper().collectBlocks(destination, 2);

		BlockType type = destination.getType();
		if (type != BlockType.WATER) getPlaceHelper().collectBlock(destination, -1);

		getInteractHelper().collectDefaultBlocks();
	}
	
	@Override
	public double cost() {
		double cost = getBreakHelper().cost();
		cost += getPlaceHelper().cost();
		cost += getInteractHelper().cost();
		return cost + super.cost();
	}
	
	@Override
	public void onRenderTick() {
		boolean interacting = getBreakHelper().onRenderTick() || getInteractHelper().onRenderTick();
		if (interacting) return;
		
		if (!getPlaceHelper().onRenderTick(false)) {
			lookAt(getDestination());
			setPressed(InputAction.MOVE_FORWARD, true);
			setPressed(InputAction.SPRINT, true);
		}
	}
	
	@Override
	public MovementState getState() {
		if (getPlaceHelper().hasTargets()) return MovementState.PROCEEDING;
		return super.getState();
	}
	
}
