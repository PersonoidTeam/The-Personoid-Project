package com.personoid;

import com.personoid.handlers.CommandHandler;
import com.personoid.handlers.NPCHandler;
import com.personoid.listeners.Events;
import com.personoid.utils.bukkit.Message;
import com.personoid.utils.bukkit.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PersonoidAPI extends JavaPlugin {
    private static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        Message.setPrefix("&d[Personoid]&r");
        CommandHandler.registerCommands();
        getServer().getPluginManager().registerEvents(new Events(), this);
        initReloader();
        new Message("&aPersonoid core enabled").prefix().send();
    }

    @Override
    public void onDisable() {
        NPCHandler.purgeNPCs();
        new Message("&cPersonoid core disabled").prefix().send();
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public void initReloader() {
        File file = new File("plugins/Personoid-1.0.0-remapped.jar");
        Bukkit.broadcastMessage(file.getPath());
        long lastModified = file.lastModified();
        new Task(() -> {
            if (file.lastModified() != lastModified) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        "&d[Personoid]&a File modified, reloading..."));
                Bukkit.reload();
            }
        }).async().repeat(0, 20);
    }
}
