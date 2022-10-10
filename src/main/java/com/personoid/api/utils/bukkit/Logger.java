package com.personoid.api.utils.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class Logger {
    private final String logger;
    private final ConsoleCommandSender sender = Bukkit.getConsoleSender();

    public Logger(String logger) {
        this.logger = logger;
    }

    public static Logger get(String logger) {
        return new Logger(logger);
    }

    public void log(Level level, String message) {
        sender.sendMessage("[" + logger + "] " + level + message);
    }

    public void info(String message) {
        sender.sendMessage("[" + logger + "] " + Level.INFO + message);
    }

    public void warning(String message) {
        sender.sendMessage("[" + logger + "] " + Level.WARNING + message);
    }

    public void severe(String message) {
        sender.sendMessage("[" + logger + "] " + Level.SEVERE + message);
    }

    public void success(String message) {
        sender.sendMessage("[" + logger + "] " + Level.SUCCESS + message);
    }

    public enum Level {
        INFO(ChatColor.GRAY),
        WARNING(ChatColor.YELLOW),
        SEVERE(ChatColor.RED),
        SUCCESS(ChatColor.GREEN);

        private final ChatColor color;

        Level(ChatColor color) {
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }

        @Override
        public String toString() {
            return color.toString();
        }
    }
}
