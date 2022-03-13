package com.notnotdoddy.personoid.utils.debug;

import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class Profiler {
    private static final Set<Type> activeMessages = new HashSet<>();

    public static void push(Type type, String message) {
        if (activeMessages.contains(type)) {
            Bukkit.broadcastMessage(message);
        }
    }

    public static void enableMessage(Type type) {
        activeMessages.add(type);
    }

    public static void disableMessage(Type type) {
        activeMessages.remove(type);
    }

    public static boolean isMessageEnabled(Type type) {
        return activeMessages.contains(type);
    }

    public enum Type {
        A_STAR,
        GOAL_SELECTION,
        NAVIGATION,
    }
}
