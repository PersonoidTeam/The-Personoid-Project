package com.personoid.activites.location;

import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import org.bukkit.Location;

public class GoToLocationActivity extends Activity {
    private final Location location;
    private final double stoppingDistance;
    private final boolean faceLocation;

    public GoToLocationActivity(Location location) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.stoppingDistance = 1.5D;
        this.faceLocation = true;
    }

    public GoToLocationActivity(Location location, boolean faceLocation) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.stoppingDistance = 1.5D;
        this.faceLocation = faceLocation;
    }

    public GoToLocationActivity(Location location, double stoppingDistance) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.stoppingDistance = stoppingDistance;
        this.faceLocation = true;
    }

    public GoToLocationActivity(Location location, double stoppingDistance, boolean faceLocation) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.stoppingDistance = stoppingDistance;
        this.faceLocation = faceLocation;
    }

    @Override
    public void onStart(StartType startType) {
        //Bukkit.broadcastMessage("Target: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        getActiveNPC().getNavigation().moveTo(location);
        if (faceLocation) {
            getActiveNPC().getLookController().face(location);
        }
    }

    @Override
    public void onUpdate() {
        if (faceLocation && getCurrentDuration() % 10 == 0) {
            getActiveNPC().getLookController().face(location);
        }
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
