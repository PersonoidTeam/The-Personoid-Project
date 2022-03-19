package com.notnotdoddy.personoid.npc.ai.pathfinding.goals;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.components.NPCComponent;
import com.notnotdoddy.personoid.npc.enums.MovementType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public abstract class PathfinderGoal extends NPCComponent {
    private final Map<Parameter.Type, Parameter> parameters = new HashMap<>();
    private final Priority priority;
    private Location targetLocation;
    private Location facingLocation;

    public PathfinderGoal(NPC npc, Priority priority) {
        super(npc);
        this.priority = priority;
        for (Parameter.Type type : Parameter.Type.values()) {
            parameters.put(type, new Parameter(type));
        }
    }

    public Location getNextNodeLocation() {
        Vec3 pos = npc.getNavigation().getPath().getNPCPosAtNode(npc, npc.getNavigation().getPath().getNextNodeIndex() + 1);
        return new Location(npc.getLocation().getWorld(), pos.x, pos.y, pos.z);
    }

    /*Initialise any goal parameters here*/
    public void initParameters() {

    }

    public Priority getPriority(){
        return priority;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    /**Sets the location of the goal**/
    public void setTargetLocation(Location location) {
        this.targetLocation = location;
    }

    public Location getFacingLocation() {
        return facingLocation;
    }

    /**Sets the facing location of the goal**/
    public void setFacingLocation(Location location) {
        this.facingLocation = location;
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

    public static class Parameter {
        private final Type type;
        private Object value;

        private Parameter(Type type) {
            this.type = type;
            value = type.defaultValue;
        }

        public <T> T get() {
            return (T) value;
        }

        public void set(Object value) {
            if (!type.clazz.isInstance(value)) {
                throw new IllegalArgumentException("Value is not of the correct type " +
                        value.getClass().getSimpleName() + " -> " + type.clazz.getSimpleName());
            }
            this.value = value;
        }

        public enum Type {
            PREFER_TYPE(PreferType.class, PreferType.TOWARDS),
            STOPPING_DISTANCE(Double.class, 1.5),
            MOVEMENT_TYPE(MovementType.class, MovementType.WALKING);

            private final Class<?> clazz;
            private final Object defaultValue;

            Type(Class<?> type, Object defaultValue) {
                this.clazz = type;
                this.defaultValue = defaultValue;
            }

            public enum PreferType {
                TOWARDS(Double.NEGATIVE_INFINITY),
                AGAINST(Double.POSITIVE_INFINITY);

                final double H;

                PreferType(double H) {
                    this.H = H;
                }
            }
        }
    }

    public Parameter getParameter(Parameter.Type type) {
        return parameters.get(type);
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
