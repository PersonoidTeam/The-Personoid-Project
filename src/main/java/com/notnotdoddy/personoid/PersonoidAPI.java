package com.notnotdoddy.personoid;

import com.notnotdoddy.personoid.handlers.CommandHandler;
import com.notnotdoddy.personoid.handlers.NPCHandler;
import com.notnotdoddy.personoid.listeners.Events;
import com.notnotdoddy.personoid.utils.bukkit.Message;
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
