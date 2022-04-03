package com.personoid.npc.ai;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.activity.ActivityManager;
import com.personoid.npc.ai.messaging.MessageManager;
import com.personoid.npc.ai.relationship.RelationshipManager;
import com.personoid.npc.components.NPCTickingComponent;

public class NPCBrain extends NPCTickingComponent {
    private final RelationshipManager relationshipManager;
    private final ActivityManager activityManager;
    private final MessageManager messageManager;

    public NPCBrain(NPC npc) {
        super(npc);
        relationshipManager = new RelationshipManager(npc);
        activityManager = new ActivityManager(npc);
        messageManager = new MessageManager(npc);
    }

    @Override
    public void tick() {
        relationshipManager.tick();
        activityManager.tick();
    }

    public RelationshipManager getRelationshipManager() {
        return relationshipManager;
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
