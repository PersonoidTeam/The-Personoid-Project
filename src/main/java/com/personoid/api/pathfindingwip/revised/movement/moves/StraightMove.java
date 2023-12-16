package com.personoid.api.pathfindingwip.revised.movement.moves;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.calc.PathFinder;
import com.personoid.api.pathfindingwip.revised.movement.Move;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.movements.StraightMovement;

public class StraightMove extends Move {
	
	public StraightMove(int x, int y, int z) {
		super(x, y, z);
	}
	
	@Override
	public Movement apply(NPC npc, Node node, PathFinder finder) {
		Node destination = finder.getAdjacentNode(node, getDeltaX(), getDeltaY(), getDeltaZ());
		return new StraightMovement(node, destination);
	}
	
}
