package com.personoid.activites.location;

import com.personoid.npc.ai.Priority;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.utils.MathUtils;
import org.bukkit.entity.Entity;

public class FollowEntityActivity extends Activity {
    private final Entity entity;
    private final double stoppingDistance;

    public FollowEntityActivity(Entity entity) {
        super(ActivityType.FOLLOWING, Priority.LOW, new BoredomSettings(MathUtils.random(600, 2400), MathUtils.random(2400, 12000)));
        this.entity = entity;
        this.stoppingDistance = 1.5D;
    }

    public FollowEntityActivity(Entity entity, double stoppingDistance) {
        super(ActivityType.FOLLOWING);
        this.entity = entity;
        this.stoppingDistance = stoppingDistance;
    }

    @Override
    public void onStart(StartType startType) {

    }

    @Override
    public void onUpdate() {
        if (getCurrentDuration() % 5 == 0) { //5
            run(new GoToLocationActivity(entity.getLocation(), stoppingDistance));
        }
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
