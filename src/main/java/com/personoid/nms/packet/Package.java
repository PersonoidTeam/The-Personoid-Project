package com.personoid.nms.packet;

import com.personoid.nms.mappings.Mappings;
import com.personoid.nms.mappings.NMSClass;

public class Package {
    public static final Package PROTOCOL = minecraft("network.protocol");
    public static final Package ENTITY = minecraft("world.entity");

    public static final Package LIVING_ENTITY_CLASS = minecraft("world.entity.LivingEntity");
    public static final Package ITEM_STACK_CLASS = minecraft("world.item.ItemStack");
    public static final Package SERVER_PLAYER_CLASS = minecraft("server.level.ServerPlayer");
    public static final Package PLAYER_CLASS = minecraft("world.entity.player.Player");
    public static final Package ENTITY_CLASS = minecraft("world.entity.Entity");
    public static final Package ENTITY_DATA_CLASS = minecraft("network.syncher.SynchedEntityData");

    private final String packageName;

    public Package(String packageName) {
        this.packageName  = packageName;
    }

    public static Package minecraft(String packageName) {
        return new Package("net.minecraft." + packageName);
    }

    public static Package mojang(String packageName) {
        return new Package("com.mojang." + packageName);
    }

    public Package sub(String subPackage) {
        return new Package(packageName + "." + subPackage);
    }

    public NMSClass getMappedClass() {
        return Mappings.get().getClassFromMojang(packageName);
    }

    public Class<?> getRawClass() {
        return getMappedClass().getRawClass();
    }

    @Override
    public String toString() {
        return packageName;
    }
}
