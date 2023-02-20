package com.personoid.api;

import com.personoid.PersonoidPlugin;
import com.personoid.api.npc.NPCRegistry;
import com.personoid.api.utils.bukkit.Logger;

public class PersonoidAPI {
    private static final Logger LOGGER = Logger.get("Personoid");
    private static final Logger LOGGER_REG = Logger.get("Personoid Registry");
    private static NPCRegistry registry;
    private static PersonoidPlugin basePlugin;

/*    static {
        PersonoidPlugin basePlugin = (PersonoidPlugin) Bukkit.getPluginManager().getPlugin("Personoid");
        Plugin providingPlugin = JavaPlugin.getProvidingPlugin(PersonoidAPI.class);
        if (basePlugin != null) {
            PersonoidAPI.basePlugin = basePlugin;
            basePlugin.addProvidingPlugin(providingPlugin.getName());
            LOGGER_REG.info("Located Personoid plugin, using it as registry");
        } else {
            LOGGER_REG.info("Personoid plugin not found, using shaded API as registry");
        }
        if (PersonoidAPI.basePlugin != null) {
            LOGGER.info("Registered internal binding: " + providingPlugin.getName());
        } else {
            LOGGER.info("Registered external binding: " + providingPlugin.getName());
        }
    }*/

    public static NPCRegistry getRegistry() {
        if (registry != null) return registry;
        return registry = basePlugin != null ? basePlugin.getBaseRegistry() : new NPCRegistry();
    }

    public static NPCRegistry getRegistry(String name) {
        if (basePlugin == null) {
            throw new IllegalStateException("Cannot get registry for " + name + " because the base plugin cannot be found.");
        }
        return basePlugin.getRegistry(name);
    }
}
