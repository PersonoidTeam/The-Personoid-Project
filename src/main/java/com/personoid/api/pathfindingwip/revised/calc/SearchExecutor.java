package com.personoid.api.pathfindingwip.revised.calc;

import com.personoid.PersonoidPlugin;
import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingwip.revised.calc.favoring.Favoring;
import com.personoid.api.pathfindingwip.revised.calc.goal.Goal;
import com.personoid.api.pathfindingwip.revised.movement.MovementExecutor;
import com.personoid.api.pathfinding.calc.utils.BlockPos;
import com.personoid.api.utils.bukkit.Task;

public class SearchExecutor {
	private static final long INITIAL_TIMEOUT = 500;
	private static final long INITIAL_FAILURE_TIMEOUT = 2000;
	private static final long PLAN_AHEAD_TIMEOUT = 4000;
	private static final long PLAN_AHEAD_FAILURE_TIMEOUT = 5000;
	private static final long CALCULATION_TIME_BUFFER = 3000;
	private static final long SLEEP_TIME = 100;

	private PathFinder finder;
	private final NPC npc;
	private final MovementExecutor executor;

	public SearchExecutor(NPC npc, MovementExecutor executor) {
		this.npc = npc;
		this.executor = executor;
	}
	
	public void startSearch(Goal goal) {
		stopSearch();
		Path path = new Path();
		executor.followPath(path);

		new Task(() -> {
			while (true) {
				boolean required = path.equals(executor.getPath());
				if (!required) break;

				boolean initial = path.isEmpty();
				long primaryTimeout = initial ? INITIAL_TIMEOUT : PLAN_AHEAD_TIMEOUT;
				long failureTimeout = initial ? INITIAL_FAILURE_TIMEOUT : PLAN_AHEAD_FAILURE_TIMEOUT;
				long requiredTime = primaryTimeout + CALCULATION_TIME_BUFFER;
				boolean sleep = path.isFinished() || path.timeLeft() > requiredTime;

				if (sleep) {
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (InterruptedException ignored) {}
					continue;
				}

				Favoring favoring = new Favoring(npc.getWorld(), path.lastSegment());
				finder = new PathFinder(npc, goal, favoring);
				BlockPos start = executor.getDestination();
				PathSegment segment = finder.find(start, primaryTimeout, failureTimeout);
				boolean paused = finder.wasPaused();
				finder = null;

				if (segment == null) {
					path.setState(PathState.FAILED);
					continue;
				}

				boolean empty = segment.isEmpty();
				if (!empty) path.add(segment);
				if (!paused) path.setState(PathState.FOUND_GOAL);
			}
		}, PersonoidPlugin.getPlugin()).async().run();
	}
	
	public void stopSearch() {
		if (!isInSearch()) return;
		finder.stop();
	}
	
	public PathSegment getLastConsideration() {
		if (!isInSearch()) return null;
		return finder.lastConsideredPath();
	}
	
	public PathSegment getBestSoFar() {
		if (!isInSearch()) return null;
		return finder.bestSoFar();
	}
	
	public boolean isInSearch() {
		return finder != null;
	}
	
}
