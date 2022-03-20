package com.personoid.npc.ai.pathfinding.requirements;

import org.bukkit.block.Block;

public abstract class PathRequirement {
    public abstract boolean canPathTo(Block from, Block to);
}
