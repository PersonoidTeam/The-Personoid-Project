package com.personoid.api.pathfindingwip.revised.movement.movements;

import com.personoid.api.npc.InputAction;
import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.MovementState;
import org.bukkit.Material;

public class StraightMovement extends Movement {
	
	public StraightMovement(NPC npc, Node source, Node destination) {
		super(npc, source, destination);
	}
	
	@Override
	public void updateHelpers() {
		Node destination = getDestination();
		getBreakHelper().collectBlocks(destination, 2);

		Material type = destination.getType();
		if (type != Material.WATER) getPlaceHelper().collectBlock(destination, -1);

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
