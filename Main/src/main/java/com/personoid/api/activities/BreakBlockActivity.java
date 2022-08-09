package com.personoid.api.activities;

import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.debug.Profiler;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BreakBlockActivity extends Activity {
    private final Block block;
    private final Material originalMaterial;

    public BreakBlockActivity(Block block) {
        super(ActivityType.INTERACTION);
        this.block = block;
        originalMaterial = block.getType();
    }

    @Override
    public void onStart(Activity.StartType startType) {
        getNPC().getLookController().face(block.getLocation());
        getNPC().getBlockBreaker().start(block);
    }

    @Override
    public void onUpdate() {
        if (!LocationUtils.canReach(block.getLocation(), getNPC().getLocation())) {
            Profiler.ACTIVITIES.push("Block too far away, distance: " + getNPC().getLocation().distance(block.getLocation()));
            markAsFinished(new Result<>(Result.Type.FAILURE));
        }
        // Broke the block
        else if (block.getType() != originalMaterial){
            Profiler.ACTIVITIES.push("Broke " + originalMaterial.name().toLowerCase().replace("_", ""));
            markAsFinished(new Result<>(Result.Type.SUCCESS));
        }
    }

    @Override
    public void onStop(StopType stopType) {
        getNPC().getLookController().forget();
        if (stopType == StopType.FAILURE) {
            getNPC().getBlockBreaker().stop();
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
