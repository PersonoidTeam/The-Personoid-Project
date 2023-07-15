package com.personoid.api.npc.blocker.instruction;

import com.personoid.api.npc.NPC;
import com.personoid.api.npc.blocker.BlockProcess;
import com.personoid.api.utils.Result;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public abstract class Instruction {
    private final NPC npc;
    private final Block block;

    public Instruction(NPC npc, Block block) {
        this.npc = npc;
        this.block = block;
    }

    public abstract void onStart();

    public abstract void tick();

    public abstract BlockProcess getProcess();

    public void finish(Result<?> result) {

    }

    protected NPC getNPC() {
        return npc;
    }

    public Block getBlock() {
        return block;
    }

    public void stop() {
        finish(Result.failure("Stopped"));
    }

    @Override
    public String toString() {
        return String.format("Instruction{process=%s, block=%s}", getProcess(), block);
    }

    protected boolean withinRange(double distance) {
        return block.getLocation().distanceSquared(npc.getEntity().getLocation()) <= distance * distance;
    }

    protected boolean sameWorld() {
        return block.getWorld().equals(npc.getEntity().getWorld());
    }

    protected ItemStack getHand() {
        return npc.getEntity().getInventory().getItemInMainHand();
    }
}
