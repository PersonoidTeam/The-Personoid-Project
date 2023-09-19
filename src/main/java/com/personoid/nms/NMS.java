package com.personoid.nms;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.Parameter;
import com.personoid.api.utils.cache.Cache;
import com.personoid.nms.mappings.*;
import com.personoid.nms.packet.NMSReflection;
import com.personoid.nms.packet.Package;
import com.personoid.nms.packet.Packages;
import com.personoid.nms.packet.Packet;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
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

    public static <T> T invoke(NPC npc, String methodName, Parameter... args) {
        Class<?>[] argTypes = Arrays.stream(args).map(Parameter::getType).toArray(Class[]::new);
        NMSClass currentClass = Package.SERVER_PLAYER_CLASS.getMappedClass();
        while (currentClass != null) {
            NMSMethod method = currentClass.getMethod(methodName, argTypes);
            if (method != null) {
                Object[] argValues = Arrays.stream(args).map(Parameter::getValue).toArray(Object[]::new);
                return method.invoke(toNMSPlayer(npc), argValues);
            } else {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    public static Object toNMSPlayer(NPC npc) {
        return NPC_CACHE.getOrPut("nmsPlayer." + npc.getEntityId(), () -> {
            return NMSReflection.getHandle(npc.getEntity());
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
        NMSClass registry = CACHE.getOrPut("EntityDataSerializers", () -> {
            return Package.NETWORK_SYNCHER.sub("EntityDataSerializers").getMappedClass();
        });
        NMSClass serializerClass = CACHE.getOrPut("EntityDataSerializer", () -> {
            return Package.NETWORK_SYNCHER.sub("EntityDataSerializer").getMappedClass();
        });
        NMSConstructor constructor = CACHE.getOrPut("EntityDataAccessor", () -> {
            NMSClass nmsClass = Package.NETWORK_SYNCHER.sub("EntityDataAccessor").getMappedClass();
            return nmsClass.getConstructor(int.class, serializerClass.getRawClass());
        });

        NMSField field = registry.getField(type.toUpperCase());
        Object data = constructor.newInstance(key, field.getStaticValue());
        Object entityData = NPC_CACHE.getOrPut("entityData." + npc.getEntityId(), () -> {
            return invoke(npc, "getEntityData");
        });

        NMSClass dataClass = Package.NETWORK_SYNCHER.sub("SynchedEntityData").getMappedClass();
        NMSMethod setMethod = dataClass.getMethod("set", data.getClass(), Object.class);
        setMethod.invoke(entityData, data, value);
    }

    public static void setPos(NPC npc, Vector pos) {
        invoke(npc, "setPos", Parameter.of(toVec3(pos)));
    }

    public static void respawn(NPC npc) {
        invoke(npc, "respawn");
    }

    public static void setArrowCount(NPC npc, int count) {
        invoke(npc, "setArrowCount", new Parameter(int.class, count));
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
                        Object craftPlayer = NMSReflection.getHandle(to);
                        NMSField connField = Package.SERVER_PLAYER_CLASS.getMappedClass().getField("connection");
                        Object conn = connField.getValue(craftPlayer);
                        Class<?> packetClass = Class.forName(Package.PROTOCOL.sub("Packet").toString());
                        NMSClass connClass = Package.minecraft("server.network.ServerGamePacketListenerImpl").getMappedClass();
                        connClass.getMethod("send", packetClass).invoke(conn, packetInstance);
                    } catch (NullPointerException | ClassNotFoundException e) {
                        throw new RuntimeException("Could not get handle of player", e);
                    }
                }
            };
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create packet " + className, e);
        }
    }
}
