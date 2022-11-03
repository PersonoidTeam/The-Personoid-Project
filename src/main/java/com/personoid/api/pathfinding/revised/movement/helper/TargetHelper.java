package com.personoid.api.pathfinding.revised.movement.helper;

import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.movement.Movement;
import com.personoid.api.utils.bukkit.BlockPos;
import de.stylextv.maple.event.events.RenderWorldEvent;
import de.stylextv.maple.input.target.BlockTarget;
import de.stylextv.maple.input.target.TargetList;
import de.stylextv.maple.render.ShapeRenderer;
import de.stylextv.maple.scheme.Color;

public abstract class TargetHelper<T extends BlockTarget> extends MovementHelper<Movement> {
	
	private final TargetList<T> targets = new TargetList<>();
	
	public TargetHelper(Movement movement) {
		super(movement);
	}
	
	public void collectBlocks(Node node, int height) {
		collectBlocks(node, 0, height);
	}
	
	public void collectBlocks(Node node, int offset, int height) {
		int x = node.getX();
		int y = node.getY();
		int z = node.getZ();
		collectBlocks(x, y, z, offset, height);
	}
	
	public void collectBlocks(int x, int y, int z, int offset, int height) {
		for (int i = 0; i < height; i++) {
			collectBlock(x, y + offset + i, z);
		}
	}
	
	public void collectBlock(Node node) {
		collectBlock(node, 0);
	}
	
	public void collectBlock(Node node, int offset) {
		int x = node.getX();
		int y = node.getY() + offset;
		int z = node.getZ();
		collectBlock(x, y, z);
	}
	
	public abstract void collectBlock(int x, int y, int z);
	
	public void render(RenderWorldEvent event, Color color) {
		for (BlockTarget target : targets) {
			BlockPos pos = target.getPos();
			ShapeRenderer.drawBox(event, pos, color, 2);
		}
	}
	
	public void addTarget(T target) {
		targets.add(target);
	}
	
	public void removeTarget(T target) {
		targets.remove(target);
	}
	
	public boolean hasTarget(BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return hasTarget(x, y, z);
	}
	
	public boolean hasTarget(int x, int y, int z) {
		return getTarget(x, y, z) != null;
	}
	
	public T getTarget(int x, int y, int z) {
		for (T target : targets) {
			BlockPos pos = target.getPos();
			if (pos.getX() == x && pos.getY() == y && pos.getZ() == z) return target;
		}
		return null;
	}
	
	public boolean hasTargets() {
		return !targets.isEmpty();
	}
	
	public TargetList<T> getTargets() {
		return targets;
	}
	
}
