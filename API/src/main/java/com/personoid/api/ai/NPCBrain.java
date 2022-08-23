package com.personoid.api.ai;

import com.personoid.api.ai.activity.ActivityManager;
import com.personoid.api.ai.looking.OpticsManager;
import com.personoid.api.ai.mood.MoodManager;
import com.personoid.api.npc.NPC;

public class NPCBrain {
    private final MoodManager moodManager;
    private final ActivityManager activityManager;
    private final OpticsManager opticsManager;

    public NPCBrain(NPC npc) {
        moodManager = new MoodManager(npc);
        activityManager = new ActivityManager(npc);
        opticsManager = new OpticsManager(npc);
    }

    public void tick() {
        moodManager.tick();
        activityManager.tick();
        opticsManager.tick();
    }

    public MoodManager getMoodManager() {
        return moodManager;
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public OpticsManager getOpticsManager() {
        return opticsManager;
    }
}
