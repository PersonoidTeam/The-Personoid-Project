package com.notnotdoddy.personoid.npc.ai;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.ai.activity.ActivityManager;
import com.notnotdoddy.personoid.npc.components.NPCTickingComponent;

public class NPCBrain extends NPCTickingComponent {
    private final ActivityManager activityManager;

    public NPCBrain(NPC npc) {
        super(npc);
        activityManager = new ActivityManager(npc);
    }

    @Override
    public void tick() {
        activityManager.tick();
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }
}
