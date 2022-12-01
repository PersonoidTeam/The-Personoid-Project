package com.personoid.api.ai.looking;

import com.personoid.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;

public class OpticsManager {
    private final NPC npc;

    public OpticsManager(NPC npc) {
        this.npc = npc;
    }

    public void tick() {

    }

    public boolean isOccluded(Block block) {
        List<Block> blocks = raycast(5);
        if (!blocks.contains(block)) return true;
        return blocks.get(0).equals(block);
    }

    public List<Block> raycast(int range) {
        List<Block> blocks = new ArrayList<>();
        Location eyeLoc = npc.getLocation().add(0, npc.getEntity().getEyeHeight(), 0);
        BlockIterator iterator = new BlockIterator(npc.getWorld(), eyeLoc.toVector(), eyeLoc.getDirection(), 0.0D, range);
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (block.getType().isSolid()) {
                blocks.add(block);
            }
        }
        return blocks;
    }
}
