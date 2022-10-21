package com.personoid.api.utils.packet;

import java.util.function.BiFunction;

public enum Packages {
    CRAFTBUKKIT((version, vInt) -> "org.bukkit.craftbukkit"),
    SERVER((version, vInt) -> "net.minecraft.server"),
    WORLD((version, vInt) -> "net.minecraft.world"),
    SERVER_VERSION((version, vInt) -> SERVER + "." + version),
    SERVER_VERSION_MOD((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : SERVER.toString()),
    WORLD_VERSION((version, vInt) -> WORLD + "." + version),
    PACKETS((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : "net.minecraft.network.protocol"),
    NETWORK((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : "net.minecraft.network"),
    SERVER_NETWORK((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : "net.minecraft.server.network"),
    CORE((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : "net.minecraft.core"),
    SERVER_LEVEL((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : SERVER + ".level"),
    SERVER_WORLD((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : SERVER + ".world"),
    AUTH_LIB((version, vInt) -> "com.mojang.authlib"),
    PLAYER((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : WORLD + ".entity.player"),
    ENTITY((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : WORLD + ".entity"),
    DAMAGE_SOURCE((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : WORLD + ".damagesource"),
    ITEM_SLOT((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : WORLD + ".entity"),
    ITEM_STACK((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : WORLD + ".item"),
    CRAFT_ITEM_STACK((version, vInt) -> CRAFTBUKKIT + "." + version + ".inventory"),
    INTERACTION_HAND((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : WORLD.toString()),
    ATTRIBUTES((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : WORLD + ".entity.ai.attributes"),
    PARTICLE_TYPES((version, vInt) -> vInt < 17 ? SERVER_VERSION.toString() : CORE + ".particles"),
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
