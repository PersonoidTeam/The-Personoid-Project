package us.notnotdoddy.personoid.utils;

import me.definedoddy.fluidapi.FluidMessage;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class DebugMessage {
    private static final List<String> keys = new ArrayList<>();
    private static boolean console;

    public static void attemptMessage(String key, String string) {
        if (keys.contains(key)) {
            new FluidMessage("&c[DEBUG: " + key.toUpperCase() + "] &r" + string, FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
            if (console) {
                new FluidMessage("&c[DEBUG: " + key.toUpperCase() + "] &r" + string).send();
            }
        }
    }

    public static void attemptMessage(String string) {
        attemptMessage("default", string);
    }

    public static void addKey(String key) {
        if (key.equals("all")) {
            keys.addAll(List.of("all", "default", "other", "food", "resource", "goal"));
        } else {
            keys.add(key);
        }
    }

    public static void removeKey(String key) {
        if (key.equals("all")) {
            keys.removeAll(List.of("all", "default", "other", "food", "resource", "goal"));
        } else {
            keys.remove(key);
        }
    }

    public static boolean isKeyActive(String key) {
        return keys.contains(key);
    }

    public static void toggleConsole() {
        console = !console;
    }

    public static void enableConsole() {
        console = true;
    }

    public static void disableConsole() {
        console = false;
    }

    public static boolean console() {
        return console;
    }
}
