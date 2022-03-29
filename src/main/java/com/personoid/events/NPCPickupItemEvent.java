package com.personoid.events;

import com.personoid.npc.NPC;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NPCPickupItemEvent extends NPCEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Item item;
    private boolean cancel = false;
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

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
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
