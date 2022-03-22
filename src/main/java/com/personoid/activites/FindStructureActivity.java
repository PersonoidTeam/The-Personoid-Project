package com.personoid.activites;

import com.personoid.enums.Structure;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import com.personoid.utils.MathUtils;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class FindStructureActivity extends Activity {
    private final Structure structure;
    private final List<Location> attempted = new ArrayList<>();

    public FindStructureActivity(Structure structure) {
        super(ActivityType.SEARCHING);
        this.structure = structure;
    }

    @Override
    public void onStart(StartType startType) {
        attempted.clear(); // acts like a 'try again after a break'
        if (startType == StartType.START) {
            tryNewLocation();
        }
    }

    public void tryNewLocation() {
        if (attempted.size() > 5) { // tweak this value
            markAsFinished(new Result<>(Result.Type.FAILURE, attempted));
        }
        Location loc = getActiveNPC().getLocation().add(MathUtils.random(-50, 50), MathUtils.random(-50, 50), MathUtils.random(-50, 50));
        attempted.add(loc);
        run(new GoToLocationActivity(loc).onFinished((result) -> {
            if (result.getType() == Result.Type.SUCCESS) {
                // TODO: better tree detection
                // check every block in 20 block radius
                for (int x = -20; x <= 20; x++) {
                    for (int y = -20; y <= 20; y++) {
                        for (int z = -20; z <= 20; z++) {
                            Location checkLoc = loc.clone().add(x, y, z);
                            if (checkLoc.getBlock().getType().isSolid()) {
                                if (checkLoc.getBlock().getType().name().contains("LOG")) {
                                    // found a log
                                    markAsFinished(new Result<>(Result.Type.SUCCESS, checkLoc));
                                }
                            }
                        }
                    }
                }
            } else tryNewLocation();
        }));
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onStop(StopType stopType) {

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
