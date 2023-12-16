package com.personoid.api.pathfindingwip.revised.calc;

import com.personoid.api.pathfindingwip.revised.movement.Movement;

import java.util.List;

public class PathSegment {
	
	private final List<Movement> list;
	private int pointer;
	
	public PathSegment(List<Movement> list) {
		this.list = list;
	}
	
	public void next() {
		pointer++;
	}
	
	public double ticksLeft() {
		double sum = 0;
		for (int i = pointer; i < list.size(); i++) {
			Movement m = list.get(i);
			sum += m.favoredCost();
		}
		return sum;
	}
	
	public int nodesLeft() {
		return length() - pointer;
	}
	
	public void trimToNode(Node n) {
		while (!isEmpty()) {
			Movement movement = getMovement(0);
			Node source = movement.getSource();
			if (source.equals(n)) return;
			list.remove(0);
		}
	}
	
	public int trim(int amount) {
		if (amount >= length()) {
			list.clear();
			return length();
		}
		for (int i = 0; i < amount; i++) {
			list.remove(length() - 1);
		}
		return amount;
	}
	
	public Node findMatch(Path path) {
		int index = path.getCurrentIndex();
		int start = length() - 1;
		
		for (int i = start; i >= 0; i--) {
			Node node = getNode(i);
			int j = path.indexOf(node);
			if (j != -1 && j > index) return node;
		}
		
		return null;
	}
	
	public boolean contains(Node n) {
		return indexOf(n) != -1;
	}
	
	public int indexOf(Node n) {
		for (int i = 0; i < length(); i++) {
			Node other = getNode(i);
			if (other.equals(n)) return i;
		}
		return -1;
	}
	
	public Node lastNode() {
		return getNode(length() - 1);
	}
	
	public Node getCurrentNode() {
		if (isFinished()) return null;
		return getNode(pointer);
	}
	
	public Node getNode(int index) {
		Movement movement = getMovement(index);
		return movement.getDestination();
	}
	
	public Movement getCurrentMovement() {
		if (isFinished()) return null;
		return getMovement(pointer);
	}
	
	public Movement getMovement(int index) {
		return list.get(index);
	}
	
	public boolean isFinished() {
		return pointer >= length();
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	public int length() {
		return list.size();
	}
	
	public int getPointer() {
		return pointer;
	}
	
}
