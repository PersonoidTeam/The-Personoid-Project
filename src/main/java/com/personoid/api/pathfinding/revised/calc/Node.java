package com.personoid.api.pathfinding.revised.calc;

import com.personoid.api.pathfinding.revised.calc.goal.Goal;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.utils.bukkit.BlockPos;
import de.stylextv.maple.cache.CacheManager;
import de.stylextv.maple.cache.block.BlockType;
import de.stylextv.maple.util.world.CoordUtil;
import org.bukkit.Material;

public class Node {

	private static final double MIN_COST_IMPROVEMENT = 0.01;
	
	private final int x;
	private final int y;
	private final int z;
	
	private Material type;
	private Node parent;
	private Movement movement;
	
	private double gCost;
	private double hCost;
	private double fCost;
	
	private int heapPosition = -1;
	
	public Node(BlockPos pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public Node(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		updateBlockType();
	}
	
	public void updateBlockType() {
		type = CacheManager.getBlockType(x, y, z);
	}
	
	public void updateHeuristic(Goal goal) {
		hCost = goal.heuristic(this);
		updateFinalCost();
	}
	
	public void updateFinalCost() {
		fCost = gCost + hCost;
	}
	
	public boolean updateParent(Node node, Movement movement, double cost) {
		double d = node.getGCost() + cost;
		double improvement = gCost - d;
		if (improvement > MIN_COST_IMPROVEMENT) {
			setParent(node, movement, cost);
			return true;
		}
		return false;
	}
	
	public void setParent(Node node, Movement movement, double cost) {
		parent = node;
		this.movement = movement;
		gCost = node.getGCost() + cost;
		updateFinalCost();
	}
	
	public double getPartialCost(float coefficient) {
		return hCost + gCost / coefficient;
	}
	
	public BlockPos blockPos() {
		return new BlockPos(x, y, z);
	}
	
	public int squaredDistanceTo(BlockPos pos) {
		return squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public int squaredDistanceTo(Node n) {
		return squaredDistanceTo(n.getX(), n.getY(), n.getZ());
	}
	
	public int squaredDistanceTo(int x, int y, int z) {
		int dx = x - this.x;
		int dy = y - this.y;
		int dz = z - this.z;
		return dx * dx + dy * dy + dz * dz;
	}
	
	public boolean isOpen() {
		return heapPosition != -1;
	}
	
	public long getHash() {
		return posAsLong(x, y, z);
	}
	
	public boolean equals(BlockPos pos) {
		return equals(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public boolean equals(Node n) {
		return equals(n.getX(), n.getY(), n.getZ());
	}
	
	public boolean equals(int x, int y, int z) {
		return this.x == x && this.y == y && this.z == z;
	}
	
	@Override
	public String toString() {
		return String.format("Node{x=%s, y=%s, z=%s, type=%s}", x, y, z, type);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public Movement getMovement() {
		return movement;
	}
	
	public Material getType() {
		return type;
	}
	
	public double getGCost() {
		return gCost;
	}
	
	public double getHCost() {
		return hCost;
	}
	
	public double getFinalCost() {
		return fCost;
	}
	
	public int getHeapPosition() {
		return heapPosition;
	}
	
	public void setHeapPosition(int heapPosition) {
		this.heapPosition = heapPosition;
	}
	
	public static long posAsLong(int x, int y, int z) {
		return CoordUtil.posAsLong(x, y, z);
	}

}
