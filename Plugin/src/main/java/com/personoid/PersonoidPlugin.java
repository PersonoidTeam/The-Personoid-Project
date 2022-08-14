package com.personoid;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.npc.NPCHandler;
import com.personoid.api.utils.bukkit.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class PersonoidPlugin extends JavaPlugin {
    private final Logger logger = Logger.get("Personoid");
    private final Map<String, NPCHandler> registries = new HashMap<>();

    @Override
    public void onEnable() {
        logger.info("Successfully loaded Personoid plugin.");
    }

    @Override
    public void onDisable() {
        logger.info("Successfully unloaded Personoid plugin.");
    }

    public void addUserPlugin(String name) {
        registries.put(name, PersonoidAPI.getRegistry());
    }
}
