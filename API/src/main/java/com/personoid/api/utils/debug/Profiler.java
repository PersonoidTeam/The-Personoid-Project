package com.personoid.api.utils.debug;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public enum Profiler {
    PATHFINDING(ChatColor.LIGHT_PURPLE),
    ACTIVITIES(ChatColor.GREEN),
    NAVIGATION(ChatColor.AQUA);

    boolean enabled;
    final ChatColor color;
    String display;

    Profiler(ChatColor color) {
        this.color = color;
        display = name().toLowerCase().replace("_", " ");
        display = display.substring(0, 1).toUpperCase() + this.display.substring(1);
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void push(String message) {
        if (enabled) {
            Bukkit.broadcastMessage(ChatColor.RED + "[Profiler] " + color + "[" + display + "] " + ChatColor.GRAY + message);
        }
    }
}
