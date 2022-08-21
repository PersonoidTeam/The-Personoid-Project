package com.personoid.api.events;

import com.personoid.api.npc.NPC;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NPCPickupItemEvent extends NPCCancellableEvent {
    private final Item item;
    private LivingEntity thrower;
    //private final int remaining;

    public NPCPickupItemEvent(@NotNull NPC npc, Item item) {
        super(npc);
        this.item = item;
    }

    public NPCPickupItemEvent(@NotNull NPC npc, Item item, LivingEntity thrower) {
        super(npc);
        this.item = item;
        this.thrower = thrower;
    }

    @NotNull
    public Item getItem() {
        return this.item;
    }

    @Nullable
    public LivingEntity getThrower() {
        return thrower;
    }

/*    public int getRemaining() {
        return this.remaining;
    }*/
}
