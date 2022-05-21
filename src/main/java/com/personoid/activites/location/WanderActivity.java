package com.personoid.activites.location;

import com.personoid.npc.ai.Priority;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.utils.LocationUtils;
import com.personoid.utils.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WanderActivity extends Activity {
    private final Range range;

    public WanderActivity() {
        super(ActivityType.WANDERING, Priority.LOWEST);
        range = new Range(10, 25);
    }

    public WanderActivity(Range range) {
        super(ActivityType.WANDERING, Priority.LOWEST);
        this.range = range;
    }

    @Override
    public void onStart(StartType startType) {
        goToNewLocation();
    }

    private void goToNewLocation() {
        Location loc = LocationUtils.validRandom(getActiveNPC().getLocation(), range, 0.85F);
        Bukkit.broadcastMessage("Going to " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        run(new GoToLocationActivity(loc).onFinished(result -> goToNewLocation()));
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
