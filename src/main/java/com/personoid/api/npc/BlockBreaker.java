package com.personoid.api.npc;

import com.personoid.api.ai.looking.Target;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.packet.Packets;
import com.personoid.api.utils.types.HandEnum;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockBreaker {
    private final NPC npc;
    private Block block;
    private BlockData data;
    private float currentProgress;
    private float hardnessOfBlock;
    private boolean canTick;
    private int tick;

    public BlockBreaker(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (!canTick) return;
        //currentProgress += block.getBreakSpeed(npc.getEntity()) * 5.1F; // TODO: implement
        sendPacket(getProgress());
        //npc.swingHand(HandEnum.RIGHT);
        if (hardnessOfBlock * currentProgress >= hardnessOfBlock) {
            block.breakNaturally(npc.getEntity().getItemInHand());
            playBreakSound();
            stop();
        } else if (tick % 20 == 0) playHitSound();

        //Bukkit.broadcastMessage("Break speed: "+block.getBreakSpeed(npc.getBukkitEntity()));
        //Bukkit.broadcastMessage(hardnessOfBlock*currentProgress+"/"+hardnessOfBlock+"%");
        //Bukkit.broadcastMessage("Mapped value: " + getProgress()+"/9");
    }

    public void start(Block block) {
        this.block = block;
        this.data = block.getBlockData();
        hardnessOfBlock = block.getType().getHardness();
        npc.getLookController().addTarget("block_breaker_block", new Target(block, Priority.HIGHEST));
        tick = 0;
        currentProgress = 0;
        canTick = true;
    }

    private void sendPacket(int stage) {
        Packets.blockDestruction(npc.getEntity().getEntityId(), getLocation(), stage).send();
    }

    private void playHitSound() {
        float volume = (float) MathUtils.random(0.05, 0.25);
        npc.getLocation().getWorld().playSound(block.getLocation(), data.getSoundGroup().getHitSound(), SoundCategory.BLOCKS, volume, 0.5F);
    }

    private void playBreakSound() {
        npc.getLocation().getWorld().playSound(block.getLocation(), data.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 0.75F, 0.5F);
    }

    public void stop() {
        canTick = false;
        npc.getLookController().removeTarget("block_breaker_block");
        if (block != null) sendPacket(10);
    }

    public Block getCurrentBlock() {
        return block;
    }

    private Location getLocation() {
        return block.getLocation();
    }

    private int getProgress() {
        double mappedValue = MathUtils.remap(hardnessOfBlock * currentProgress, hardnessOfBlock, 0, 9);
        return Math.round((long) mappedValue);
    }
}