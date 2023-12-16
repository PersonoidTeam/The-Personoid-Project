package com.personoid.api.pathfindingold;

import com.personoid.api.pathfindingold.goal.Goal;
import com.personoid.api.pathfindingold.movement.Movement;
import org.bukkit.Location;
import org.bukkit.World;

public class Node implements Comparable<Node> {
    private final int x;
    private final int y;
    private final int z;

    private double gCost; // cost from the start node to this node
    private double hCost; // heuristic cost to the target node

    private Node parent;
    private Movement movement;

    public Node(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public Location toLocation(World world) {
    	return new Location(world, x, y, z);
    }

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
    }

    public BlockType getType(World world) {
        return CacheManager.get(world).getBlockType(new BlockPos(x, y, z));
    }

    public double getGCost() {
        return gCost;
    }

    public void setGCost(double gCost) {
        this.gCost = gCost;
    }

    public double getHCost() {
        return hCost;
    }

    public void setHCost(double hCost) {
        this.hCost = hCost;
    }

    public double getFCost() {
        return gCost + hCost;
    }

    public void setParent(Node node, Movement movement, double cost) {
        this.parent = node;
        this.movement = movement;
        this.gCost = node.getGCost() + cost;
    }

    public Node getParent() {
        return this.parent;
    }

    public Movement getMovement() {
    	return movement;
    }

    public void updateHeuristic(Goal goal) {
        hCost = goal.heuristic(this);
    }

    public int squaredDistanceTo(Node node) {
        return squaredDistanceTo(new BlockPos(node.getX(), node.getY(), node.getZ()));
    }

    public int squaredDistanceTo(BlockPos blockPos) {
        int dx = blockPos.getX() - this.getX();
        int dy = blockPos.getY() - this.getY();
        int dz = blockPos.getZ() - this.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.getFCost(), other.getFCost());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node other = (Node) obj;
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
        return false;
    }
}
