package com.personoid.events;

import com.personoid.npc.NPC;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class NPCEvent extends Event {
    protected NPC npc;

    public NPCEvent(@NotNull NPC npc) {
        this.npc = npc;
    }

    @NotNull
    public NPC getNPC() {
        return this.npc;
    }
}
