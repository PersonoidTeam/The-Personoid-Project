package com.personoid.api.events;

import com.personoid.api.npc.NPC;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public abstract class NPCCancellableEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;

    public NPCCancellableEvent(@NotNull NPC npc) {
        super(npc);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
