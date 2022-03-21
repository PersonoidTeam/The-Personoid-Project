package com.personoid.activites;

import com.personoid.enums.Structure;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;

public class FindStructureActivity<T extends Structure> extends Activity {
    private final T structure;

    public FindStructureActivity(T structure) {
        super(ActivityType.SEARCHING);
        this.structure = structure;
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
