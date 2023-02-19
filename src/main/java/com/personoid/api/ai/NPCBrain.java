package com.personoid.api.ai;

import com.personoid.api.ai.activity.ActivityManager;
import com.personoid.api.ai.looking.OpticsManager;
import com.personoid.api.ai.memory.MemoryManager;
import com.personoid.api.npc.NPC;

public class NPCBrain {
    private final ActivityManager activityManager;
    private final OpticsManager opticsManager;
    private final MemoryManager memoryManager;

    public NPCBrain(NPC npc) {
        activityManager = new ActivityManager(npc);
        opticsManager = new OpticsManager(npc);
        memoryManager = new MemoryManager(npc);
    }

    public void tick() {
        activityManager.tick();
        opticsManager.tick();
        memoryManager.tick();
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public OpticsManager getOpticsManager() {
        return opticsManager;
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }
}
