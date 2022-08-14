package com.personoid.api.utils.packet;

import com.personoid.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class Packet {
    public void send() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    public void send(NPC from) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getLocation().distance(from.getLocation()) < 144) {
                send(player);
            }
        });
    }

    public void send(Player... to) {
        for (Player player : to) {
            send(player);
        }
    }

    protected void send(Player to) {}
}
