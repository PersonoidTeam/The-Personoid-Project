package com.personoid.api.activities;

import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.looking.Target;
import com.personoid.api.npc.Pose;
import com.personoid.api.pathfinding.Path;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class GoToLocationActivity extends Activity {
    private final Location location;
    private final Path path;
    private final Location groundLoc;
    private final MovementType movementType;
    private final Options options;
    private Block blockLoc;
    private int tick;

    private Location lastLocation;
    private int stuckTicks;
    private Location startingLocation;

    public GoToLocationActivity(Location location, MovementType movementType) {
        super(ActivityType.LOCATION);
        this.location = location;
        path = null;
        groundLoc = LocationUtils.getBlockInDir(location, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        this.movementType = movementType;
        options = new Options();
    }

    public GoToLocationActivity(Location location, Path path, MovementType movementType) {
        super(ActivityType.LOCATION);
        this.location = location;
        this.path = path;
        groundLoc = LocationUtils.getBlockInDir(location, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        this.movementType = movementType;
        options = new Options();
    }

    @Override
    public void onStart(StartType startType) {
        startingLocation = getNPC().getLocation().clone();
        blockLoc = location.getBlock();
        getNPC().setJumping(movementType.name().contains("JUMP"));
        switch (movementType) {
            case WALK:
            case SPRINT:
            case SPRINT_JUMP:
                getNPC().setPose(Pose.STANDING);
                break;
            case SNEAK:
                getNPC().setPose(Pose.SNEAKING);
                break;
            case FLY:
                getNPC().setPose(Pose.FLYING);
                break;
        }
        updateLocation();
        Profiler.ACTIVITIES.push("going to location: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    @Override
    public void onUpdate() {
        if (getNPC().canSprint() && !getNPC().isSprinting()) {
            getNPC().setSprinting(movementType.name().contains("SPRINT"));
        }
        if (++tick % 10 == 0) {
            updateLocation();
        }
        doStuckDetection();
        finishCheck();
    }

    private void doStuckDetection() {
        if (lastLocation != null) {
            Location tempLoc = getNPC().getLocation().clone();
            tempLoc.setY(lastLocation.getY());
            if (lastLocation.distanceSquared(tempLoc) < options.stuckDelta) {
                stuckTicks++;
                if (stuckTicks >= options.stuckTime) {
                    stuckTicks = 0;
                    onStuck();
                }
            } else {
                stuckTicks = 0;
            }
        } else {
            stuckTicks = 0;
        }
        lastLocation = getNPC().getLocation().clone();
    }

    private void updateLocation() {
        blockLoc = getNPC().getNavigation().moveTo(location, path);
        if (options.canFaceLocation()) {
            Location lookLoc = groundLoc.clone().add(0F, 2F, 0F);
            getNPC().getLookController().addTarget("travel_location", new Target(lookLoc, options.facePriority));
        }
    }

    private boolean finishCheck() {
        if (groundLoc.distance(getNPC().getLocation()) <= options.getStoppingDistance()) {
            markAsFinished(new Result<>(Result.Type.SUCCESS));
            return true;
        }
        return false;
    }

    @Override
    public void onStop(StopType stopType) {
        getNPC().getNavigation().stop();
        getNPC().getMoveController().stop();
        if (options.canFaceLocation()) getNPC().getLookController().removeTarget("travel_location");
    }

    @Override
    public boolean canStart(StartType startType) {
        return true; //return location.distance(getNPC().getLocation()) > options.getStoppingDistance();
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }

    public void onStuck() {
        if (options.getStuckAction() == StuckAction.STOP) {
            getNPC().getNavigation().stop();
            markAsFinished(new Result<>(Result.Type.FAILURE));
        } else if (options.getStuckAction() == StuckAction.RESTART) {
            getNPC().getNavigation().stop();
            getNPC().getNavigation().moveTo(location);
        } else if (options.getStuckAction() == StuckAction.REVERSE) {
            getNPC().getNavigation().stop();
            getNPC().getNavigation().moveTo(startingLocation);
        } else if (options.getStuckAction() == StuckAction.TELEPORT) {
            getNPC().getNavigation().stop();
            getNPC().teleport(location);
        } else if (options.getStuckAction() == StuckAction.IGNORE) {
            // do nothing
        }
    }

    public Options getOptions() {
        return options;
    }

    public static class Options {
        public double stoppingDistance = 0.5;
        public boolean faceLocation = true;
        public Priority facePriority = Priority.NORMAL;
        public int stuckTime = 20;
        public double stuckDelta = 0.01;
        public StuckAction stuckAction = StuckAction.STOP;

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

        public void setFaceLocation(boolean faceLocation, Priority priority) {
            this.faceLocation = faceLocation;
            facePriority = priority;
        }

        public Priority getFacePriority() {
            return facePriority;
        }

        public int getStuckTime() {
            return stuckTime;
        }

        public void setStuckTime(int stuckTime) {
            this.stuckTime = stuckTime;
        }

        public double getStuckDelta() {
            return stuckDelta;
        }

        public void setStuckDelta(double stuckDelta) {
            this.stuckDelta = stuckDelta;
        }

        public StuckAction getStuckAction() {
            return stuckAction;
        }

        public void setStuckAction(StuckAction stuckAction) {
            this.stuckAction = stuckAction;
        }
    }

    public enum StuckAction {
        STOP,
        RESTART,
        REVERSE,
        TELEPORT,
        IGNORE
    }

    public enum MovementType {
        WALK,
        SNEAK,
        SPRINT,
        SPRINT_JUMP,
        FLY
    }
}
