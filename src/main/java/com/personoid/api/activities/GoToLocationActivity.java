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
        blockLoc = location.getBlock();
        if (finishCheck()) return;
        Profiler.ACTIVITIES.push("going to location: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        blockLoc = getNPC().getNavigation().moveTo(location, movementType);
        if (options.canFaceLocation()) getNPC().getLookController().addTarget("travel_location", new Target(location, Priority.NORMAL));
    }

    @Override
    public void onUpdate() {
        finishCheck();
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

    public static class Options {
        public double stoppingDistance = 0.5;
        public boolean faceLocation = true;

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
    }
}
