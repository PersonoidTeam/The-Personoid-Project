package com.personoid.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor, TabExecutor {
    private static final List<Command> commands = new ArrayList<>();

    public static void registerCommands() {
        commands.add(new ConfigCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        for (Command cmd : commands) {
            if (cmd.getName().equalsIgnoreCase(args[0])) {
                return cmd.execute(sender, args[0], newArgs);
            }
        }
        return false;
    }

    @Nullable @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (Command cmd : commands) {
                list.add(cmd.getName());
            }
            return list;
        }
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        for (Command cmd : commands) {
            if (cmd.getName().equalsIgnoreCase(args[0])) {
                return cmd.tabComplete(sender, args[0], newArgs);
            }
        }
        return new ArrayList<>();
    }
}
