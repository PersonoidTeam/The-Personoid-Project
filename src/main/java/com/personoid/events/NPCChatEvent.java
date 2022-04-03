package com.personoid.events;

import com.personoid.npc.NPC;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NPCChatEvent extends NPCCancellableEvent {
    private final String message;
    private final Player receiver;

    public NPCChatEvent(@NotNull NPC npc, String message) {
        super(npc);
        this.message = message;
        this.receiver = null;
    }

    public NPCChatEvent(@NotNull NPC npc, String message, Player receiver) {
        super(npc);
        this.message = message;
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public Player getReceiver() {
        return receiver;
    }
}
