package com.personoid.api.utils.packet;

import com.personoid.api.utils.CacheManager;
import com.personoid.api.utils.Parameter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReflectionUtils {
    private static final CacheManager CACHE = new CacheManager("reflection_utils");
    private static String version;

    public static Class<?> getClass(Packages packageType, String className) {
        return getClass(packageType.getPackageName(), className);
    }

    public static Class<?> getClass(String packageName, String className) {
        try {
            if (CACHE.contains(packageName + "." + className)) {
                return CACHE.getClass(packageName + "." + className);
            }
            Class<?> clazz = Class.forName(packageName + "." + className);
            CACHE.put(className, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getNMSClass(String packageName, String className) throws ClassNotFoundException {
        if (packageName == null) {
            return Class.forName("net.minecraft.server." + getVersion() + "." + className);
        } else {
            return Class.forName("net.minecraft.server." + getVersion() + "." + packageName + "." + className);
        }
    }

    public static Class<?> getNMSClassNoVer(String packageName, String className) throws ClassNotFoundException {
        if (packageName == null) {
            return Class.forName("net.minecraft.server." + className);
        } else {
            return Class.forName("net.minecraft.server." + packageName + "." + className);
        }
    }

    public static Class<?> getNMSClass(String oldPackageName, String newPackageName, String className, int minVerPackageChange) {
        try {
            if (Integer.parseInt(Objects.requireNonNull(getVersion()).split("_")[1]) >= minVerPackageChange) {
                if (newPackageName == null) {
                    return Class.forName("net.minecraft.server." + className);
                } else {
                    return Class.forName("net.minecraft.server." + newPackageName + "." + className);
                }
            } else {
                if (oldPackageName == null) {
                    return Class.forName("net.minecraft.server." + getVersion() + "." + className);
                } else {
                    return Class.forName("net.minecraft.server." + getVersion() + "." + oldPackageName + "." + className);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getCraftClass(String packageName, String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + packageName + "." + className);
    }

    @Nonnull
    public static <T> T getMethod(Object obj, String methodName) {
        try {
            return (T) obj.getClass().getMethod(methodName).invoke(obj);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        throw new RuntimeException("Failed to get NMS base for " + obj.getClass().getName() + " with method " + methodName);
    }

    public static Packet createPacket(String className, Parameter... parameters) throws ClassNotFoundException {
        Class<?> packetClass = null;
        for (String subPackage : getSubPackages(Packages.PACKETS.toString())) {
            try {
                packetClass = Class.forName(Packages.PACKETS + subPackage + "." + className);
            } catch (ClassNotFoundException ignored) {}
        }
        if (packetClass == null) {
            throw new ClassNotFoundException("Could not find packet " + className + " in sub packages");
        }
        try {
            Class<?>[] types = Arrays.stream(parameters).map(Parameter::getType).toArray(Class<?>[]::new);
            Object[] args = Arrays.stream(parameters).map(Parameter::getValue).toArray();
            Object packetInstance = packetClass.getConstructor(types).newInstance(args);
            return new Packet() {
                @Override
                public void send(Player to) {
                    try {
                        Object craftPlayer = to.getClass().getMethod("getHandle").invoke(to);
                        Field connField = craftPlayer.getClass().getField("b");
                        connField.setAccessible(true);
                        Object connection = connField.get(craftPlayer);
                        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.Packet");
                        connection.getClass().getMethod("a", packetClass).invoke(connection, packetInstance);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
                        throw new RuntimeException("Could not get handle of player", e);
                    }
                }
            };
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create packet " + className, e);
        }
    }

    public static Object getEntityPlayer(Player player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get handle of player", e);
        }
    }

    public static Object getNMSEntity(Entity player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get handle of entity", e);
        }
    }

    public static Object getItemStack(ItemStack itemStack) {
        try {
            return itemStack.getClass().getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get NMS item stack", e);
        }
    }

    public static Object getEquipmentSlot(EquipmentSlot slot) {
        try {
            return getClass(Packages.WORLD, "EnumItemSlot").getMethod("valueOf", String.class).invoke(null, slot.name());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get NMS equipment slot", e);
        }
    }

    private static List<String> getSubPackages(String prefix) {
        // get all sub packages in the given package
        return Arrays.stream(Packages.values())
                .filter(p -> p.toString().startsWith(prefix))
                .map(p -> p.toString().substring(prefix.length()))
                .collect(Collectors.toList());
    }

    public static String getVersion() {
        if (version != null) return version;
        try {
            return version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static int getVersionInt() {
        if (version != null) return Integer.parseInt(version.split("_")[1]);
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            return Integer.parseInt(version.split("_")[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }
}
