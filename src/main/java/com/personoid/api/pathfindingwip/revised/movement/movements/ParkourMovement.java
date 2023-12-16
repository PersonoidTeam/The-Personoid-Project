package com.personoid.api.pathfindingwip.revised.movement.movements;

import com.personoid.api.npc.InputAction;
import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.helper.ParkourHelper;

public class ParkourMovement extends Movement {

	private final int dx;
	private final int dy;
	private final int dz;
	
	private final int distance;
	
	private final ParkourHelper parkourHelper = new ParkourHelper(this);
	
	public ParkourMovement(NPC npc, Node source, Node destination, int dx, int dy, int dz, int distance) {
		super(npc, source, destination);
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		this.distance = distance;
	}
	
	@Override
	public double cost() {
		double cost = parkourHelper.cost();
		return cost + super.cost();
	}
	
	@Override
	public void onRenderTick() {
		boolean sprint = parkourHelper.shouldSprint();
		setPressed(InputAction.SPRINT, sprint);
		
		boolean prepared = getPositionHelper().prepareParkourJump();
		if (!prepared) return;
		
		boolean jump = getJumpHelper().canJump();
		setPressed(InputAction.JUMP, jump);
	}
	
	public int getDeltaX() {
		return dx;
	}
	
	public int getDeltaY() {
		return dy;
	}
	
	public int getDeltaZ() {
		return dz;
	}
	
	public int getDistance() {
		return distance;
	}
	
}
