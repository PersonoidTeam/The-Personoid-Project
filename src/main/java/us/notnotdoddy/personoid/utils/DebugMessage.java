package us.notnotdoddy.personoid.utils;

import org.bukkit.Bukkit;

import java.util.List;

public class DebugMessage {
    static List<String> strings = List.of(
            "default",
            "hehe",
            "alsjdkajd",
            "i was here"
    );

    public static void attemptMessage(String key, String string){
        if (strings.contains(key)){
            Bukkit.broadcastMessage(string);
        }
    }

    public static void attemptMessage(String string){
        if (strings.contains("default")){
            Bukkit.broadcastMessage(string);
        }
    }
}
