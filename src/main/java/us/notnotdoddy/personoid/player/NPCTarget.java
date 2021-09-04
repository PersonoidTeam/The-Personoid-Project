package us.notnotdoddy.personoid.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class NPCTarget {
    private UUID uuid;

    public NPCTarget(LivingEntity entity) {
        uuid = entity.getUniqueId();
    }

    public void target() {

    }

    public <T> T getTarget(Class<T> type) {
        if (type == LivingEntity.class) {
            return (T)Bukkit.getEntity(uuid);
        } else return null;
    }

    public UUID getUUID() {
        return uuid;
    }
}
