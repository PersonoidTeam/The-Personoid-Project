package com.personoid.events;

import com.personoid.npc.NPC;
import org.jetbrains.annotations.NotNull;

public class NPCDeathEvent extends NPCCancellableEvent {
    public NPCDeathEvent(@NotNull NPC npc) {
        super(npc);
    }
}
