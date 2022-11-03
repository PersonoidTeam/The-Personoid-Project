package com.personoid.api.pathfinding.revised.movement.helper;

import com.personoid.api.pathfinding.revised.calc.Cost;
import com.personoid.api.pathfinding.revised.calc.Node;
import com.personoid.api.pathfinding.revised.movement.movements.FallMovement;
import de.stylextv.maple.cache.block.BlockType;
import de.stylextv.maple.context.PlayerContext;
import de.stylextv.maple.context.WorldContext;
import de.stylextv.maple.gui.ToolSet;
import de.stylextv.maple.input.InputAction;
import de.stylextv.maple.input.controller.GuiController;
import de.stylextv.maple.input.controller.InputController;
import net.minecraft.world.dimension.DimensionType;
import org.bukkit.inventory.ItemStack;

public class FallHelper extends MovementHelper<FallMovement> {
	
	private boolean placedWaterBucket;
	
	public FallHelper(FallMovement m) {
		super(m);
	}
	
	@Override
	public double cost() {
		if (placedWaterBucket) return 0;
		
		if (hasToMlg()) {
			if (!canPlaceWater()) return Cost.INFINITY;

			ToolSet tools = ToolSet.getTools();
			if (!tools.hasWaterBucket()) return Cost.INFINITY;
		}
		
		return 0;
	}
	
	public boolean hasToMlg() {
		FallMovement movement = getMovement();

		int fallDistance = movement.getFallDistance();
		if (fallDistance < 4) return false;
		
		Node destination = getDestination();
		BlockType type = destination.getType();

		return type != BlockType.WATER;
	}
	
	public void onRenderTick() {
		boolean onGround = PlayerContext.isOnGround();
		if (onGround || !hasToMlg() || !selectWaterBucket()) return;
		
		InputController.setPressed(InputAction.RIGHT_CLICK, true);
		
		placedWaterBucket = true;
	}
	
	private boolean selectWaterBucket() {
		ToolSet tools = ToolSet.getTools();
		if (!tools.hasWaterBucket()) return false;

		ItemStack stack = tools.getWaterBucket();
		GuiController.selectItem(stack);

		return true;
	}
	
	public boolean isFinished() {
		if (!placedWaterBucket) return true;
		
		boolean onGround = PlayerContext.isOnGround();
		boolean inWater = PlayerContext.isInWater();
		if (!inWater) return onGround;
		
		InputController.setPressed(InputAction.RIGHT_CLICK, true);
		
		return false;
	}
	
	private static boolean canPlaceWater() {
		DimensionType dimension = WorldContext.dimension();
		return !dimension.isUltrawarm();
	}
	
}
