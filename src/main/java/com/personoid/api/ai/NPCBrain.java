package com.personoid.api.ai;

import com.personoid.api.ai.activity.ActivityManager;
import com.personoid.api.ai.looking.Optics;
import com.personoid.api.npc.NPC;

public class NPCBrain {
    private final ActivityManager activityManager;
    private final Optics optics;
    private final Awareness awareness;

    public NPCBrain(NPC npc) {
        activityManager = new ActivityManager(npc);
        optics = new Optics(npc);
        awareness = new Awareness(npc);
    }

    public void tick() {
        activityManager.tick();
        optics.tick();
        awareness.tick();
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public Optics getOptics() {
        return optics;
    }

    public Awareness getAwareness() {
        return awareness;
    }
}
