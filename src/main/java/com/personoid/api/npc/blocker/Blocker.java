package com.personoid.api.npc.blocker;

import com.personoid.api.npc.NPC;
import com.personoid.api.npc.blocker.instruction.BlockBreakInstruction;
import com.personoid.api.npc.blocker.instruction.BlockPlaceInstruction;
import com.personoid.api.npc.blocker.instruction.Instruction;
import com.personoid.api.utils.Result;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class Blocker {
    private final NPC npc;
    private BlockProcess process = BlockProcess.IDLE;
    private final Queue<Instruction> queue = new LinkedList<>();
    private Instruction currentInstruction;

    public Blocker(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (currentInstruction != null) {
            currentInstruction.tick();
        } else {
            nextInstruction();
        }
    }

    private void nextInstruction() {
        currentInstruction = queue.poll();
        if (currentInstruction == null) {
            process = BlockProcess.IDLE;
            return;
        }
        process = currentInstruction.getProcess();
        currentInstruction.onStart();
    }

    public void mine(Block block, boolean collectDrops) {
        queue.add(new BlockBreakInstruction(npc, block, collectDrops) {
            @Override
            public void finish(Result<?> result) {
                super.finish(result);
                nextInstruction();
            }
        });
    }

    public void mine(Block block, boolean collectDrops, Consumer<Result<?>> onFinish) {
        queue.add(new BlockBreakInstruction(npc, block, collectDrops) {
            @Override
            public void finish(Result<?> result) {
                super.finish(result);
                onFinish.accept(result);
                nextInstruction();
            }
        });
    }

    public void place(Block block, Material replacement) {
        queue.add(new BlockPlaceInstruction(npc, block, replacement) {
            @Override
            public void finish(Result<?> result) {
                super.finish(result);
                nextInstruction();
            }
        });
    }

    public void stop() {
        if (currentInstruction != null) {
            currentInstruction.stop();
        }
        queue.clear();
        process = BlockProcess.IDLE;
    }

    public BlockProcess getProcess() {
        return process;
    }
}
