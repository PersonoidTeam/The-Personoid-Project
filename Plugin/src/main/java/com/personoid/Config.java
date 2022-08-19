package com.personoid;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Config {
    private static final JavaPlugin plugin = PersonoidPlugin.getPlugin(PersonoidPlugin.class);
    private static boolean autoReload;

    static {
        reload();
    }

    public static void reset() {
        autoReload = true;
    }

    public static boolean doesConfigExist() {
        return new File("plugins/Personoid/config.yml").exists();
    }

    public static void reload() {
        if (!doesConfigExist()) reset();
        autoReload = plugin.getConfig().getBoolean("auto-reload");
    }

    public static boolean isAutoReload() {
        return autoReload;
    }

    public static void setAutoReload(boolean value) {
        plugin.getConfig().set("auto-reload", value);
        plugin.saveConfig();
        reload();
    }
}
