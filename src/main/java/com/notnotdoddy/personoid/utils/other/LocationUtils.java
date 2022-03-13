package com.notnotdoddy.personoid.utils.other;

import com.notnotdoddy.personoid.handlers.NPCHandler;
import com.notnotdoddy.personoid.npc.NPC;
import org.bukkit.Location;

public class LocationUtils {
    public static NPC getClosestNPC(Location location) {
        NPC closestNPC = null;
        for (NPC npc : NPCHandler.getNPCs()) {
            if (closestNPC == null || npc.getLocation().distance(location) < closestNPC.getLocation().distance(location)) {
                closestNPC = npc;
            }
        }
        return closestNPC;
    }
}
