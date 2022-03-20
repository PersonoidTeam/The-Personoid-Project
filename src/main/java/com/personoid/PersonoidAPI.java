package com.personoid;

import com.personoid.handlers.CommandHandler;
import com.personoid.handlers.NPCHandler;
import com.personoid.listeners.Events;
import com.personoid.utils.bukkit.Message;
import org.bukkit.plugin.java.JavaPlugin;

public final class PersonoidAPI extends JavaPlugin {
    private static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        CommandHandler.registerCommands();
        getServer().getPluginManager().registerEvents(new Events(), this);
        new Message("&aPersonoid core enabled").prefix();
    }

    @Override
    public void onDisable() {
        NPCHandler.purgeNPCs();
        new Message("&cPersonoid core disabled").prefix();
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
