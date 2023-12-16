package com.personoid.api.pathfindingwip.revised.movement.helper;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import de.stylextv.maple.context.PlayerContext;
import de.stylextv.maple.input.controller.AwarenessController;
import de.stylextv.maple.util.world.CollisionUtil;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class JumpHelper extends MovementHelper<Movement> {
	private static final Box COLLISION_BOX = new Box(-0.5, 0.6, -0.5, 0.5, 1.5, 0.5);
	
	public JumpHelper(Movement m) {
		super(m);
	}
	
	public boolean shouldJump() {
		if (!canJump()) return false;
		return isCollisionAhead();
	}
	
	private boolean isCollisionAhead() {
		Node source = getSource();
		Node destination = getDestination();
		
		double x = (source.getX() + destination.getX() + 1) * 0.5;
		double z = (source.getZ() + destination.getZ() + 1) * 0.5;
		
		Vec3d v = PlayerContext.position();
		
		double y = Math.max(v.getY(), source.getY());
		if (y >= destination.getY()) return false;
		
		Box box = COLLISION_BOX.offset(x, y, z);
		return CollisionUtil.collidesWithBlocks(box);
	}
	
	public boolean canJump() {
		return AwarenessController.canJump();
	}
	
}
