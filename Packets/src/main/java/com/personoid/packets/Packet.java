package com.personoid.packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class Packet {
    public void send() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    public void send(Player... players) {
        for (Player player : players) {
            send(player);
        }
    }

    protected abstract void send(Player to);
}
