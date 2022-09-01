package com.personoid.api.utils.packet;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class NMSUtils {
    private static String version;

    public static String getVersion() {
        if (version != null) return version;
        try {
            return version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static ServerPlayer getServerPlayer(Player player) {
        return switch (Objects.requireNonNull(getVersion()).split("_R")[0]) {
            case "v1_18" -> ((org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer) player).getHandle();
            case "v1_19" -> ((org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer) player).getHandle();
            default -> null;
        };
    }

    public static net.minecraft.world.entity.Entity getEntity(Entity entity) {
        return switch (Objects.requireNonNull(getVersion()).split("_R")[0]) {
            case "v1_18" -> ((org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity) entity).getHandle();
            case "v1_19" -> ((org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity) entity).getHandle();
            default -> null;
        };
    }

    public static net.minecraft.world.item.ItemStack getItemStack(ItemStack itemStack) {
        return switch (Objects.requireNonNull(getVersion()).split("_R")[0]) {
            case "v1_18" -> org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack.asNMSCopy(itemStack);
            case "v1_19" -> org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack.asNMSCopy(itemStack);
            default -> null;
        };
    }

    public static net.minecraft.world.entity.EquipmentSlot getSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> net.minecraft.world.entity.EquipmentSlot.MAINHAND;
            case OFF_HAND -> net.minecraft.world.entity.EquipmentSlot.OFFHAND;
            case FEET -> net.minecraft.world.entity.EquipmentSlot.FEET;
            case LEGS -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case CHEST -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case HEAD -> net.minecraft.world.entity.EquipmentSlot.HEAD;
        };
    }
}
