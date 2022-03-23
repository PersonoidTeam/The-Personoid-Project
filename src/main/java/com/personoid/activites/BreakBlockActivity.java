package com.personoid.activites;

import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import org.bukkit.block.Block;

public class BreakBlockActivity extends Activity {
    private final Block block;

    public BreakBlockActivity(Block block) {
        super(ActivityType.INTERACTION);
        this.block = block;
    }

    @Override
    public void onStart(StartType startType) {
        getActiveNPC().getLookController().face(block.getLocation());
        getActiveNPC().getBlockBreaker().start(block);
    }

    @Override
    public void onUpdate() {
        if (getActiveNPC().getLocation().distance(block.getLocation()) > 5) {
            markAsFinished(new Result<>(Result.Type.FAILURE));
        }
    }

    @Override
    public void onStop(StopType stopType) {
        getActiveNPC().getLookController().forget();
        getActiveNPC().getBlockBreaker().stop();
    }

    @Override
    public boolean canStart(StartType startType) {
        return true;
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }
}
