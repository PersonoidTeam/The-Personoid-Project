package com.personoid.api.utils.types;

import org.bukkit.Material;

public enum LogType {
    OAK(Material.OAK_LOG),
    DARK_OAK(Material.DARK_OAK_LOG),
    BIRCH(Material.BIRCH_LOG),
    SPRUCE(Material.SPRUCE_LOG),
    JUNGLE(Material.JUNGLE_LOG),
    ACACIA(Material.ACACIA_LOG),
    ;

    public final Material base;

    LogType(Material material){
        base = material;
    }

    public Material getBase() {
        return base;
    }
}
