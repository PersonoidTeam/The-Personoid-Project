package com.personoid.api.utils.packet;

public class ReflectionUtils {
    public static Class<?> getNMSClass(String packageName, String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + NMSUtils.getVersion() + "." + packageName + "." + className);
    }

    public static Class<?> getCraftClass(String packageName, String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + NMSUtils.getVersion() + "." + packageName + "." + className);
    }
}
