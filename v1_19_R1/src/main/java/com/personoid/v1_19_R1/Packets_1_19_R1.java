package com.personoid.v1_19_R1;

import com.personoid.api.utils.packet.Packets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Packets_1_19_R1 {
    public static class AddPlayer extends Packets.AddPlayer {
        public AddPlayer(Player player) {
            super(player);
        }

        @Override
        public void send(Player to) {
            ServerPlayer serverPlayer = ((CraftPlayer) getPlayer()).getHandle();
            ClientboundPlayerInfoPacket info = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer);
            ClientboundAddPlayerPacket add = new ClientboundAddPlayerPacket(serverPlayer);
            ClientboundSetEntityDataPacket data = new ClientboundSetEntityDataPacket(serverPlayer.getId(), serverPlayer.getEntityData(), true);
            ServerGamePacketListenerImpl conn = ((CraftPlayer)to).getHandle().connection;
            conn.send(info);
            conn.send(add);
            conn.send(data);
        }
    }

    public static class RemovePlayer extends Packets.RemovePlayer {
        public RemovePlayer(Player player) {
            super(player);
        }

        @Override
        public void send(Player to) {
            ServerPlayer serverPlayer = ((CraftPlayer) getPlayer()).getHandle();
            ClientboundPlayerInfoPacket info = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer);
            ClientboundRemoveEntitiesPacket remove = new ClientboundRemoveEntitiesPacket(serverPlayer.getId());
            ServerGamePacketListenerImpl conn = ((CraftPlayer)to).getHandle().connection;
            conn.send(info);
            conn.send(remove);
        }
    }

    public static class EntityTakeItem extends Packets.EntityTakeItem {
        public EntityTakeItem(int itemId, int EntityId, int amount) {
            super(itemId, EntityId, amount);
        }

        @Override
        public void send(Player to) {
            ClientboundTakeItemEntityPacket packet = new ClientboundTakeItemEntityPacket(itemId, entityId, amount);
            ServerGamePacketListenerImpl conn = ((CraftPlayer)to).getHandle().connection;
            conn.send(packet);
        }
    }

    public static class BlockDestruction extends Packets.BlockDestruction {
        public BlockDestruction(int breakerId, Location location, int stage) {
            super(breakerId, location, stage);
        }

        @Override
        public void send(Player to) {
            BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(breakerId, blockPos, stage);
            ServerGamePacketListenerImpl conn = ((CraftPlayer)to).getHandle().connection;
            conn.send(packet);
        }
    }
}
