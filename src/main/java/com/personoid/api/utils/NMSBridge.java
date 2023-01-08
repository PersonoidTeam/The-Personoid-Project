package com.personoid.api.utils;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.packet.Packages;
import com.personoid.api.utils.packet.ReflectionUtils;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class NMSBridge {
    private static final CacheManager CACHE = new CacheManager("NMSBridge");
    private static final CacheManager NPC_CACHE = new CacheManager("NMSBridge_NPC");

    static {
        CACHE.put("entity_player", ReflectionUtils.findClass(Packages.SERVER_LEVEL, "EntityPlayer"));
    }

    public static void removeCachedData(NPC npc) {
        int id = npc.getEntityId();
        NPC_CACHE.remove("nmsPlayer." + id);
        NPC_CACHE.remove("entityData." + id);
    }

    public static Object toNMSPlayer(NPC npc) {
        return NPC_CACHE.getOrPut("nmsPlayer." + npc.getEntityId(), () -> {
            try {
                return npc.getEntity().getClass().getMethod("getHandle").invoke(npc.getEntity());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static Object toVec3(Vector vector) {
        Constructor<?> vec3Constructor = CACHE.getOrPut("Vec3()", () -> {
            Class<?> vec3Class = ReflectionUtils.findClass(Packages.VEC3D, "Vec3D");
            try {
                return vec3Class.getConstructor(double.class, double.class, double.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to convert bukkit vector to nms vector", e);
            }
        });
        try {
            return vec3Constructor.newInstance(vector.getX(), vector.getY(), vector.getZ());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to convert bukkit vector to nms vector", e);
        }
    }

    public static void setEntityData(NPC npc, int key, String type, Object value) {
        if (type.equalsIgnoreCase("byte")) type = "a";
        try {
            Class<?> registry = CACHE.getOrPut("DataWatcherRegistry", () -> {
                return ReflectionUtils.findClass(Packages.ENTITY_DATA_WATCHER, "DataWatcherRegistry"); // EntityDataSerializers
            });
            Class<?> serializerClass = CACHE.getOrPut("DataWatcherSerializer", () -> {
                return ReflectionUtils.findClass(Packages.ENTITY_DATA_WATCHER, "DataWatcherSerializer"); // EntityDataSerializer
            });
            Constructor<?> objectConstructor = CACHE.getOrPut("DataWatcherObject", () -> {
                try {
                    return ReflectionUtils.findClass(Packages.ENTITY_DATA_WATCHER, "DataWatcherObject") // EntityDataAccessor
                            .getConstructor(int.class, serializerClass); // EntityDataAccessor
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            Object data = objectConstructor.newInstance(key, ReflectionUtils.getField(registry, type));
            Object entityData = NPC_CACHE.getOrPut("entityData." + npc.getEntityId(), () -> {
                return ReflectionUtils.invoke(toNMSPlayer(npc), "al"); // getEntityData
            });
            entityData.getClass().getMethod("b", data.getClass(), Object.class).invoke(entityData, data, value);
            //ReflectionUtils.invoke(entityData, "b", data, value); // set
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPos(NPC npc, Vector pos) {
        ReflectionUtils.invoke(toNMSPlayer(npc), "a_", toVec3(pos)); // setPos
    }

    public static void respawn(NPC npc) {
        ReflectionUtils.invoke(toNMSPlayer(npc), "fC"); // respawn
    }

    public static void setArrowCount(NPC npc, int count) {
        try {
            CACHE.getClass("entity_player").getMethod("p", int.class).invoke(toNMSPlayer(npc), count); // setArrowCount
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addToWorld(NPC npc, World world) {
        try {
            Object nmsWorld = ReflectionUtils.invoke(world, "getHandle"); // TODO: cache this?
            //ReflectionUtils.invoke(nmsWorld, "c", toNMSPlayer(npc)); // addNewPlayer
            nmsWorld.getClass().getMethod("c", CACHE.getClass("entity_player")).invoke(nmsWorld, toNMSPlayer(npc));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void remove(NPC npc) {
        Object discardReason = CACHE.getOrPut("discard_removal_reason", () -> {
            Class<?> removalReasonClass = ReflectionUtils.findClass(Packages.ENTITY, "Entity$RemovalReason");
            return ReflectionUtils.getEnum(removalReasonClass, "DISCARDED");
        });
        ReflectionUtils.invoke(toNMSPlayer(npc), "a", discardReason);
    }
}
