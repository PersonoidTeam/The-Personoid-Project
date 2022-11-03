package com.personoid.api.pathfinding.revised.movement.movements;

import com.personoid.api.pathfinding.revised.calc.Cost;
import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.movement.MovementState;
import de.stylextv.maple.context.PlayerContext;
import de.stylextv.maple.input.InputAction;

public class PillarMovement extends Movement {
	
	public PillarMovement(Node source, Node destination) {
		super(source, destination);
	}
	
	@Override
	public void updateHelpers() {
		getBreakHelper().collectBlocks(getSource(), 2, 1);
		getPlaceHelper().collectBlock(getSource());
		
		getInteractHelper().collectDefaultBlocks();
	}
	
	@Override
	public double cost() {
		double cost = Cost.JUMP;
		cost += getBreakHelper().cost();
		cost += getInteractHelper().cost();

		if (getPlaceHelper().cost() >= Cost.INFINITY) return Cost.INFINITY;

		cost += Cost.placeCost();
		return cost + super.cost();
	}
	
	@Override
	public void onRenderTick() {
		boolean interacting = getBreakHelper().onRenderTick() || getInteractHelper().onRenderTick();
		if (interacting) return;

		boolean atCenter = getPositionHelper().centerOnSource();
		if (!atCenter) return;
		
		getPlaceHelper().onRenderTick(false);
		
		boolean jump = getJumpHelper().canJump();
		setPressed(InputAction.JUMP, jump);
	}
	
	@Override
	public MovementState getState() {
		if (!PlayerContext.isOnGround()) return MovementState.PROCEEDING;
		return super.getState();
	}
	
}
