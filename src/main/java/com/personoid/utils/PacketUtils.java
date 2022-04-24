package com.personoid.utils;

import com.personoid.npc.NPC;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class PacketUtils {
    public static void send(Packet<?>... packets) {
        for (Packet<?> packet : packets) {
            try {
                Bukkit.getOnlinePlayers().forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packet));
            } catch (Exception ignored) { }
        }
    }

    // sends packets to all players in range of npc/can see npc
    public static void send(NPC npc, Packet<?>... packets) {
        try {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (npc.getLocation().distance(player.getLocation()) > 144) return; // TODO: tweak range value
                for (Packet<?> packet : packets) {
                    ((CraftPlayer) player).getHandle().connection.send(packet);
                }
            });
        } catch (Exception ignored) { }
    }

    public static void send(List<Player> players, Packet<?>... packets) {
        for (Packet<?> packet : packets) {
            try {
                players.forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packet));
            } catch (Exception ignored) { }
        }
    }
}
