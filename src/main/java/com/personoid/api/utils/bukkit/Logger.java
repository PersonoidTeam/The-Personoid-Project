package com.personoid.api.utils.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Color;
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
        sender.sendMessage("[" + logger + "] " + level.color + message);
    }

    public void info(String message) {
        sender.sendMessage("[" + logger + "] " + Level.INFO.color + message);
    }

    public void warning(String message) {
        sender.sendMessage("[" + logger + "] " + Level.WARNING.color + message);
    }

    public void severe(String message) {
        sender.sendMessage("[" + logger + "] " + Level.SEVERE.color + message);
    }

    public void success(String message) {
        sender.sendMessage("[" + logger + "] " + Level.SUCCESS.color + message);
    }

    public enum Level {
        INFO(Color.GRAY),
        WARNING(Color.ORANGE),
        SEVERE(Color.RED),
        SUCCESS(Color.GREEN);

        private final Color color;

        Level(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }
}
