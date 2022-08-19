package com.personoid;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.npc.NPCHandler;
import com.personoid.api.utils.bukkit.Logger;
import com.personoid.api.utils.bukkit.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PersonoidPlugin extends JavaPlugin {
    private final Logger LOGGER = Logger.get("Personoid");
    private final Map<String, NPCHandler> registries = new HashMap<>();

    @Override
    public void onEnable() {
        LOGGER.info("Successfully loaded Personoid plugin.");
        initReloader();
    }

    @Override
    public void onDisable() {
        LOGGER.info("Successfully unloaded Personoid plugin.");
    }

    public void addUserPlugin(String name) {
        registries.put(name, PersonoidAPI.getRegistry());
    }

    public void initReloader() {
        File file = new File("plugins/Personoid-1.0.0.jar");
        long lastModified = file.lastModified();
        new Task(() -> {
            if (file.lastModified() != lastModified) {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "[Personoid] " + ChatColor.GREEN + "Plugin modified, reloading...");
                Bukkit.reload();
            }
        }, this).async().repeat(0, 20);
    }
}
