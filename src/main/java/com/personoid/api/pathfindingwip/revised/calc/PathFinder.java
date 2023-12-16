package com.personoid.api.pathfindingwip.revised.calc;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.favoring.Favoring;
import com.personoid.api.pathfindingwip.revised.calc.goal.Goal;
import com.personoid.api.pathfindingwip.revised.calc.openset.HeapOpenSet;
import com.personoid.api.pathfindingwip.revised.movement.Move;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfinding.calc.utils.BlockPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathFinder {
	private static final int MAX_CHUNK_BORDER_HITS = 50;
	private static final float[] PS_COEFFICIENTS = { 1.5F, 2, 2.5F, 3, 4, 5, 10 };
	private static final int PS_MIN_DISTANCE = 25;

	private final NPC npc;
	private final Long2ObjectOpenHashMap<Node> map;
	private final HeapOpenSet openSet;
	private final Set<Node> closedSet;
	private final Node[] partialSolutions;
	private final Goal goal;
	private final Favoring favoring;
	private int chunkBorderHits;
	private Node startNode;
	private Node lastConsideration;
	private boolean stop;
	private boolean pause;
	private boolean failing = true;
	
	public PathFinder(NPC npc, Goal goal, Favoring favoring) {
		this.npc = npc;
		this.goal = goal;
		this.favoring = favoring;
		this.map = new Long2ObjectOpenHashMap<>(1024, 0.75f);
		this.openSet = new HeapOpenSet();
		this.closedSet = new HashSet<>();
		this.partialSolutions = new Node[PS_COEFFICIENTS.length];
	}
	
	public PathSegment find(BlockPos start, long primaryTimeout, long failureTimeout) {
		return find(start.getX(), start.getY(), start.getZ(), primaryTimeout, failureTimeout);
	}
	
	public PathSegment find(int startX, int startY, int startZ, long primaryTimeout, long failureTimeout) {
		long startTime = System.currentTimeMillis();
		startNode = getNode(startX, startY, startZ);
		openSet.add(startNode);
		
		while (!openSet.isEmpty()) {
			if (stop) return null;
			Node node = openSet.poll();
			lastConsideration = node;
			closedSet.add(node);
			updatePartialSolutions(node);
			if (goal.isFinalNode(node)) return backtrace(node);
			addAdjacentNodes(node);
			long now = System.currentTimeMillis();
			long elapsedTime = now - startTime;
			if (chunkBorderHits > MAX_CHUNK_BORDER_HITS) break;
			boolean ranOutOfTime = elapsedTime > failureTimeout || (!failing && elapsedTime > primaryTimeout);
			if (ranOutOfTime) break;
		}

		pause = true;
		return bestSoFar();
	}
	
	public void stop() {
		stop = true;
	}
	
	public PathSegment bestSoFar() {
		if (startNode == null) return null;

		for (Node node : partialSolutions) {
			if (node == null) continue;
			int dis = startNode.squaredDistanceTo(node);
			boolean b = dis > PS_MIN_DISTANCE;
			if (b) return backtrace(node);
		}

		return null;
	}
	
	public PathSegment lastConsideredPath() {
		if (lastConsideration == null) return null;
		return backtrace(lastConsideration);
	}
	
	private PathSegment backtrace(Node node) {
		List<Movement> list = new ArrayList<>();

		while (true) {
			Node parent = node.getParent();
			if (parent == null) break;
			list.add(0, node.getMovement());
			node = parent;
		}

		return new PathSegment(list);
	}
	
	private void updatePartialSolutions(Node node) {
		for (int i = 0; i < PS_COEFFICIENTS.length; i++) {
			Node closest = partialSolutions[i];
			boolean closer = true;

			if (closest != null) {
				float f = PS_COEFFICIENTS[i];
				closer = node.getPartialCost(f) < closest.getPartialCost(f);
			}
			
			if (closer) {
				partialSolutions[i] = node;
				int dis = startNode.squaredDistanceTo(node);
				if(dis > PS_MIN_DISTANCE) failing = false;
			}
		}
	}
	
	private void addAdjacentNodes(Node node) {
		for (Move move : Move.getAllMoves()) {
			Movement movement = move.apply(npc, node, this);
			if (movement != null) addAdjacentNode(node, movement);
		}
	}
	
	private void addAdjacentNode(Node parent, Movement movement) {
		Node node = movement.getDestination();
		Material type = node.getType();

		if (type.isUnloaded() || closedSet.contains(node)) return;
		
		double cost = movement.favoredCost(favoring);
		if (cost >= Cost.INFINITY) return;

		if (node.isOpen()) {
			if (node.updateParent(parent, movement, cost)) {
				openSet.update(node);
			}
		} else {
			node.setParent(parent, movement, cost);
			openSet.add(node);
		}
	}
	
	public Node getAdjacentNode(Node node, int dx, int dy, int dz) {
		int x = node.getX() + dx;
		int y = node.getY() + dy;
		int z = node.getZ() + dz;
		return getNode(x, y, z);
	}
	
	private Node getNode(int x, int y, int z) {
		long hash = Node.posAsLong(x, y, z);
		Node node = map.get(hash);
		if (node != null) return node;
		node = new Node(x, y, z);
		node.updateHeuristic(goal);
		map.put(hash, node);
		if (node.getType().isUnloaded()) chunkBorderHits++;
		return node;
	}
	
	public Goal getGoal() {
		return goal;
	}
	
	public Favoring getFavoring() {
		return favoring;
	}
	
	public boolean wasStopped() {
		return stop;
	}
	
	public boolean wasPaused() {
		return pause;
	}
	
	public boolean isFailing() {
		return failing;
	}
	
}
