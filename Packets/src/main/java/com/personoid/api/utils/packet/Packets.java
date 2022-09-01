package com.personoid.api.utils.packet;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Packets {
    public static Packet addPlayer(Player player) {
        ServerPlayer serverPlayer = NMSUtils.getServerPlayer(player);
        ClientboundPlayerInfoPacket info = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer);
        ClientboundAddPlayerPacket add = new ClientboundAddPlayerPacket(serverPlayer);
        ClientboundSetEntityDataPacket data = new ClientboundSetEntityDataPacket(serverPlayer.getId(), serverPlayer.getEntityData(), true);
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = NMSUtils.getServerPlayer(to).connection;
                conn.send(info);
                conn.send(add);
                conn.send(data);
            }
        };
    }

    public static Packet removePlayer(Player player) {
        ServerPlayer serverPlayer = NMSUtils.getServerPlayer(player);
        ClientboundPlayerInfoPacket info = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer);
        ClientboundRemoveEntitiesPacket remove = new ClientboundRemoveEntitiesPacket(serverPlayer.getId());
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = NMSUtils.getServerPlayer(to).connection;
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
                ServerGamePacketListenerImpl conn = NMSUtils.getServerPlayer(to).connection;
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
                ServerGamePacketListenerImpl conn = NMSUtils.getServerPlayer(to).connection;
                conn.send(packet);
            }
        };
    }

    public static Packet rotateEntity(Entity entity, float yaw, float pitch) {
        byte yawByte = (byte) ((yaw % 360) * 256 / 360);
        byte pitchByte = (byte) ((pitch % 360) * 256 / 360);
        ClientboundRotateHeadPacket rotateHead = new ClientboundRotateHeadPacket(NMSUtils.getEntity(entity), yawByte);
        ClientboundMoveEntityPacket.Rot rotateEntity = new ClientboundMoveEntityPacket.Rot(entity.getEntityId(), yawByte, pitchByte, false);
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = NMSUtils.getServerPlayer(to).connection;
                conn.send(rotateHead);
                //conn.send(rotateEntity);
            }
        };
    }

    public static Packet setEntityData(int entityId, SynchedEntityData data, boolean value) {
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entityId, data, value);
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = NMSUtils.getServerPlayer(to).connection;
                conn.send(packet);
            }
        };
    }

    public static Packet entityEquipment(int entityId, Map<EquipmentSlot, ItemStack> equipment) {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> list = new ArrayList<>();
        equipment.forEach((slot, item) -> list.add(Pair.of(NMSUtils.getSlot(slot), NMSUtils.getItemStack(item))));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, list);
        return new Packet() {
            @Override
            public void send(Player to) {
                ServerGamePacketListenerImpl conn = NMSUtils.getServerPlayer(to).connection;
                conn.send(packet);
            }
        };
    }
}
