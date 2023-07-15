package com.personoid.api.npc.blocker.instruction;

import com.personoid.api.npc.NPC;
import com.personoid.api.npc.blocker.BlockProcess;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.HandEnum;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockPlaceInstruction extends Instruction {
    private final Material replacement;

    public BlockPlaceInstruction(NPC npc, Block block, Material replacement) {
        super(npc, block);
        this.replacement = replacement;
    }

    @Override
    public void onStart() {
        if (!replacement.isBlock()) {
            finish(Result.failure("Replacement material is not a block!"));
            return;
        }
        if (!sameWorld()) {
            finish(Result.failure("NPC and block are not in the same world!"));
            return;
        }
        if (!withinRange(5)) {
            finish(Result.failure("NPC too far away from block! Must be within 5 blocks."));
            return;
        }
        getNPC().face(getBlock().getLocation());
    }

    @Override
    public void tick() {
        if (getHand().getType().isAir()) {
            getNPC().swingHand(HandEnum.RIGHT);
            BlockData data = getHand().getType().createBlockData();
            getBlock().setBlockData(data);
            float volume = (float) MathUtils.random(0.05, 0.25);
            getBlock().getWorld().playSound(getBlock().getLocation(), data.getSoundGroup().getPlaceSound(), volume, 0.5F);
        }
    }

    @Override
    public BlockProcess getProcess() {
        return BlockProcess.MINING;
    }
}
