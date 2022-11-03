package com.personoid.api.pathfinding.revised.movement.movements;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.movement.Movement;
import de.stylextv.maple.input.InputAction;

public class DiagonalMovement extends Movement {
	
	public DiagonalMovement(Node source, Node destination) {
		super(source, destination);
	}
	
	@Override
	public void updateHelpers() {
		getInteractHelper().collectDefaultBlocks();
	}
	
	@Override
	public double cost() {
		double cost = getInteractHelper().cost();
		return cost + super.cost();
	}
	
	@Override
	public void onRenderTick() {
		if (getInteractHelper().onRenderTick()) return;
		lookAt(getDestination());
		setPressed(InputAction.MOVE_FORWARD, true);
		setPressed(InputAction.SPRINT, true);
	}
	
}
