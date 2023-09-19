package com.personoid.api.npc.blocker.instruction;

import com.personoid.api.npc.NPC;
import com.personoid.api.npc.blocker.BlockProcess;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.bukkit.Logger;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.HandEnum;
import com.personoid.nms.packet.Packets;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class BlockBreakInstruction extends Instruction {
    private final boolean collectDrops;
    private BlockData data;
    private float progress;
    private float hardness;
    private int tick;

    public BlockBreakInstruction(NPC npc, Block block, boolean collectDrops) {
        super(npc, block);
        this.collectDrops = collectDrops;
    }

    @Override
    public void onStart() {
        Logger.get().severe("BlockBreakInstruction.onStart() called!");
        if (collectDrops) {
            Logger.get().title("Blocker").warning("Collecting drops is not yet implemented!");
        }
        if (!sameWorld()) {
            finish(Result.failure("NPC and block are not in the same world!"));
            return;
        }
/*        if (withinRange(5)) {
            finish(Result.failure("NPC too far away from block! Must be within 5 blocks."));
            return;
        }*/
        data = getBlock().getBlockData();
        hardness = getBlock().getType().getHardness();
        getNPC().face(getBlock().getLocation());
    }

    @Override
    public void tick() {
        progress += getBlock().getBreakSpeed(getNPC().getEntity()) * 5.1F; // WHY THIS VALUE?
        Packets.blockDestruction(getNPC().getEntityId(), getBlock().getLocation(), getStage()).send();
        getNPC().swingHand(HandEnum.RIGHT);
        Logger.get().severe("progress: " + progress + ", hardness: " + hardness);
        if (hardness * progress >= hardness) {
            ItemStack hand = getNPC().getEntity().getInventory().getItemInMainHand();
            getBlock().breakNaturally(hand);
            playBreakSound();
            Packets.blockDestruction(getNPC().getEntityId(), getBlock().getLocation(), 0).send();
            finish(Result.success());
        } else if (++tick % 20 == 0) {
            playHitSound();
            playHitParticle();
        }
    }

    private int getStage() {
        double mappedValue = MathUtils.remap(hardness * progress, hardness, 0, 9);
        return Math.round((long) mappedValue);
    }

    private void playHitSound() {
        float volume = (float) MathUtils.random(0.05, 0.25);
        getBlock().getWorld().playSound(getBlock().getLocation(), data.getSoundGroup().getHitSound(), volume, 0.5F);
    }

    private void playHitParticle() {
        getBlock().getWorld().spawnParticle(Particle.BLOCK_CRACK, getBlock().getLocation().add(0.5, 0.5, 0.5),
                1, 1, 0.1, 0.1, 0.1, getBlock().getBlockData());
    }

    private void playBreakSound() {
        float volume = (float) MathUtils.random(0.05, 0.25);
        getBlock().getWorld().playSound(getBlock().getLocation(), data.getSoundGroup().getBreakSound(), volume, 0.5F);
    }

    @Override
    public BlockProcess getProcess() {
        return BlockProcess.MINING;
    }
}
