package com.personoid.utils.npc;

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

    public static void send(List<Player> players, Packet<?>... packets) {
        for (Packet<?> packet : packets) {
            try {
                players.forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packet));
            } catch (Exception ignored) { }
        }
    }
}
