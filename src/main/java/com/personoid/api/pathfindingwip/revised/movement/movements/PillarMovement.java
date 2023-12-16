package com.personoid.api.pathfindingwip.revised.movement.movements;

import com.personoid.api.npc.InputAction;
import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Cost;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.MovementState;

public class PillarMovement extends Movement {
	
	public PillarMovement(NPC npc, Node source, Node destination) {
		super(npc, source, destination);
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
		if (!npc.isOnGround()) return MovementState.PROCEEDING;
		return super.getState();
	}
	
}
