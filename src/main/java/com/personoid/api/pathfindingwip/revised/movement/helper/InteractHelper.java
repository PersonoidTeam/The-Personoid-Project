package com.personoid.api.pathfindingwip.revised.movement.helper;

import com.personoid.api.pathfindingwip.revised.calc.Node;
import com.personoid.api.pathfindingwip.revised.movement.Movement;
import de.stylextv.maple.input.target.targets.OpenableTarget;
import de.stylextv.maple.pathing.calc.Cost;
import de.stylextv.maple.world.BlockInterface;
import de.stylextv.maple.world.interact.Openable;
import net.minecraft.block.BlockState;

public class InteractHelper extends TargetHelper<OpenableTarget> {
	
	public InteractHelper(Movement m) {
		super(m);
	}
	
	public void collectDefaultBlocks() {
		Node source = getSource();
		Node destination = getDestination();
		
		collectBlocks(source, 2);
		collectBlocks(destination, 2);
	}
	
	@Override
	public void collectBlock(int x, int y, int z) {
		BlockState state = BlockInterface.getState(x, y, z);
		
		Openable o = Openable.fromState(state);
		
		if(o == null) return;
		
		OpenableTarget target = getTarget(x, y, z);
		
		if(target == null) {
			
			target = new OpenableTarget(x, y, z, o);
			
			addTarget(target);
			
			return;
		}
		
		target.updateOpenable(o);
	}
	
	@Override
	public double cost() {
		Movement m = getMovement();
		
		for(OpenableTarget target : getTargets()) {
			
			boolean locked = target.isLocked();
			
			if(locked && !target.isOpen(m)) return Cost.INFINITY;
		}
		
		return 0;
	}
	
	public boolean onRenderTick() {
		if(!hasTargets()) return false;
		
		Movement m = getMovement();
		
		for(OpenableTarget target : getTargets()) {
			
			if(target.isOpen(m)) {
				
				removeTarget(target);
				
				continue;
			}
			
			if(target.continueOpening()) return true;
		}
		
		return false;
	}
	
}
