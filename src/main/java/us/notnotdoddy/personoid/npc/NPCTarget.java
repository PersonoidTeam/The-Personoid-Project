package us.notnotdoddy.personoid.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class NPCTarget {
    private TargetType targetType;
    private EntityTargetType entityTargetType;
    private BlockTargetType blockTargetType;
    private Location location;
    private UUID uuid;
    private Block block;

    public enum TargetType {
        LOCATION,
        ENTITY,
        BLOCK,
    }

    public enum EntityTargetType {
        FOLLOW,
        ATTACK,
    }

    public enum BlockTargetType {
        LOCATION,
        BREAK,
        INTERACT,
    }

    public NPCTarget(Location location) {
        this.location = location;
        targetType = TargetType.LOCATION;
    }

    public NPCTarget(LivingEntity entity) {
        this(entity, EntityTargetType.FOLLOW);
    }

    public NPCTarget(LivingEntity entity, EntityTargetType type) {
        uuid = entity.getUniqueId();
        targetType = TargetType.ENTITY;
        entityTargetType = type;
    }

    public NPCTarget(Block block) {
        this(block, BlockTargetType.LOCATION);
    }

    public NPCTarget(Block block, BlockTargetType type) {
        this.block = block;
        targetType = TargetType.BLOCK;
        blockTargetType = type;
    }

    public NPCTarget target(PersonoidNPC npc) {
        npc.data.target = this;
        if (targetType == TargetType.LOCATION) {
            npc.getNavigator().setTarget(location);
        } else if (targetType == TargetType.ENTITY) {
            npc.getNavigator().getLocalParameters().straightLineTargetingDistance(0);
            if (entityTargetType == EntityTargetType.FOLLOW) {
                npc.citizen.getNavigator().setTarget(getTarget(LivingEntity.class), false);
            } else if (entityTargetType == EntityTargetType.ATTACK) {
                npc.citizen.getNavigator().setTarget(getTarget(LivingEntity.class), false);
            }
        } else if (targetType == TargetType.BLOCK) {
            npc.citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
            // note to self: in future make sure entity stops near block if it can't get to it exactly
            if (blockTargetType == BlockTargetType.LOCATION) {
                npc.citizen.getNavigator().setTarget(block.getLocation());
            } else if (blockTargetType == BlockTargetType.BREAK) {
                npc.breakBlock(block.getLocation());
            } else if (blockTargetType == BlockTargetType.INTERACT) {
                Bukkit.broadcastMessage("Block interaction is not yet implemented");
            }
        }
        return this;
    }

    public <T> T getTarget(Class<T> type) {
        if (type == LivingEntity.class) {
            return (T)Bukkit.getEntity(uuid);
        } else if (type == Block.class) {
            return (T)block;
        } else if (type == Location.class) {
            if (targetType == TargetType.LOCATION) {
                return (T)location;
            } else if (targetType == TargetType.ENTITY) {
                return (T)Bukkit.getEntity(uuid).getLocation();
            } else if (targetType == TargetType.BLOCK) {
                return (T)block.getLocation();
            }
        }
        return null;
    }

    public <T> T getTargetType() {
        if (targetType == TargetType.LOCATION) {
            return (T)TargetType.LOCATION;
        } else if (targetType == TargetType.ENTITY) {
            return (T)entityTargetType;
        } else if (targetType == TargetType.BLOCK) {
            return (T)blockTargetType;
        }
        return null;
    }
}
