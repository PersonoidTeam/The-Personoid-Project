package com.personoid.utils;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.InteractionHand;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockBreaker extends NPCTickingComponent {
    private Block block;
    private BlockData data;
    private float currentProgress;
    private float hardnessOfBlock;
    private boolean canTick;

    public BlockBreaker(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        if (!canTick) return;
        super.tick();
        currentProgress += block.getBreakSpeed(npc.getBukkitEntity())*5.2F;
        sendPacket(getProgress());
        npc.swing(InteractionHand.MAIN_HAND);
        if (hardnessOfBlock*currentProgress >= hardnessOfBlock) {
            block.breakNaturally(/*npc.getInventory().getItemInMainHand()*/);
            playBreakSound();
            stop();
        } else if (currentTick % 4 == 0) playHitSound();

        //Bukkit.broadcastMessage("Break speed: "+block.getBreakSpeed(npc.getBukkitEntity()));
        //Bukkit.broadcastMessage(hardnessOfBlock*currentProgress+"/"+hardnessOfBlock+"%");
        //Bukkit.broadcastMessage("Mapped value: " + getProgress()+"/9");
    }

    public void start(Block block) {
        this.block = block;
        this.data = block.getBlockData();
        hardnessOfBlock = block.getType().getHardness();
        currentTick = 0;
        currentProgress = 0;
        canTick = true;
    }

    private void sendPacket(int stage) {
        PacketUtils.send(new ClientboundBlockDestructionPacket(npc.getBukkitEntity().getEntityId(), getBlockPos(), stage));
    }

    private void playHitSound() {
        npc.getBukkitEntity().getWorld().playSound(block.getLocation(), data.getSoundGroup().getHitSound(), 0.25F, 0.5F);
    }

    private void playBreakSound() {
        npc.getBukkitEntity().getWorld().playSound(block.getLocation(), data.getSoundGroup().getBreakSound(), 0.75F, 0.5F);
    }

    public void stop() {
        canTick = false;
        if (block != null) sendPacket(10);
    }

    public Block getCurrentBlock() {
        return block;
    }

    private BlockPos getBlockPos() {
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    private int getProgress(){
        double mappedValue = MathUtils.map(hardnessOfBlock * currentProgress, hardnessOfBlock, 0, 9);
        return Math.round((long) mappedValue);
    }
}
