package us.notnotdoddy.personoid.utils;

import org.bukkit.Bukkit;

public class DebugMessage {

    public static boolean enabled = false;

    public static void attemptMessage(String string){
        if (enabled){
            Bukkit.broadcastMessage(string);
        }
    }
}
