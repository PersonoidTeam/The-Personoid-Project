package com.personoid.api.utils.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Packets {
    private static String version;

    private static ServerPlayer getServerPlayer(Player player) {
        return switch (Objects.requireNonNull(getVersion()).split("_R")[0]) {
            case "v1_18" -> ((org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer) player).getHandle();
            case "v1_19" -> ((org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer) player).getHandle();
            default -> null;
        };
    }

    private static net.minecraft.world.entity.Entity getEntity(Entity entity) {
        return switch (Objects.requireNonNull(getVersion()).split("_R")[0]) {
            case "v1_18" -> ((org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity) entity).getHandle();
            case "v1_19" -> ((org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity) entity).getHandle();
            default -> null;
        };
    }

    private static String getVersion() {
        if (version != null) return version;
        try {
            return version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static Packet addPlayer(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        ClientboundPlayerInfoPacket info = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer);
        ClientboundAddPlayerPacket add = new ClientboundAddPlayerPacket(serverPlayer);
        ClientboundSetEntityDataPacket data = new ClientboundSetEntityDataPacket(serverPlayer.getId(), serverPlayer.getEntityData(), true);
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                conn.send(info);
                conn.send(add);
                conn.send(data);
            }
        };
    }

    public static Packet removePlayer(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        ClientboundPlayerInfoPacket info = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer);
        ClientboundRemoveEntitiesPacket remove = new ClientboundRemoveEntitiesPacket(serverPlayer.getId());
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                conn.send(info);
                conn.send(remove);
            }
        };
    }

    public static Packet entityTakeItem(int itemId, int entityId, int amount) {
        ClientboundTakeItemEntityPacket packet = new ClientboundTakeItemEntityPacket(itemId, entityId, amount);
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                conn.send(packet);
            }
        };
    }

    public static Packet blockDestruction(int breakerId, Location location, int stage) {
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(breakerId, blockPos, stage);
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                conn.send(packet);
            }
        };
    }

    public static Packet rotateEntity(Entity entity, float yaw, float pitch) {
        byte yawByte = (byte) ((yaw % 360) * 256 / 360);
        byte pitchByte = (byte) ((pitch % 360) * 256 / 360);
        ClientboundRotateHeadPacket rotateHead = new ClientboundRotateHeadPacket(getEntity(entity), yawByte);
        ClientboundMoveEntityPacket.Rot rotateEntity = new ClientboundMoveEntityPacket.Rot(entity.getEntityId(), yawByte, pitchByte, false);
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                conn.send(rotateHead);
                //conn.send(rotateEntity);
            }
        };
    }
}
