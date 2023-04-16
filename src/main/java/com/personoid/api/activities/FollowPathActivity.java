package com.personoid.api.activities;

import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.looking.Target;
import com.personoid.api.npc.Pose;
import com.personoid.api.pathfinding.Path;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Location;

public class FollowPathActivity extends Activity {
    private final Path path;
    private Location endLocation;
    private final MovementType movementType;
    private final Options options;
    private int tick;

    private Location lastLocation;
    private int stuckTicks;

    public FollowPathActivity(Path path, MovementType movementType) {
        super(ActivityType.LOCATION);
        this.movementType = movementType;
        this.options = new Options();
        this.path = path;
    }

    @Override
    public void onStart(StartType startType) {
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
        this.endLocation = path.getNode(path.size() - 1).getPos().toLocation(getNPC().getWorld());
        getNPC().getNavigation().moveTo(endLocation, path);
        if (options.canFaceLocation()) {
            Location lookLoc = endLocation.clone().add(0F, 0F, 0F);
            getNPC().getLookController().addTarget("travel_location", new Target(lookLoc, options.facePriority));
        }
    }

    @Override
    public void onUpdate() {
        if (getNPC().canSprint() && !getNPC().isSprinting()) {
            getNPC().setSprinting(movementType.name().contains("SPRINT"));
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

    private boolean finishCheck() {
        if (options.getStoppingDistance() <= 0F) {
            Location lastNodeLoc = path.getNode(path.size() - 1).getPos().toLocation(getNPC().getWorld()).add(0.5, 0, 0.5);
            if (lastNodeLoc.distance(getNPC().getLocation()) <= 0.3F) {
                markAsFinished(new Result<>(Result.Type.SUCCESS));
                return true;
            }
        } else if (endLocation.distance(getNPC().getLocation()) <= options.getStoppingDistance()) {
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
        if (options.getStuckAction() == GoToLocationActivity.StuckAction.STOP) {
            getNPC().getNavigation().stop();
            markAsFinished(new Result<>(Result.Type.FAILURE));
        } else if (options.getStuckAction() == GoToLocationActivity.StuckAction.RESTART) {
            getNPC().getNavigation().stop();
            getNPC().getNavigation().moveTo(endLocation);
        } else if (options.getStuckAction() == GoToLocationActivity.StuckAction.REVERSE) {
            getNPC().getNavigation().stop();
            getNPC().getNavigation().moveTo(path.getNode(0).getPos().toLocation(getNPC().getWorld()));
        } else if (options.getStuckAction() == GoToLocationActivity.StuckAction.TELEPORT) {
            getNPC().getNavigation().stop();
            getNPC().teleport(endLocation);
        } else if (options.getStuckAction() == GoToLocationActivity.StuckAction.IGNORE) {
            // do nothing
        }
    }

    public Options getOptions() {
        return options;
    }

    public static class Options {
        public double stoppingDistance;
        public boolean faceLocation = true;
        public Priority facePriority = Priority.NORMAL;
        public int stuckTime = 20;
        public double stuckDelta = 0.01;
        public GoToLocationActivity.StuckAction stuckAction = GoToLocationActivity.StuckAction.STOP;

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

        public GoToLocationActivity.StuckAction getStuckAction() {
            return stuckAction;
        }

        public void setStuckAction(GoToLocationActivity.StuckAction stuckAction) {
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
