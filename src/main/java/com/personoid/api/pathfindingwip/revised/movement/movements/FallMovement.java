package com.personoid.api.pathfindingwip.revised.movement.movements;

import com.personoid.api.npc.InputAction;
import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Cost;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.MovementState;
import com.personoid.api.pathfindingwip.revised.movement.helper.FallHelper;
import com.personoid.api.utils.bukkit.BlockPos;

public class FallMovement extends Movement {
	
	private final int fallDistance;
	
	private final FallHelper fallHelper = new FallHelper(this);
	
	public FallMovement(NPC npc, Node source, Node destination, int fallDistance) {
		super(npc, source, destination);
		this.fallDistance = fallDistance;
	}
	
	@Override
	public void updateHelpers() {
		int x = getDestination().getX();
		int y = getSource().getY();
		int z = getDestination().getZ();
		
		getBreakHelper().collectBlocks(x, y, z, -1, 3);
		
		getInteractHelper().collectDefaultBlocks();
		getInteractHelper().collectBlocks(x, y, z, -1, 3);
	}
	
	@Override
	public double cost() {
		double cost = Cost.FALL_N_BLOCKS[fallDistance];
		cost += getBreakHelper().cost();
		cost += getInteractHelper().cost();
		cost += fallHelper.cost();
		return cost + super.cost();
	}
	
	@Override
	public void onRenderTick() {
		boolean interacting = getBreakHelper().onRenderTick() || getInteractHelper().onRenderTick();
		if (interacting) return;
		
		fallHelper.onRenderTick();
		Node node = getDestination();
		
		lookAt(node, true);
		BlockPos pos = npc.getBlockPos();
		
		int x = pos.getX();
		int z = pos.getZ();
		
		boolean aboveDestination = x == node.getX() && z == node.getZ();
		boolean onGround = npc.isOnGround();

		setPressed(InputAction.MOVE_FORWARD, !aboveDestination || onGround);
		setPressed(InputAction.SPRINT, true);
	}
	
	@Override
	public MovementState getState() {
		if (!fallHelper.isFinished()) return MovementState.PROCEEDING;
		return super.getState();
	}
	
	public int getFallDistance() {
		return fallDistance;
	}
	
}
