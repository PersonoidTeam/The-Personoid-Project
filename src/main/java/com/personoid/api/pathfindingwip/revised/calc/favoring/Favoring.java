package com.personoid.api.pathfindingwip.revised.calc.favoring;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.calc.PathSegment;
import com.personoid.api.pathfindingwip.revised.utils.CoordUtil;
import com.personoid.api.pathfinding.calc.avoidance.Avoidance;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.bukkit.World;

public class Favoring {
	private static final double BACKTRACKING_COEFFICIENT = 0.5;
	private static final long UPDATE_DELAY = 2000;

	private static Favoring defaultFavoring;
	private static long lastUpdate;

	private final Long2DoubleOpenHashMap map;
	private final World world;
	
	public Favoring(World world, PathSegment segment) {
		this(world);
		if (segment != null) applyBacktracking(segment);
	}
	
	public Favoring(World world) {
		this.world = world;
		map = new Long2DoubleOpenHashMap(512);
		map.defaultReturnValue(1);

		applyAvoidance();
	}
	
	public void applyBacktracking(PathSegment segment) {
		for (int i = 0; i < segment.length(); i++) {
			Node n = segment.getNode(i);
			setCoefficient(n, BACKTRACKING_COEFFICIENT);
		}
	}
	
	public void applyAvoidance() {
		for (Avoidance a : Avoidance.list(world)) {
			a.apply(map);
		}
	}
	
	public double getCoefficient(Node node) {
		int x = node.getX();
		int y = node.getY();
		int z = node.getZ();
		return getCoefficient(x, y, z);
	}
	
	public double getCoefficient(int x, int y, int z) {
		long hash = CoordUtil.posAsLong(x, y, z);
		return map.get(hash);
	}
	
	public void setCoefficient(Node node, double d) {
		int x = node.getX();
		int y = node.getY();
		int z = node.getZ();
		setCoefficient(x, y, z, d);
	}
	
	public void setCoefficient(int x, int y, int z, double d) {
		long hash = CoordUtil.posAsLong(x, y, z);
		map.put(hash, d);
	}
	
	public static Favoring getDefault(World world) {
		long time = System.currentTimeMillis();
		long dt = time - lastUpdate;
		if (dt > UPDATE_DELAY) {
			defaultFavoring = new Favoring(world);
			lastUpdate = time;
		}
		return defaultFavoring;
	}
	
}
