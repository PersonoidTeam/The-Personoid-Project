package com.personoid.api.pathfindingwip.revised.movement.helper;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.movements.ParkourMovement;
import de.stylextv.maple.context.PlayerContext;
import de.stylextv.maple.input.InputAction;
import de.stylextv.maple.input.controller.InputController;
import de.stylextv.maple.input.controller.ViewController;
import net.minecraft.util.math.Vec3d;

public class PositionHelper extends MovementHelper<Movement> {

	private static final double MAX_DISTANCE_FROM_CENTER = 0.016;
	private static final double MAX_SIDEWAYS_DISTANCE = 0.12;
	
	public PositionHelper(Movement m) {
		super(m);
	}
	
	public boolean prepareParkourJump() {
		Node source = getSource();
		
		double sourceX = source.getX() + 0.5;
		double sourceZ = source.getZ() + 0.5;
		
		ParkourMovement movement = (ParkourMovement) getMovement();
		
		int dirX = movement.getDirectionX();
		int dirZ = movement.getDirectionZ();
		
		Vec3d v = PlayerContext.position();
		
		double x = v.getX();
		double z = v.getZ();
		
		double dx = x - sourceX;
		double dz = z - sourceZ;
		
		double forwards = dx * dirX;
		double sideways = dz;
		
		if (dirX == 0) {
			forwards = dz * dirZ;
			sideways = dx;
		}
		
		double sidewaysDis = Math.abs(sideways);
		
		if (sidewaysDis > MAX_SIDEWAYS_DISTANCE) {
			double targetX = sourceX - dirX * 0.5;
			double targetZ = sourceZ - dirZ * 0.5;
			moveTo(targetX, targetZ);
			return false;
		}
		
		Node destination = movement.getDestination();
		ViewController.lookAt(destination);
		InputController.setPressed(InputAction.MOVE_FORWARD, true);
		return forwards > 0.7 && forwards < 1;
	}
	
	public boolean centerOnSource() {
		Node source = getSource();
		double sourceX = source.getX() + 0.5;
		double sourceZ = source.getZ() + 0.5;
		return moveTo(sourceX, sourceZ);
	}
	
	private boolean moveTo(double x, double z) {
		double dis = PlayerContext.squaredDistanceTo(x, z);
		boolean reached = dis < MAX_DISTANCE_FROM_CENTER;
		
		if (reached) {
			double speed = PlayerContext.horizontalSpeed();
			return speed == 0;
		}
		
		InputController.setPressed(InputAction.MOVE_FORWARD, true);
		InputController.setPressed(InputAction.SNEAK, true);
		ViewController.lookAt(x, z);

		return false;
	}
	
}
