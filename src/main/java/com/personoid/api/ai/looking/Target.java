package com.personoid.api.ai.looking;

import com.personoid.api.utils.types.Priority;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Target {
    private final Location location;
    private final Entity entity;
    private final Block block;
    private final Type type;
    private final Priority priority;
    private boolean tracking;

    public Target(Location location, Priority priority) {
        this.location = location;
        this.priority = priority;
        this.entity = null;
        this.block = null;
        this.type = Type.LOCATION;
    }

    public Target(Entity entity, Priority priority) {
        this.priority = priority;
        this.location = null;
        this.entity = entity;
        this.block = null;
        this.type = Type.ENTITY;
    }

    public Target(Block block, Priority priority) {
        this.priority = priority;
        this.location = null;
        this.entity = null;
        this.block = block;
        this.type = Type.BLOCK;
    }

    public Target track() {
        this.tracking = true;
        return this;
    }

    public Location getLocation() {
        switch (type) {
            case LOCATION: return location;
            case ENTITY: return entity.getLocation();
            case BLOCK: return block.getLocation().clone().add(0.5, -0.5, 0.5);
        }
        return null;
    }

    public Entity getEntity() {
        return entity;
    }

    public Block getBlock() {
        return block;
    }

    public Type getType() {
        return type;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isTracking() {
        return tracking;
    }

    public enum Type {
        LOCATION,
        ENTITY,
        BLOCK
    }
}
