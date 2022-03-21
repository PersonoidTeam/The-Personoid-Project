package com.personoid.utils;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.npc.PacketUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.InteractionHand;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class BlockBreaker extends NPCTickingComponent {
    private Block block;
    private int stage;
    private float currentProgress = 0;
    private float hardnessOfBlock = 0;

    public BlockBreaker(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        if (block == null) return;
        super.tick();
        Bukkit.broadcastMessage("Break speed: "+block.getBreakSpeed(npc.getBukkitEntity()));
        currentProgress += block.getBreakSpeed(npc.getBukkitEntity())*5F;
        Bukkit.broadcastMessage(hardnessOfBlock*currentProgress+"/"+hardnessOfBlock+"%");
        Bukkit.broadcastMessage("Mapped value: " + getProgress()+"/9");

        PacketUtils.send(new ClientboundBlockDestructionPacket(npc.getBukkitEntity().getEntityId(), getBlockPos(), getProgress()));
        npc.swing(InteractionHand.MAIN_HAND);
        npc.getLookController().face(block.getLocation());
        if (hardnessOfBlock*currentProgress >= hardnessOfBlock) {
            block.breakNaturally(/*npc.getInventory().getItemInMainHand()*/);
            stop();
        }
    }

    public void start(Block block) {
        // TODO: add this check to tick()? - if npc can reach block after method is called then it will start being broken
        if (npc.getLocation().distance(block.getLocation()) > 5) return; // to far away to break
        this.block = block;
        hardnessOfBlock = block.getType().getHardness();
        currentTick = 0;
    }

    public void stop() {
        PacketUtils.send(new ClientboundBlockDestructionPacket(npc.getBukkitEntity().getEntityId(), getBlockPos(), 10));
        this.block = null;
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
