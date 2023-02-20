package com.personoid.commands;

import com.personoid.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args[0].equalsIgnoreCase("autoreload")) {
            if (args.length == 1) {
                sender.sendMessage("Auto-reload is " + (Config.isAutoReload() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                return true;
            } else if (args.length == 2) {
                if (args[1].equalsIgnoreCase("true")) {
                    Config.setAutoReload(true);
                    sender.sendMessage("Set auto-reload to " + ChatColor.GREEN + "enabled");
                    return true;
                } else if (args[1].equalsIgnoreCase("false")) {
                    Config.setAutoReload(false);
                    sender.sendMessage("Set auto-reload to " + ChatColor.RED + "disabled");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid argument");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid argument");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid argument");
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("autoreload");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("autoreload")) {
                return Arrays.asList("true", "false");
            }
        }
        return new ArrayList<>();
    }
}
