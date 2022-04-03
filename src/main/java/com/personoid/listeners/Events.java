package com.personoid.listeners;

import com.personoid.events.NPCPickupItemEvent;
import com.personoid.handlers.NPCHandler;
import com.personoid.npc.NPC;
import com.personoid.npc.ai.messaging.MessageManager;
import com.personoid.utils.LocationUtils;
import com.personoid.utils.bukkit.Task;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicReference;

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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        NPC npc = LocationUtils.getClosestNPC(player.getLocation());
        MessageManager messageManager = npc.getNPCBrain().getMessageManager();
        new Task(() -> {
            String response = messageManager.getResponseFrom(event.getMessage(), event.getPlayer().getName());
            npc.sendMessage(response);
        }).async().run(20); // TODO: message delay based on message length
    }

    @EventHandler
    public void onNPCPickupItem(NPCPickupItemEvent event) {
        if (event.getThrower() != null && event.getThrower() instanceof Player player) {
            ItemStack item = event.getItem().getItemStack();
            MessageManager messageManager = event.getNPC().getNPCBrain().getMessageManager();
            String npcName = event.getNPC().getName().getString();
            AtomicReference<String> response = new AtomicReference<>("");
            new Task(() -> {
                String msg = player.getDisplayName() + " gave " + npcName + " x" + item.getAmount() + " " +
                        item.getType().name().toLowerCase().replace("_", " ");
                response.set(messageManager.getResponse(msg));
            }).async();
            new Task(() -> event.getNPC().getBukkitEntity().performCommand("msg " + player.getDisplayName() + " " + response)).run(20);
            // TODO: message delay based on message length
        }
    }
}
