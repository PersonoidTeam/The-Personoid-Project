package com.notnotdoddy.personoid.npc.ai.pathfinding.requirements;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class WalkablePathRequirement extends PathRequirement {
    @Override
    public boolean canPathTo(Block from, Block to) {
        boolean validHeight = !to.getType().isSolid() && !to.getRelative(BlockFace.UP).getType().isSolid(); // checks if is player height
        boolean validGround = to.getRelative(BlockFace.DOWN).getType().isSolid(); // is there a block underneath that they can stand on?
        boolean validFromPrev = to.getLocation().subtract(from.getLocation()).getY() <= 1; // is it max one block higher than the last one?
        return validHeight && validGround && validFromPrev;
    }
}
