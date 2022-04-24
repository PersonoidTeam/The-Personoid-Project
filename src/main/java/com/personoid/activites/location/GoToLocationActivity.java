package com.personoid.activites.location;

import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import org.bukkit.Location;

public class GoToLocationActivity extends Activity {
    private final Location location;
    private final double stoppingDistance;

    public GoToLocationActivity(Location location) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.stoppingDistance = 1.5D;
    }

    public GoToLocationActivity(Location location, double stoppingDistance) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.stoppingDistance = stoppingDistance;
    }

    @Override
    public void onStart(StartType startType) {
        //getActiveNPC().getNavigation().setTarget(location);
        getActiveNPC().npcNavigation.moveTo(location);
        getActiveNPC().getLookController().face(location);
        //Bukkit.broadcastMessage("Target: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    @Override
    public void onUpdate() {
        if (location.distance(getActiveNPC().getLocation()) <= stoppingDistance) {
            markAsFinished(new Result<>(Result.Type.SUCCESS));
        }
    }

    @Override
    public void onStop(StopType stopType) {
        //getActiveNPC().getNavigation().setTarget(null);
        getActiveNPC().getLookController().forget();
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
