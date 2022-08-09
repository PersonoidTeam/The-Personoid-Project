package com.personoid.api.ai;

import com.personoid.api.ai.activity.ActivityManager;
import com.personoid.api.ai.mood.MoodManager;
import com.personoid.api.npc.NPC;

public class NPCBrain {
    private final MoodManager moodManager;
    private final ActivityManager activityManager;

    public NPCBrain(NPC npc) {
        moodManager = new MoodManager(npc);
        activityManager = new ActivityManager(npc);
    }

    public void tick() {
        moodManager.tick();
        activityManager.tick();
    }

    public MoodManager getMoodManager() {
        return moodManager;
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }
}
