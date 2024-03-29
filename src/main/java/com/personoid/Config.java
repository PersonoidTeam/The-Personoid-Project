package com.personoid;

import com.personoid.PersonoidPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Config {
    private static final JavaPlugin plugin = PersonoidPlugin.getPlugin(PersonoidPlugin.class);
    private static boolean autoReload;

    public static void reset() {
        autoReload = false;
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
