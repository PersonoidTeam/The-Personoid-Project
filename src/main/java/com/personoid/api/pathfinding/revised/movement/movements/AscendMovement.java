package com.personoid.api.pathfinding.revised.movement.movements;

import com.personoid.api.pathfinding.revised.calc.Cost;
import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.movement.MovementState;
import de.stylextv.maple.input.InputAction;

public class AscendMovement extends Movement {
	
	public AscendMovement(Node source, Node destination) {
		super(source, destination);
	}
	
	@Override
	public void updateHelpers() {
		getBreakHelper().collectBlocks(getDestination(), 2);
		getBreakHelper().collectBlocks(getSource(), 2, 1);
		
		Node destination = getDestination();
		getPlaceHelper().collectBlock(destination, -1);

		getInteractHelper().collectDefaultBlocks();
	}
	
	@Override
	public double cost() {
		double cost = Cost.JUMP + getBreakHelper().cost();
		cost += getPlaceHelper().cost();
		cost += getInteractHelper().cost();
		return cost + super.cost();
	}
	
	@Override
	public void onRenderTick() {
		boolean interacting = getBreakHelper().onRenderTick() || getInteractHelper().onRenderTick();
		if (interacting) return;
		
		if (!getPlaceHelper().onRenderTick(true)) {
			lookAt(getDestination());

			setPressed(InputAction.MOVE_FORWARD, true);
			setPressed(InputAction.SPRINT, true);
			
			boolean jump = getJumpHelper().shouldJump();
			setPressed(InputAction.JUMP, jump);
		}
	}
	
	@Override
	public MovementState getState() {
		if (getPlaceHelper().hasTargets()) return MovementState.PROCEEDING;
		return super.getState();
	}
	
}
