package com.personoid.api.activities;

import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.looking.Target;
import com.personoid.api.ai.movement.MovementType;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class GoToLocationActivity extends Activity {
    private final Location location;
    private final MovementType movementType;
    private final Options options;
    private Block blockLoc;
    private int tick;

    public GoToLocationActivity(Location location, MovementType movementType) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.movementType = movementType;
        options = new Options();
    }

    @Override
    public void onStart(StartType startType) {
        blockLoc = location.getBlock();
        if (finishCheck()) return;
        updateLocation();
        Profiler.ACTIVITIES.push("going to location: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    @Override
    public void onUpdate() {
        if (++tick % 10 == 0) updateLocation();
        finishCheck();
    }

    private void updateLocation() {
        blockLoc = getNPC().getNavigation().moveTo(location, movementType);
        if (options.canFaceLocation()) getNPC().getLookController().addTarget("travel_location", new Target(location, Priority.NORMAL));
    }

    private boolean finishCheck() {
        if (blockLoc.getLocation().distance(getNPC().getLocation()) <= options.getStoppingDistance()) {
            markAsFinished(new Result<>(Result.Type.SUCCESS));
            return true;
        }
        return false;
    }

    @Override
    public void onStop(StopType stopType) {
        if (options.canFaceLocation()) getNPC().getLookController().removeTarget("travel_location");
    }

    @Override
    public boolean canStart(StartType startType) {
        //return location.distance(getNPC().getLocation()) > options.getStoppingDistance();
        return true;
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }

    public Options getOptions() {
        return options;
    }

    public static class Options {
        public double stoppingDistance = 0.5;
        public boolean faceLocation = true;

        private Options() {}

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
    }
}
