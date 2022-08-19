package com.personoid.commands;

import com.personoid.PersonoidPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class CommandManager implements CommandExecutor {
    private static final JavaPlugin plugin = PersonoidPlugin.getPlugin(PersonoidPlugin.class);
    private static final CommandManager instance = new CommandManager();

    public static void addCommand(Command command) {
        plugin.getCommand(command.getName()).setExecutor(instance);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return command.execute(sender, label, args);
    }
}
