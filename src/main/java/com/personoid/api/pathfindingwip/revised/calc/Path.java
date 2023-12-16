package com.personoid.api.pathfindingwip.revised.calc;

import com.personoid.api.pathfindingwip.revised.movement.Movement;
import com.personoid.api.pathfindingwip.revised.utils.TimeUtil;
import com.personoid.api.pathfinding.calc.utils.BlockPos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Path {
	private static final int OBSTACLE_LOOKAHEAD = 6;
	private final List<PathSegment> segments = new CopyOnWriteArrayList<>();
	
	private PathSegment segment;
	private PathState state = PathState.PAUSED;
	
	public void add(PathSegment segment) {
		Node node = segment.findMatch(this);
		if (node != null) {
			segment.trimToNode(node);
			trimFromNode(node);
		}
		if (!segment.isEmpty()) segments.add(segment);
	}
	
	public void next() {
		PathSegment segment = getSegment();
		segment.next();
	}
	
	private void nextSegment() {
		if (segment != null) segments.remove(0);
		if (isEmpty()) {
			segment = null;
			return;
		}
		segment = segments.get(0);
	}
	
	public void clear() {
		segments.clear();
		segment = null;
		state = PathState.PAUSED;
	}
	
	// TODO if path is empty give estimate based on goal
	public long timeToGoal() {
		if (isEmpty()) return 0;
		long time = timeLeft();
		if (!isFinished()) {
			Node node = lastNode();
			time += TimeUtil.ticksToMS(node.getHCost());
		}
		return time;
	}
	
	public long timeLeft() {
		return TimeUtil.ticksToMS(ticksLeft());
	}
	
	public double ticksLeft() {
		return segments.stream().mapToDouble(PathSegment::ticksLeft).sum();
	}
	
	public int nodesLeft() {
		return segments.stream().mapToInt(PathSegment::nodesLeft).sum();
	}
	
	public Node lastNode() {
		return lastSegment().lastNode();
	}
	
	public PathSegment lastSegment() {
		if (length() == 0) return null;
		return segments.get(length() - 1);
	}
	
	public double distanceSqr(BlockPos pos) {
		Movement movement = getCurrentMovement();
		if (movement == null) return 0;
		return movement.squaredDistanceTo(pos);
	}
	
	public void trimFromNode(Node node) {
		int index = indexOf(node);
		int amount = lengthInNodes() - 1 - index;
		trim(amount);
	}
	
	public void trim(int amount) {
		while (!isEmpty() && amount > 0) {
			PathSegment segment = lastSegment();
			amount -= segment.trim(amount);
			if (!segment.isEmpty()) return;
			segments.remove(segment);
		}
	}
	
	public int indexOf(Node n) {
		int i = 0;
		for (PathSegment segment : segments) {
			int index = segment.indexOf(n);
			if (index != -1) return i + index;
			i += segment.length();
		}
		return -1;
	}
	
	public Movement getNextMovement(int offset) {
		for (PathSegment segment : segments) {
			int nodesLeft = segment.nodesLeft();
			if (offset >= nodesLeft) {
				offset -= nodesLeft;
				continue;
			}
			int i = segment.getPointer();
			return segment.getMovement(offset + i);
		}
		return null;
	}
	
	public int getCurrentIndex() {
		Node node = getCurrentNode();
		if (node == null) return -1;
		return indexOf(node);
	}
	
	public Node getCurrentNode() {
		PathSegment segment = getSegment();
		if (segment == null) return null;
		return segment.getCurrentNode();
	}
	
	public Movement getCurrentMovement() {
		PathSegment segment = getSegment();
		if (segment == null) return null;
		return segment.getCurrentMovement();
	}
	
	public PathSegment getSegment() {
		if (segment == null || segment.isFinished()) nextSegment();
		return segment;
	}
	
	public int lengthInNodes() {
		return segments.stream().mapToInt(PathSegment::length).sum();
	}
	
	public int length() {
		return segments.size();
	}
	
	public boolean isImpossible() {
		for (int i = 0; i < OBSTACLE_LOOKAHEAD; i++) {
			Movement movement = getNextMovement(i);
			if (movement == null) break;
			if (movement.isImpossible()) return true;
		}
		return false;
	}
	
	public boolean isFinished() {
		return state != PathState.PAUSED;
	}
	
	public boolean isEmpty() {
		return segments.isEmpty();
	}
	
	public List<PathSegment> getAllSegments() {
		return segments;
	}
	
	public PathState getState() {
		return state;
	}
	
	public void setState(PathState state) {
		this.state = state;
	}
	
}
