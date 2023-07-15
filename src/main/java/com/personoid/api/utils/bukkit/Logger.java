package com.personoid.api.utils.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import java.util.HashMap;

public class Logger {
    private static final HashMap<String, Logger> loggers = new HashMap<>();
    private static final ConsoleCommandSender sender = Bukkit.getConsoleSender();

    private final String logger;
    private String title = "";

    private int infoCount;
    private int warningCount;
    private int severeCount;
    private int successCount;

    public Logger(String logger) {
        this.logger = logger;
    }

    private Logger() {
        this.logger = null;

    }

    public Logger title(String title) {
        this.title = title + ": ";
        return this;
    }

    public static Logger get(String logger) {
        if (loggers.containsKey(logger)) return loggers.get(logger);
        Logger newLogger = new Logger(logger);
        loggers.put(logger, newLogger);
        return newLogger;
    }

    public static Logger get() {
        return new Logger();
    }

    public void log(Level level, String message) {
        if (logger == null) {
            sender.sendMessage(title + level + message);
        } else {
            sender.sendMessage("[" + logger + "] " + title + level + message);
        }
        switch (level) {
            case INFO:
                infoCount++;
                break;
            case WARNING:
                warningCount++;
                break;
            case SEVERE:
                severeCount++;
                break;
            case SUCCESS:
                successCount++;
                break;
        }
    }

    public void info(String message) {
        if (logger == null) {
            sender.sendMessage(title + Level.INFO + message);
        } else {
            sender.sendMessage("[" + logger + "] " + title + Level.INFO + message);
        }
        infoCount++;
    }

    public void warning(String message) {
        if (logger == null) {
            sender.sendMessage(title + Level.WARNING + message);
        } else {
            sender.sendMessage("[" + logger + "] " + title + Level.WARNING + message);
        }
        warningCount++;
    }

    public void severe(String message) {
        if (logger == null) {
            sender.sendMessage(title + Level.SEVERE + message);
        } else {
            sender.sendMessage("[" + logger + "] " + title + Level.SEVERE + message);
        }
        severeCount++;
    }

    public void success(String message) {
        if (logger == null) {
            sender.sendMessage(title + Level.SUCCESS + message);
        } else {
            sender.sendMessage("[" + logger + "] " + title + Level.SUCCESS + message);
        }
        successCount++;
    }

    public int getInfoCount() {
        return infoCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getSevereCount() {
        return severeCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getLogCount() {
        return infoCount + warningCount + severeCount + successCount;
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
