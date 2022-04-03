package com.personoid.events;

import com.personoid.npc.NPC;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class NPCEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected NPC npc;

    public NPCEvent(@NotNull NPC npc) {
        this.npc = npc;
    }

    @NotNull
    public NPC getNPC() {
        return this.npc;
    }

    @NotNull @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
