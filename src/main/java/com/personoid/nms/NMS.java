package com.personoid.nms;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.Parameter;
import com.personoid.api.utils.cache.Cache;
import com.personoid.nms.mappings.Mappings;
import com.personoid.nms.mappings.NMSClass;
import com.personoid.nms.packet.Package;
import com.personoid.nms.packet.*;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class NMS {
    private static final Cache CACHE = new Cache("NMSBridge");
    private static final Cache NPC_CACHE = new Cache("NMSBridge_NPC");

    static {
        CACHE.put("entity_player", NMSReflection.findClass(Packages.SERVER_LEVEL, "EntityPlayer"));
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
            Class<?> vec3Class = NMSReflection.findClass(Packages.VEC3D, "Vec3D");
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
                return NMSReflection.findClass(Packages.ENTITY_DATA_WATCHER, "DataWatcherRegistry"); // EntityDataSerializers
            });
            Class<?> serializerClass = CACHE.getOrPut("DataWatcherSerializer", () -> {
                return NMSReflection.findClass(Packages.ENTITY_DATA_WATCHER, "DataWatcherSerializer"); // EntityDataSerializer
            });
            Constructor<?> objectConstructor = CACHE.getOrPut("DataWatcherObject", () -> {
                try {
                    return NMSReflection.findClass(Packages.ENTITY_DATA_WATCHER, "DataWatcherObject") // EntityDataAccessor
                            .getConstructor(int.class, serializerClass); // EntityDataAccessor
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            Object data = objectConstructor.newInstance(key, NMSReflection.getField(registry, type));
            Object entityData = NPC_CACHE.getOrPut("entityData." + npc.getEntityId(), () -> {
                return NMSReflection.invoke(toNMSPlayer(npc), "aj"); // getEntityData
            });
            entityData.getClass().getMethod("b", data.getClass(), Object.class).invoke(entityData, data, value); // set
            //ReflectionUtils.invoke(entityData, "b", data, value); // set
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPos(NPC npc, Vector pos) {
        NMSReflection.invoke(toNMSPlayer(npc), "a", toVec3(pos)); // setPos
    }

    public static void respawn(NPC npc) {
        NMSReflection.invoke(toNMSPlayer(npc), "fH"); // respawn
    }

    public static void setArrowCount(NPC npc, int count) {
        try {
            CACHE.getClass("entity_player").getMethod("o", int.class).invoke(toNMSPlayer(npc), count); // setArrowCount
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addToWorld(NPC npc, World world) {
        Object nmsWorld = NMSReflection.getHandle(world);
        NMSClass serverLevel = Package.minecraft("server.level.ServerLevel").getMappedClass();
        serverLevel.getMethod("addNewPlayer", Package.SERVER_PLAYER_CLASS.getRawClass()).invoke(nmsWorld, toNMSPlayer(npc));
    }

    public static void remove(NPC npc) {
        Object discardReason = CACHE.getOrPut("discard_removal_reason", () -> {
            Class<?> removalReasonClass = NMSReflection.findClass(Packages.ENTITY, "Entity$RemovalReason");
            return NMSReflection.getEnum(removalReasonClass, "DISCARDED");
        });
        NMSReflection.invoke(toNMSPlayer(npc), "a", discardReason);
    }

    public static double getItemSpeed(ItemStack item) {
        return 0;
    }

    private static List<String> getProtocolPackages() {
        return Arrays.asList(
                Package.PROTOCOL.sub("game").toString(),
                Package.PROTOCOL.sub("handshake").toString(),
                Package.PROTOCOL.sub("login").toString(),
                Package.PROTOCOL.sub("status").toString()
        );
    }

    public static Packet createPacket(String className, Parameter... parameters) {
        Class<?> packetClass = null;
        for (String subPackage : getProtocolPackages()) {
            try {
                packetClass = Class.forName(Mappings.get().getSpigotClassName(subPackage + "." + className));
            } catch (ClassNotFoundException ignored) {}
        }
        if (packetClass == null) {
            throw new RuntimeException("Could not find packet " + className + " in sub packages");
        }
        try {
            Class<?>[] types = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                types[i] = parameters[i].getType();
            }
            Object[] args = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                args[i] = parameters[i].getValue();
            }
            Object packetInstance = packetClass.getConstructor(types).newInstance(args);
            return new Packet() {
                @Override
                public void send(Player to) {
                    try {
                        Object craftPlayer = to.getClass().getMethod("getHandle").invoke(to);
                        Field connField = craftPlayer.getClass().getField("b");
                        connField.setAccessible(true);
                        Object connection = connField.get(craftPlayer);
                        Class<?> packetClass = Class.forName(Package.PROTOCOL.sub("Packet").toString());
                        connection.getClass().getMethod("a", packetClass).invoke(connection, packetInstance);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                             NoSuchFieldException | ClassNotFoundException e) {
                        throw new RuntimeException("Could not get handle of player", e);
                    }
                }
            };
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create packet " + className, e);
        }
    }

    public static Object getPlayer(Player player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get handle of player", e);
        }
    }

    public static Object getEntity(Entity entity) {
        try {
            return entity.getClass().getMethod("getHandle").invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get handle of entity", e);
        }
    }
}
