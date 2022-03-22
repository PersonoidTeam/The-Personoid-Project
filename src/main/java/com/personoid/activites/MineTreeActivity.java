package com.personoid.activites;

import com.personoid.enums.LogType;
import com.personoid.enums.Structure;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class MineTreeActivity extends Activity {
    private final LogType logType;

    public MineTreeActivity() {
        super(ActivityType.GATHERING);
        logType = LogType.OAK; // search for nearest tree type?
    }

    public MineTreeActivity(LogType logType) {
        super(ActivityType.GATHERING);
        this.logType = logType;
    }

    @Override
    public void onStart(StartType startType) {
        // this activity is made up of a sequence of activities + its own logic
        if (startType == StartType.START) {
            // runs the find structure activity (tree)
            run(new FindStructureActivity(Structure.TREE).onFinished((result) -> {
                // check if it found a tree
                if (result.getType() == Result.Type.SUCCESS) {
                    // if it did, get the location of the tree
                    Location loc = result.getResult(Location.class);

                    // TODO: mine log, check if it is correct log type

                    Bukkit.broadcastMessage("Found tree at " + loc.toString());
                    markAsFinished(new Result<>(Result.Type.SUCCESS, loc));
                }
            }));
        }
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
