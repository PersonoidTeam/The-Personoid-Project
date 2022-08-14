package com.personoid.api;

import com.personoid.PersonoidPlugin;
import com.personoid.api.npc.NPCHandler;
import com.personoid.api.utils.bukkit.Logger;
import com.personoid.v1_18_R2.NPCHandler_1_18_R2;
import com.personoid.v1_19_R1.NPCHandler_1_19_R1;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class PersonoidAPI {
    private static NPCHandler npcHandler;
    private static String version;
    private static Plugin plugin;
    private static final Logger logger = Logger.get("Personoid");

    static {
        Plugin userPlugin = JavaPlugin.getProvidingPlugin(PersonoidAPI.class);
        if (userPlugin.getClass() != PersonoidPlugin.class) {
            PersonoidPlugin basePlugin = (PersonoidPlugin) Bukkit.getPluginManager().getPlugin("Personoid");
            if (basePlugin != null) basePlugin.addUserPlugin(userPlugin.getName());
        }
        if (PersonoidAPI.plugin == null) {
            logger.info("Registered " + userPlugin.getName() + ".");
        }
    }

    public static NPCHandler getRegistry() {
        if (npcHandler != null) return npcHandler;
        return npcHandler = switch (Objects.requireNonNull(getVersion())) {
            case "v1_18_R2" -> new NPCHandler_1_18_R2();
            case "v1_19_R1" -> new NPCHandler_1_19_R1();
            default -> null;
        };
    }

    private static String getVersion() {
        if (version != null) return version;
        try {
            return version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
