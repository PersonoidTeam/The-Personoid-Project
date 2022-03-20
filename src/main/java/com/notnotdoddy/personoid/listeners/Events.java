package com.notnotdoddy.personoid.listeners;

import com.notnotdoddy.personoid.handlers.NPCHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NPCHandler.getNPCs().forEach(npc -> npc.show(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        NPCHandler.getNPCs().forEach(npc -> npc.hide(player));
    }
}
