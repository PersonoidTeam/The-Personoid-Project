package com.personoid.events;

import com.personoid.npc.NPC;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NPCChatEvent extends NPCCancellableEvent {
    private final String message;

    public NPCChatEvent(@NotNull NPC npc, String message) {
        super(npc);
        this.message = message;
    }

    public NPCChatEvent(@NotNull NPC npc, String message, Player to) {
        super(npc);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
