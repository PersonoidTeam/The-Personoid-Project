package com.personoid.api.events;

import com.personoid.api.npc.NPC;
import org.jetbrains.annotations.NotNull;

public class NPCDeathEvent extends NPCEvent {
    public NPCDeathEvent(@NotNull NPC npc) {
        super(npc);
    }
}
