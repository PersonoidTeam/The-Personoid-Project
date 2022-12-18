package com.personoid.api.ai;

import com.personoid.api.ai.activity.ActivityManager;
import com.personoid.api.ai.looking.OpticsManager;
import com.personoid.api.npc.NPC;

public class NPCBrain {
    private final ActivityManager activityManager;
    private final OpticsManager opticsManager;

    public NPCBrain(NPC npc) {
        activityManager = new ActivityManager(npc);
        opticsManager = new OpticsManager(npc);
    }

    public void tick() {
        activityManager.tick();
        opticsManager.tick();
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public OpticsManager getOpticsManager() {
        return opticsManager;
    }
}
