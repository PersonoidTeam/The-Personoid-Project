package com.personoid.api.pathfindingold.goal;

import com.personoid.api.pathfindingold.Node;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public class CompositeGoal extends Goal {
	private final Goal[] goals;
	
	public CompositeGoal(Goal... goals) {
		this.goals = goals;
	}
	
	@Override
	public double heuristic(Node node) {
		double cost = Double.MAX_VALUE;
		for (Goal goal : goals) {
			double heuristic = goal.heuristic(node);
			if (heuristic < cost) cost = heuristic;
		}
		return cost;
	}
	
	@Override
	public boolean isFinalNode(Node node) {
		for (Goal goal : goals) {
			if (goal.isFinalNode(node)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmpty() {
		return goals.length == 0;
	}
	
	@Override
	public boolean equals(Goal other) {
		if (!(other instanceof CompositeGoal)) return false;
		
		Goal[] otherGoals = ((CompositeGoal) other).getGoals();

		int length = otherGoals.length;
		if (length != goals.length) return false;
		
		for (int i = 0; i < length; i++) {
			Goal otherGoal = otherGoals[i];
			Goal goal = goals[i];
			
			if (!otherGoal.equals(goal)) return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		String s = Arrays.toString(goals);
		return String.format("CompositeGoal{goals=%s}", s);
	}
	
	public Goal[] getGoals() {
		return goals;
	}
	
	public static CompositeGoal combine(CompositeGoal goal1, CompositeGoal goal2) {
		Goal[] goals1 = goal1.getGoals();
		Goal[] goals2 = goal2.getGoals();
		
		Stream<Goal> stream1 = Arrays.stream(goals1);
		Stream<Goal> stream2 = Arrays.stream(goals2);
		
		Goal[] goals = Stream.concat(stream1, stream2).toArray(Goal[]::new);
		
		return new CompositeGoal(goals);
	}
	
	public static <T> CompositeGoal fromCollection(Collection<T> collection, Function<T, Goal> function) {
		int size = collection.size();
		Goal[] goals = new Goal[size];
		
		int i = 0;
		for (T t : collection) {
			Goal g = function.apply(t);
			goals[i] = g;
			i++;
		}
		
		return new CompositeGoal(goals);
	}
}
