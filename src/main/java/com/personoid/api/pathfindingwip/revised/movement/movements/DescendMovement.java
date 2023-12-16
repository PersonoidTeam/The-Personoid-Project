package com.personoid.api.pathfindingwip.revised.movement.movements;

import com.personoid.api.npc.InputAction;
import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Cost;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;

public class DescendMovement extends Movement {
	
	public DescendMovement(NPC npc, Node source, Node destination) {
		super(npc, source, destination);
	}
	
	@Override
	public void updateHelpers() {
		int height = 3;
		if (isVerticalOnly()) height = 1;
		getBreakHelper().collectBlocks(getDestination(), height);
		
		getInteractHelper().collectDefaultBlocks();
	}
	
	@Override
	public double cost() {
		double cost = isVerticalOnly() ? Cost.FALL_N_BLOCKS[1] : 0;
		cost += getBreakHelper().cost();
		cost += getInteractHelper().cost();
		return cost + super.cost();
	}
	
	@Override
	public void onRenderTick() {
		boolean interacting = getBreakHelper().onRenderTick() || getInteractHelper().onRenderTick();
		if (interacting) return;
		
		boolean lookDown = isVerticalOnly();
		lookAt(getDestination(), lookDown);
		
		if (isVerticalOnly() && !npc.isOnGround()) return;
		
		setPressed(InputAction.MOVE_FORWARD, true);
		setPressed(InputAction.SPRINT, true);
	}
	
}
