package com.personoid.api.npc;

import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.packet.Packets;
import com.personoid.api.utils.types.HandEnum;
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
    private Location lastLookLoc;

    public BlockBreaker(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (!canTick) return;
        currentProgress += block.getBreakSpeed(npc.getEntity()) * 5.1F;
        sendPacket(getProgress());
        npc.swingHand(HandEnum.RIGHT);
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
        lastLookLoc = npc.getLookController().getFacing();
        npc.getLookController().face(block.getLocation().clone().add(0.5, 0.5, 0.5));
        tick = 0;
        currentProgress = 0;
        canTick = true;
    }

    private void sendPacket(int stage) {
        Packets.blockDestruction(npc.getEntity().getEntityId(), getLocation(), stage).send();
    }

    private void playHitSound() {
        float volume = (float) MathUtils.random(0.05, 0.25);
        npc.getEntity().getWorld().playSound(block.getLocation(), data.getSoundGroup().getHitSound(), SoundCategory.BLOCKS, volume, 0.5F);
    }

    private void playBreakSound() {
        npc.getEntity().getWorld().playSound(block.getLocation(), data.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 0.75F, 0.5F);
    }

    public void stop() {
        canTick = false;
        npc.getLookController().face(lastLookLoc);
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