package com.personoid.api.utils.packet;

import java.util.function.BiFunction;

public enum Packages {
    CRAFTBUKKIT((version, vInt) -> "org.bukkit.craftbukkit"),
    SERVER((version, vInt) -> "net.minecraft.server"),
    SERVER_VERSION((version, vInt) -> SERVER + "." + version),
    PACKETS((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : "net.minecraft.protocol"),
    NETWORK((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : "net.minecraft.network"),
    CORE((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : "net.minecraft.core"),
    LEVEL((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : SERVER + ".level"),
    WORLD((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : SERVER + ".world"),
    ;

    private final String packageName;

    Packages(BiFunction<String, Integer, String> packageName) {
        this.packageName = packageName.apply(ReflectionUtils.getVersion(), ReflectionUtils.getVersionInt());
    }

    public String getPackageName() {
        return packageName;
    }

    public String plus(String packageName) {
        return this.packageName + "." + packageName;
    }

    @Override
    public String toString() {
        return packageName;
    }
}
