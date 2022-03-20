package com.notnotdoddy.personoid.utils;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.components.NPCTickingComponent;
import com.notnotdoddy.personoid.utils.npc.PacketUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import org.bukkit.block.Block;

public class BlockBreaker extends NPCTickingComponent {
    private Block block;

    public BlockBreaker(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        if (block == null) return;
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(MathUtils.random(0, 999), getBlockPos(), getProgress());
        PacketUtils.send(packet);
        if (getProgress() >= 100) {
            block.breakNaturally(/*npc.getInventory().getItemInMainHand()*/);
        }
    }

    public void start(Block block) {
        // TODO: add this check to tick()? - if npc can reach block after method is called then it will start being broken
        if (npc.getLocation().distance(block.getLocation()) > 5) return; // to far away to break
        this.block = block;
    }

    public void stop() {
        this.block = null;
    }

    public Block getCurrentBlock() {
        return block;
    }

    private BlockPos getBlockPos() {
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    private int getProgress() {
        float speed = block.getBreakSpeed(npc.getBukkitEntity());
        return (int) (speed * 20);
    }
}
