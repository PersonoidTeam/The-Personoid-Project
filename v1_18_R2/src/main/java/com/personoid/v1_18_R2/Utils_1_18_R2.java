package com.personoid.v1_18_R2;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Utils_1_18_R2 {
    public static ServerPlayer getServerPlayer(Player player) {
        return ((CraftPlayer)player).getHandle();
    }
}
