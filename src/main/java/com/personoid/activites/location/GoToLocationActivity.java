package com.personoid.activites.location;

import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import com.personoid.npc.ai.pathfinding.MovementType;
import org.bukkit.Location;

public class GoToLocationActivity extends Activity {
    private final Location location;
    private Options options;
    private boolean wasFaceSmoothing;

    public GoToLocationActivity(Location location, Options options) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.options = options;
    }

    @Override
    public void onStart(StartType startType) {
        //Bukkit.broadcastMessage("Target: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        getActiveNPC().getNavigation().moveTo(location, MovementType.WALKING);

        if (options.canFaceLocation()) {
            getActiveNPC().getLookController().face(location);
        }
        wasFaceSmoothing = getActiveNPC().getLookController().isSmoothing();
        getActiveNPC().getLookController().setSmoothing(options.isFaceSmoothing());
    }

    @Override
    public void onUpdate() {
        getActiveNPC().getLookController().face(location);
        if (location.distance(getActiveNPC().getLocation()) <= options.getStoppingDistance()) {
            markAsFinished(new Result<>(Result.Type.SUCCESS));
        }
    }

    @Override
    public void onStop(StopType stopType) {
        //getActiveNPC().getNavigation().setTarget(null);
        getActiveNPC().getLookController().forget();
        getActiveNPC().getLookController().setSmoothing(wasFaceSmoothing);
    }

    @Override
    public boolean canStart(StartType startType) {
        return !(location.distance(getActiveNPC().getLocation()) <= options.getStoppingDistance());
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }

    public static class Options {
        public double stoppingDistance;
        public boolean faceLocation;
        public boolean faceSmoothing;

        public Options(double stoppingDistance, boolean faceLocation) {
            this.stoppingDistance = stoppingDistance;
            this.faceLocation = faceLocation;
        }

        public double getStoppingDistance() {
            return stoppingDistance;
        }

        public void setStoppingDistance(double stoppingDistance) {
            this.stoppingDistance = stoppingDistance;
        }

        public boolean canFaceLocation() {
            return faceLocation;
        }

        public void setFaceLocation(boolean faceLocation) {
            this.faceLocation = faceLocation;
        }

        public boolean isFaceSmoothing() {
            return faceSmoothing;
        }

        public void setFaceSmoothing(boolean faceSmoothing) {
            this.faceSmoothing = faceSmoothing;
        }
    }
}
