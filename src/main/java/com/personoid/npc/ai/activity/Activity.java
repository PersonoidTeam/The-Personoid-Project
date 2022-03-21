package com.personoid.npc.ai.activity;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.Priority;
import org.jetbrains.annotations.NotNull;

public abstract class Activity implements Comparable<Activity> {
    private ActivityManager manager;
    private final ActivityType type;
    private Priority priority;
    private long currentDuration;
    private boolean finished;
    private boolean paused;

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

    public void internalStart(ActivityManager manager, StartType startType) {
        this.manager = manager;
        if (startType == StartType.START) {
            currentDuration = 0;
            finished = false;
        }
    }

    public void internalUpdate() {
        currentDuration++;
    }

    public abstract void onStart(StartType startType);

    public abstract void onUpdate();

    public abstract void onStop(StopType stopType);

    public abstract boolean canStart(StartType startType);

    public abstract boolean canStop(StopType stopType);

    private void markAsFinished() {
        finished = true;
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
        PAUSE
    }
}
