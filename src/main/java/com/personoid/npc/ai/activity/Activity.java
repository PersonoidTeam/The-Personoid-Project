package com.personoid.npc.ai.activity;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.Priority;
import org.jetbrains.annotations.NotNull;

public abstract class Activity implements Comparable<Activity> {
    private ActivityManager manager;
    private final ActivityType type;
    private Priority priority;

    public Activity(ActivityType type, Priority priority) {
        this.type = type;
        this.priority = priority;
    }

    public void setManager(ActivityManager activityManager){
        this.manager = activityManager;
    }

    public NPC getActiveNPC(){
        return manager.getNPC();
    }

    public abstract void onStart();

    public abstract void onUpdate();

    public abstract void onStop();

    public void end() {
        manager.startNextActivity();
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

    @Override
    public int compareTo(@NotNull Activity activity) {
        return Double.compare(priority.getValue(), activity.priority.getValue());
    }
}
