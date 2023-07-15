package com.personoid.api.ai;

import com.personoid.api.ai.activity.ActivityManager;
import com.personoid.api.ai.looking.Optics;
import com.personoid.api.ai.memory.MemoryManager;
import com.personoid.api.npc.NPC;

public class NPCBrain {
    private final ActivityManager activityManager;
    private final Optics optics;
    private final MemoryManager memoryManager;
    private final Awareness awareness;

    public NPCBrain(NPC npc) {
        activityManager = new ActivityManager(npc);
        optics = new Optics(npc);
        memoryManager = new MemoryManager(npc);
        awareness = new Awareness(npc);
    }

    public void tick() {
        activityManager.tick();
        optics.tick();
        memoryManager.tick();
        awareness.tick();
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public Optics getOptics() {
        return optics;
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public Awareness getAwareness() {
        return awareness;
    }
}
