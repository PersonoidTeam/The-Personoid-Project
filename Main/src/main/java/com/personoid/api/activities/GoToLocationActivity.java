package com.personoid.api.activities;

import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.movement.MovementType;
import com.personoid.api.utils.Result;
import org.bukkit.Location;

public class GoToLocationActivity extends Activity {
    private final Location location;
    private final MovementType movementType;
    private final Options options;
    private boolean wasFaceSmoothing;

    public GoToLocationActivity(Location location, MovementType movementType) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.movementType = movementType;
        options = new Options();
    }

    public GoToLocationActivity(Location location, MovementType movementType, Options options) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.movementType = movementType;
        this.options = options;
    }

    @Override
    public void onStart(StartType startType) {
        //Bukkit.broadcastMessage("Target: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        getNPC().getNavigation().moveTo(location, movementType);
        if (options.canFaceLocation()) getNPC().getLookController().face(location);
        wasFaceSmoothing = getNPC().getLookController().isSmoothing();
        getNPC().getLookController().setSmoothing(options.isFaceSmoothing());
    }

    @Override
    public void onUpdate() {
        getNPC().getLookController().face(location);
        if (location.distance(getNPC().getLocation()) <= options.getStoppingDistance()) {
            markAsFinished(new Result<>(Result.Type.SUCCESS));
        }
    }

    @Override
    public void onStop(StopType stopType) {
        //getActiveNPC().getNavigation().setTarget(null);
        getNPC().getLookController().forget();
        getNPC().getLookController().setSmoothing(wasFaceSmoothing);
    }

    @Override
    public boolean canStart(StartType startType) {
        return !(location.distance(getNPC().getLocation()) <= options.getStoppingDistance());
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }

    public static class Options {
        public double stoppingDistance;
        public boolean faceLocation = true;
        public boolean faceSmoothing = true;

        public Options() {

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
