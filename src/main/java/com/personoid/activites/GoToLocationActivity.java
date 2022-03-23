package com.personoid.activites;

import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GoToLocationActivity extends Activity {
    private final Location location;

    public GoToLocationActivity(Location location) {
        super(ActivityType.LOCATION);
        this.location = location;
    }

    @Override
    public void onStart(StartType startType) {
        getActiveNPC().getNavigation().setTarget(location);
        Bukkit.broadcastMessage("Target: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    @Override
    public void onUpdate() {
        if (location.distance(getActiveNPC().getLocation()) <= 1) {
            Bukkit.broadcastMessage("Reached target");
            markAsFinished(new Result<>(Result.Type.SUCCESS));
        }
    }

    @Override
    public void onStop(StopType stopType) {
        getActiveNPC().getNavigation().setTarget(null);
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
