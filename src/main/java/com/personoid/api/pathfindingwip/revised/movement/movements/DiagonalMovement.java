package com.personoid.api.pathfindingwip.revised.movement.movements;

import com.personoid.api.npc.InputAction;
import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;

public class DiagonalMovement extends Movement {
	
	public DiagonalMovement(NPC npc, Node source, Node destination) {
		super(npc, source, destination);
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
