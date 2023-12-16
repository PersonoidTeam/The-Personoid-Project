package com.personoid.api.pathfindingwip.revised.movement.moves;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.calc.PathFinder;
import com.personoid.api.pathfindingwip.revised.movement.Move;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.movement.movements.PillarMovement;

public class PillarMove extends Move {
	
	public PillarMove(int x, int y, int z) {
		super(x, y, z);
	}
	
	@Override
	public Movement apply(NPC npc, Node node, PathFinder finder) {
		Node destination = finder.getAdjacentNode(node, getDeltaX(), getDeltaY(), getDeltaZ());
		return new PillarMovement(npc, node, destination);
	}
	
}
