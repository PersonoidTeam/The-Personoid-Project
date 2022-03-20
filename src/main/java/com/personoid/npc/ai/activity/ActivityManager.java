package com.personoid.npc.ai.activity;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;

public class ActivityManager extends NPCTickingComponent {
    private Activity currentActivity;
    private final Queue<Activity> activityQueue = new PriorityQueue<>(Collections.reverseOrder());

    public ActivityManager(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        if (currentActivity != null) super.tick();
    }

    public Activity startNextActivity() {
        if (activityQueue.isEmpty()) return null;
        currentTick = 0;
        currentActivity = activityQueue.poll();
        currentActivity.onStart();
        return currentActivity;
    }

    public void queueActivity(Activity activity) {
        activity.setManager(this);
        activityQueue.add(activity);
    }
}
