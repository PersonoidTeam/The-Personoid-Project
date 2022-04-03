package com.personoid.npc.ai.activity;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.Priority;
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

    private final Set<Consumer<Result<?>>> callbacks = new HashSet<>();
    private Activity currentlyRunning;

    public Activity(ActivityType type) {
        this.type = type;
        this.priority = Priority.NORMAL;
    }

    public Activity(ActivityType type, Priority priority) {
        this.type = type;
        this.priority = priority;
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
}
