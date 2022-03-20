package com.notnotdoddy.personoid.npc.ai.activity;

import com.notnotdoddy.personoid.npc.ai.Priority;
import org.jetbrains.annotations.NotNull;

public abstract class Activity implements Comparable<Activity> {
    private final ActivityType type;
    private Priority priority;

    public Activity(ActivityType type, Priority priority) {
        this.type = type;
        this.priority = priority;
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
