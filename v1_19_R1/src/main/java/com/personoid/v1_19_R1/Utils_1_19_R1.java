package com.personoid.v1_19_R1;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Utils_1_19_R1 {
    public static ServerPlayer getConnection(Player player) {
        return ((CraftPlayer)player).getHandle();
    }
}
