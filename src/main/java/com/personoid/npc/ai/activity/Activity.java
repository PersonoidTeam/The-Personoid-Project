package com.personoid.npc.ai.activity;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.Priority;
import com.personoid.utils.debug.Profiler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Activity implements Comparable<Activity> {
    private ActivityManager manager;
    private final ActivityType type;
    private Priority priority;
    private long currentDuration;
    private boolean finished;
    private Result<?> result;
    private boolean paused;
    private BoredomSettings boredomSettings;

    private final Set<Consumer<Result<?>>> callbacks = new HashSet<>();
    private Activity currentlyRunning;

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

    public NPC getActiveNPC(){
        return manager.getNPC();
    }

    public long getCurrentDuration() {
        return currentDuration;
    }

    public void setManager(ActivityManager manager) {
        this.manager = manager;
    }

    public void internalStart(StartType startType) {
        if (startType == StartType.START) {
            currentDuration = 0;
            finished = false;
        } else if (startType == StartType.RESUME) {
            if (currentlyRunning == null) return;
            currentlyRunning.internalStart(StartType.RESUME);
            currentlyRunning.onStart(StartType.RESUME);
        }
    }

    public void internalStop(StopType stopType) {
        if (stopType == StopType.PAUSE) {
            if (currentlyRunning == null) return;
            currentlyRunning.internalStop(StopType.PAUSE);
            currentlyRunning.onStop(StopType.PAUSE);
        }
    }

    public void internalUpdate() {
        if (currentlyRunning != null) {
            currentlyRunning.internalUpdate();
            currentlyRunning.onUpdate();
        }
        currentDuration++;
        if (boredomSettings != null){
            if (currentDuration >= boredomSettings.getBoredTime()){
                markAsFinished(new Result<>(Result.Type.BORED));
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
        activity.setManager(manager);
        if (activity.canStart(StartType.START)) {
            if (currentlyRunning != null) {
                currentlyRunning.internalStop(StopType.STOP);
                currentlyRunning.onStop(StopType.STOP);
            }
            currentlyRunning = activity;
            activity.internalStart(StartType.START);
            activity.onStart(StartType.START);
        }
    }

    protected void markAsFinished(Result<?> result) {
        finished = true;
        this.result = result;
        callbacks.forEach(callback -> callback.accept(result));
        onStop(result.getType() == Result.Type.SUCCESS ? StopType.SUCCESS : StopType.FAILURE);
        internalStop(StopType.SUCCESS);
        if (result.getType() == Result.Type.BORED) {
            manager.boredTasks.put(this, boredomSettings.getBoredCooldown());
            Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "Added bored cooldown for: " + this.getClass().getSimpleName());
        }
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

    public Activity getCurrentlyRunning() {
        return currentlyRunning;
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
