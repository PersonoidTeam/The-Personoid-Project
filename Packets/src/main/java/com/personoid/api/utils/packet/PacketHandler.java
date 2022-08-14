package com.personoid.api.utils.packet;

import com.personoid.v1_18_R2.Utils_1_18_R2;
import com.personoid.v1_19_R1.Utils_1_19_R1;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PacketHandler {
    private static String version;

    private static ServerPlayer getServerPlayer(Player player) {
        return switch (Objects.requireNonNull(getVersion())) {
            case "v1_18_R2" -> Utils_1_19_R1.getConnection(player);
            case "v1_19_R1" -> Utils_1_18_R2.getServerPlayer(player);
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
            public void send(Player... players) {
                for (Player to : players) {
                    ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                    conn.send(info);
                    conn.send(add);
                    conn.send(data);
                }
            }

            @Override
            public void send() {
                Bukkit.getOnlinePlayers().forEach(to -> {
                    ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                    conn.send(info);
                    conn.send(add);
                    conn.send(data);
                });
            }
        };
    }

    public static Packet removePlayer(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        ClientboundPlayerInfoPacket info = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer);
        ClientboundRemoveEntitiesPacket remove = new ClientboundRemoveEntitiesPacket(serverPlayer.getId());
        return new Packet() {
            @Override
            public void send(Player... players) {
                for (Player to : players) {
                    ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                    conn.send(info);
                    conn.send(remove);
                }
            }

            @Override
            public void send() {
                Bukkit.getOnlinePlayers().forEach(to -> {
                    ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                    conn.send(info);
                    conn.send(remove);
                });
            }
        };
    }

    public static Packet entityTakeItem(int itemId, int entityId, int amount) {
        ClientboundTakeItemEntityPacket packet = new ClientboundTakeItemEntityPacket(itemId, entityId, amount);
        return new Packet() {
            @Override
            public void send(Player... players) {
                for (Player to : players) {
                    ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                    conn.send(packet);
                }
            }

            @Override
            public void send() {
                Bukkit.getOnlinePlayers().forEach(to -> {
                    ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                    conn.send(packet);
                });
            }
        };
    }

    public static Packet blockDestruction(int breakerId, Location location, int stage) {
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(breakerId, blockPos, stage);
        return new Packet() {
            @Override
            public void send(Player... players) {
                for (Player to : players) {
                    ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                    conn.send(packet);
                }
            }

            @Override
            public void send() {
                Bukkit.getOnlinePlayers().forEach(to -> {
                    ServerGamePacketListenerImpl conn = getServerPlayer(to).connection;
                    conn.send(packet);
                });
            }
        };
    }
}
