package com.personoid.api.pathfinding.revised.movement;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.calc.PathFinder;
import com.personoid.api.pathfinding.revised.movement.moves.*;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public abstract class Move {
	private static final Move[] MOVES = new Move[30];
	public static final Move STRAIGHT_NORTH = new StraightMove(0, 0, -1);
	public static final Move STRAIGHT_SOUTH = new StraightMove(0, 0, 1);
	public static final Move STRAIGHT_EAST = new StraightMove(1, 0, 0);
	public static final Move STRAIGHT_WEST = new StraightMove(-1, 0, 0);
	public static final Move DIAGONAL_NORTHEAST = new DiagonalMove(1, 0, -1);
	public static final Move DIAGONAL_SOUTHEAST = new DiagonalMove(1, 0, 1);
	public static final Move DIAGONAL_SOUTHWEST = new DiagonalMove(-1, 0, 1);
	public static final Move DIAGONAL_NORTHWEST = new DiagonalMove(-1, 0, -1);
	public static final Move ASCEND_NORTH = new AscendMove(0, 1, -1);
	public static final Move ASCEND_SOUTH = new AscendMove(0, 1, 1);
	public static final Move ASCEND_EAST = new AscendMove(1, 1, 0);
	public static final Move ASCEND_WEST = new AscendMove(-1, 1, 0);
	public static final Move ASCEND_NORTHEAST = new AscendMove(1, 1, -1);
	public static final Move ASCEND_SOUTHEAST = new AscendMove(1, 1, 1);
	public static final Move ASCEND_SOUTHWEST = new AscendMove(-1, 1, 1);
	public static final Move ASCEND_NORTHWEST = new AscendMove(-1, 1, -1);
	public static final Move PILLAR = new PillarMove(0, 1, 0);
	public static final Move DESCEND_NORTH = new DescendMove(0, -1, -1);
	public static final Move DESCEND_SOUTH = new DescendMove(0, -1, 1);
	public static final Move DESCEND_EAST = new DescendMove(1, -1, 0);
	public static final Move DESCEND_WEST = new DescendMove(-1, -1, 0);
	public static final Move DESCEND_NORTHEAST = new DescendMove(1, -1, -1);
	public static final Move DESCEND_SOUTHEAST = new DescendMove(1, -1, 1);
	public static final Move DESCEND_SOUTHWEST = new DescendMove(-1, -1, 1);
	public static final Move DESCEND_NORTHWEST = new DescendMove(-1, -1, -1);
	public static final Move DESCEND_DOWN = new DescendMove(0, -1, 0);
	public static final Move PARKOUR_NORTH = new ParkourMove(0, 0, -1);
	public static final Move PARKOUR_SOUTH = new ParkourMove(0, 0, 1);
	public static final Move PARKOUR_EAST = new ParkourMove(1, 0, 0);
	public static final Move PARKOUR_WEST = new ParkourMove(-1, 0, 0);
	
	private static int pointer;
	
	private final int dx;
	private final int dy;
	private final int dz;
	
	public Move(int dx, int dy, int dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		
		registerMove(this);
	}
	
	public abstract Movement apply(Node n, PathFinder finder);
	
	public int getDeltaX() {
		return dx;
	}
	
	public int getDeltaY() {
		return dy;
	}
	
	public int getDeltaZ() {
		return dz;
	}
	
	private static void registerMove(Move m) {
		MOVES[pointer] = m;
		
		pointer++;
	}
	
	public static Move[] getAllMoves() {
		return MOVES;
	}
	
}
