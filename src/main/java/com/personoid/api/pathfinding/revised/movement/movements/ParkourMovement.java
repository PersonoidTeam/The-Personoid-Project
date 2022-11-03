package com.personoid.api.pathfinding.revised.movement.movements;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.pathfinding.revised.movement.helper.ParkourHelper;
import de.stylextv.maple.input.InputAction;

public class ParkourMovement extends Movement {

	private final int dx;
	private final int dy;
	private final int dz;
	
	private final int distance;
	
	private final ParkourHelper parkourHelper = new ParkourHelper(this);
	
	public ParkourMovement(Node source, Node destination, int dx, int dy, int dz, int distance) {
		super(source, destination);
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
