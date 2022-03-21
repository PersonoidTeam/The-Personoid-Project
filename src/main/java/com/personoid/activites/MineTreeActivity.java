package com.personoid.activites;

import com.personoid.enums.LogType;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;

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
        if (startType == StartType.START) {

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
        return false;
    }

    @Override
    public boolean canStop(StopType stopType) {
        return false;
    }
}
