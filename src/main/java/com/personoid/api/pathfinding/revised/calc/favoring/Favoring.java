package com.personoid.api.pathfinding.revised.calc.favoring;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.calc.PathSegment;
import de.stylextv.maple.util.world.CoordUtil;
import de.stylextv.maple.world.avoidance.Avoidance;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

public class Favoring {
	private static final double BACKTRACKING_COEFFICIENT = 0.5;
	private static final long UPDATE_DELAY = 2000;

	private static Favoring defaultFavoring;
	private static long lastUpdate;

	private final Long2DoubleOpenHashMap map;
	
	public Favoring(PathSegment segment) {
		this();
		if (segment != null) applyBacktracking(segment);
	}
	
	public Favoring() {
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
		for (Avoidance a : Avoidance.list()) {
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
	
	public static Favoring getDefault() {
		long time = System.currentTimeMillis();
		long dt = time - lastUpdate;
		if (dt > UPDATE_DELAY) {
			defaultFavoring = new Favoring();
			lastUpdate = time;
		}
		return defaultFavoring;
	}
	
}
