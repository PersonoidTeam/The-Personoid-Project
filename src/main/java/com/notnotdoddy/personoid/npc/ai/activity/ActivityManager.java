package com.notnotdoddy.personoid.npc.ai.activity;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.components.NPCTickingComponent;

import java.util.PriorityQueue;
import java.util.Queue;

public class ActivityManager extends NPCTickingComponent {
    private Activity currentActivity;
    private final Queue<Activity> activityQueue = new PriorityQueue<>();

    public ActivityManager(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public Activity getNextActivity() {
        currentTick = 0;
        return activityQueue.poll();
    }

    public void queueActivity(Activity activity) {
        activityQueue.add(activity);
    }
}
