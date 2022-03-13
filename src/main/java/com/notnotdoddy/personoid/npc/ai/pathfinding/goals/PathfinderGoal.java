package com.notnotdoddy.personoid.npc.ai.pathfinding.goals;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.ai.pathfinding.PathNode;
import org.bukkit.Location;

import java.util.List;

public abstract class PathfinderGoal {
    private Data data;
    private final NPC npc;
    private final Type type;
    private final Priority priority;
    private Location location;

    public PathfinderGoal(NPC npc, Type type, Priority priority) {
        this.npc = npc;
        this.type = type;
        this.priority = priority;
    }

    public PathfinderGoal(NPC npc, Type type, Priority priority, Data data) {
        this(npc, type, priority);
        this.data = data;
    }

    public PathNode transform(List<PathNode> navigated, PathNode finish, PathNode node) {
        //node.H = type.H; // FIXME: breaks pathfinding
        return node;
    }

    public Data getData() {
        return data;
    }

    public Priority getPriority(){
        return priority;
    }

    public Location getLocation() {
        return location;
    }

    /**Sets the location of the goal**/
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Called upon the NPC selecting the goal as their active goal.
     */
    public abstract void onStart();

    /**Is run when the selector updates the goal**/
    public abstract void onUpdate();

    /**
     * Called upon the NPC deselecting the goal as their active goal.
     */
    public abstract void onStop();

    /**
     * Is run when the selector attempts to start the goal
     * @return Whether to cancel the attempt
     **/
    public abstract boolean canStart(StartInfo info);

    /**
     * Is run when the selector attempts to stop the goal
     * @return Whether to cancel the attempt
     **/
    public abstract boolean canStop(StopInfo info);

    public static class Data {
        private float stoppingDistance;

        public float getStoppingDistance() {
            return stoppingDistance;
        }

        public Data setStoppingDistance(float distance) {
            stoppingDistance = distance;
            return this;
        }
    }

    public enum Type {
        PREFER(Double.NEGATIVE_INFINITY),
        AGAINST(Double.POSITIVE_INFINITY);

        final double H;

        Type(double H) {
            this.H = H;
        }
    }

    public enum Priority {
        HIGHEST(1F),
        HIGH(0.75F),
        NORMAL(0.5F),
        LOW(0.25F),
        LOWEST(0F);

        final float value;

        Priority(float value) {
            this.value = value;
        }

        public float getValue(){
            return value;
        }

        public boolean isHigherThan(Priority priority){
            return priority.getValue() < getValue();
        }
    }

    public enum StateReason {
        FORCED,
        PRIORITY,
        CHECK
    }

    public static class StartInfo {
        private final StateReason reason;
        private PathfinderGoal replacing;

        public StartInfo(StateReason reason) {
            this.reason = reason;
        }

        public StartInfo(StateReason reason, PathfinderGoal replacing) {
            this(reason);
            this.replacing = replacing;
        }

        public StateReason getReason() {
            return reason;
        }

        public PathfinderGoal getReplacing() {
            return replacing;
        }

        public boolean isReplacing() {
            return replacing != null;
        }
    }

    public static class StopInfo {
        private final StateReason reason;
        private PathfinderGoal replacement;

        public StopInfo(StateReason reason) {
            this.reason = reason;
        }

        public StopInfo(StateReason reason, PathfinderGoal replacer) {
            this(reason);
            this.replacement = replacer;
        }

        public StateReason getReason() {
            return reason;
        }

        public PathfinderGoal getReplacement() {
            return replacement;
        }

        public boolean hasReplacement() {
            return replacement != null;
        }
    }
}
