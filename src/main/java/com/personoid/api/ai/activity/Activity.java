package com.personoid.api.ai.activity;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.types.Priority;
import com.personoid.api.utils.debug.Profiler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Activity implements Comparable<Activity> {
    private ActivityManager manager;
    private NPC npc;
    private final ActivityType type;
    private Priority priority;
    private long currentDuration;
    private boolean finished;
    private Result<?> result;
    private boolean paused;
    private BoredomSettings boredomSettings;

    private final Set<Consumer<Result<?>>> callbacks = new HashSet<>();
    private Activity childActivity;
    private Activity parentActivity;

    public Activity(ActivityType type) {
        this.type = type;
        this.priority = Priority.NORMAL;
    }

    public Activity(ActivityType type, BoredomSettings boredomSettings) {
        this.type = type;
        this.priority = Priority.NORMAL;
        this.boredomSettings = boredomSettings;
    }

    public Activity(ActivityType type, Priority priority) {
        this.type = type;
        this.priority = priority;
    }

    public Activity(ActivityType type, Priority priority, BoredomSettings boredomSettings) {
        this.type = type;
        this.priority = priority;
        this.boredomSettings = boredomSettings;
    }

    // needs to be called before canStart() or onStart()
    void register(ActivityManager manager) {
        this.manager = manager;
        npc = manager.getNPC();
    }

    public NPC getNPC(){
        return npc;
    }

    public long getCurrentDuration() {
        return currentDuration;
    }

    public void internalStart(StartType startType) {
        if (startType == StartType.START) {
            currentDuration = 0;
            finished = false;
        } else if (startType == StartType.RESUME) {
            if (childActivity == null) return;
            childActivity.internalStart(StartType.RESUME);
            childActivity.onStart(StartType.RESUME);
        }
    }

    public void internalStop(StopType stopType) {
        if (childActivity != null) {
            childActivity.internalStop(stopType);
            childActivity.onStop(stopType);
        }
    }

    public void internalUpdate() {
        if (childActivity != null) {
            childActivity.internalUpdate();
            childActivity.onUpdate();
        }
        currentDuration++;
        if (boredomSettings != null){
            if (currentDuration >= boredomSettings.getBoredTime()){
                onStop(StopType.BORED);
                internalStop(StopType.BORED);
                manager.addBoredTask(this, boredomSettings.getBoredCooldown());
                Profiler.ACTIVITIES.push("Added bored cooldown for: " + getClass().getSimpleName());
            }
        }
    }

    public abstract void onStart(StartType startType);

    public abstract void onUpdate();

    public abstract void onStop(StopType stopType);

    public abstract boolean canStart(StartType startType);

    public abstract boolean canStop(StopType stopType);

    public Activity onFinished(Consumer<Result<?>> callback) {
        callbacks.add(callback);
        return this;
    }

    public void run(Activity activity) {
        activity.npc = npc;
        if (activity.canStart(StartType.START)) {
            if (childActivity != null) {
                childActivity.internalStop(StopType.STOP);
                childActivity.onStop(StopType.STOP);
            }
            childActivity = activity;
            activity.parentActivity = this;
            activity.internalStart(StartType.START);
            activity.onStart(StartType.START);
        }
    }

    protected void markAsFinished(Result<?> result) {
        if (parentActivity != null) {
            parentActivity.childActivity = null;
        }
        finished = true;
        this.result = result;
        callbacks.forEach(callback -> callback.accept(result));
        onStop(result.getType() == Result.Type.SUCCESS ? StopType.SUCCESS : StopType.FAILURE);
        internalStop(StopType.SUCCESS);
    }

    public BoredomSettings getBoredomSettings() {
        return boredomSettings;
    }

    public Result<?> getResult() {
        return result;
    }

    public boolean isFinished() {
        return finished;
    }

    public ActivityType getType() {
        return type;
    }

    public Priority getPriority() {
        return priority;
    }

    public void updatePriority(Priority priority) {
        this.priority = priority;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public Activity getChildActivity() {
        return childActivity;
    }

    @Override
    public int compareTo(@NotNull Activity activity) {
        return Double.compare(priority.getValue(), activity.priority.getValue());
    }

    public enum StartType {
        START,
        RESUME,
        RESTART
    }

    public enum StopType {
        STOP,
        PAUSE,
        SUCCESS,
        FAILURE,
        BORED,
    }

    public static class BoredomSettings {
        private int boredTime;
        private int boredCooldown;

        public BoredomSettings(int boredTime, int boredCooldown) {
            this.boredTime = boredTime;
            this.boredCooldown = boredCooldown;
        }

        public int getBoredTime() {
            return boredTime;
        }

        public int getBoredCooldown() {
            return boredCooldown;
        }

        public BoredomSettings setBoredTime(int boredTime) {
            this.boredTime = boredTime;
            return this;
        }

        public BoredomSettings setBoredCooldown(int boredCooldown) {
            this.boredCooldown = boredCooldown;
            return this;
        }
    }
}
