package com.personoid.commands;

import com.personoid.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ConfigCommand extends Command {
    static {
        CommandManager.addCommand(new ConfigCommand());
    }

    public ConfigCommand() {
        super("command");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args[0].equalsIgnoreCase("autoReload")) {
            if (args.length == 1) {
                sender.sendMessage("Auto-reload is " + (Config.isAutoReload() ? "enabled" : "disabled"));
            } else if (args.length == 2) {
                if (args[1].equalsIgnoreCase("true")) {
                    Config.setAutoReload(true);
                    sender.sendMessage("Auto-reload is enabled");
                } else if (args[1].equalsIgnoreCase("false")) {
                    Config.setAutoReload(false);
                    sender.sendMessage("Auto-reload is disabled");
                } else {
                    sender.sendMessage("Invalid argument");
                }
            } else {
                sender.sendMessage("Invalid argument");
            }
        } else {
            sender.sendMessage("Invalid argument");
        }
        return false;
    }
}
