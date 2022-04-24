package com.personoid.npc.ai;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.activity.ActivityManager;
import com.personoid.npc.ai.messaging.MessageManager;
import com.personoid.npc.ai.mood.MoodManager;
import com.personoid.npc.components.NPCTickingComponent;

public class NPCBrain extends NPCTickingComponent {
    private final MoodManager moodManager;
    private final ActivityManager activityManager;
    private final MessageManager messageManager;

    public NPCBrain(NPC npc) {
        super(npc);
        moodManager = new MoodManager(npc);
        activityManager = new ActivityManager(npc);
        messageManager = new MessageManager(npc);
    }

    @Override
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

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
