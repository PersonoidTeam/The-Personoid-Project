package com.personoid.npc.ai.pathfinding.requirements;

import com.personoid.utils.LocationUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class WalkablePathRequirement extends PathRequirement {
    @Override
    public boolean canPathTo(Block from, Block to) {
        boolean validHeight = !LocationUtils.isSolid(to) && !LocationUtils.isSolid(to.getRelative(BlockFace.UP)); // checks if is player height
        boolean validGround = LocationUtils.isSolid(to.getRelative(BlockFace.DOWN)); // is there a block underneath that they can stand on?
        boolean validFromPrev = to.getLocation().subtract(from.getLocation()).getY() <= 1; // is it max one block higher than the last one?
        boolean notSameXY = to.getX() != from.getX() || to.getZ() != from.getZ(); // is it not the same x or z?
        return validHeight && validGround && validFromPrev && notSameXY;
    }
}
