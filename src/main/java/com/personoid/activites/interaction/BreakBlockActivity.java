package com.personoid.activites.interaction;

import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import com.personoid.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BreakBlockActivity extends Activity {
    // I love breaking blocks

    private final Block block;
    private final Material originalMaterial;

    public BreakBlockActivity(Block block) {
        super(ActivityType.INTERACTION);
        this.block = block;
        originalMaterial = block.getType();
    }

    @Override
    public void onStart(StartType startType) {
        getActiveNPC().getLookController().face(block.getLocation());
        getActiveNPC().getBlockBreaker().start(block);
    }

    @Override
    public void onUpdate() {
        if (!LocationUtils.canReach(block.getLocation(), getActiveNPC().getLocation())) {
            Bukkit.broadcastMessage("too far away - distance: " + getActiveNPC().getLocation().distance(block.getLocation()));
            markAsFinished(new Result<>(Result.Type.FAILURE));
        }
        // Broke the block
        else if (block.getType() != originalMaterial){
            Bukkit.broadcastMessage("Broke: "+originalMaterial);
            markAsFinished(new Result<>(Result.Type.SUCCESS));
        }
    }

    @Override
    public void onStop(StopType stopType) {
        getActiveNPC().getLookController().forget();
        if (stopType == StopType.FAILURE) {
            getActiveNPC().getBlockBreaker().stop();
        }
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
