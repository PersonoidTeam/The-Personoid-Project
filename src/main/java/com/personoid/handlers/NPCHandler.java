package com.personoid.handlers;

import com.mojang.authlib.GameProfile;
import com.personoid.npc.NPC;
import com.personoid.utils.npc.PacketUtils;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPCHandler {
    private static final List<NPC> npcs = new ArrayList<>();

    public static void registerNPC(NPC npc) {
        npcs.add(npc);

        // NPC registration methods
        npc.registerActivities();
    }

    public static void unregisterNPC(NPC npc) {
        npcs.remove(npc);
    }

    public static NPC createNPCInstance(World world, String name, Player player) {
        MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
        ServerLevel level = ((CraftWorld)world).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        NPC npc = new NPC(server, level, profile, player);
        npc.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.CLIENTBOUND), npc) {
            @Override
            public void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) { }
        };
        return npc;
    }

    public static void spawnNPC(NPC npc, Location location) {
        npc.setPos(location.getX(), location.getY(), location.getZ());
        ClientboundPlayerInfoPacket playerInfoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc);
        ClientboundAddPlayerPacket addPlayerPacket = new ClientboundAddPlayerPacket(npc);
        PacketUtils.send(playerInfoPacket, addPlayerPacket);
        npc.getLevel().addNewPlayer(npc);
    }

    public static void despawnNPC(NPC npc) {
        ClientboundPlayerInfoPacket playerInfoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc);
        ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(npc.getId());
        PacketUtils.send(playerInfoPacket, removePacket);
        npc.remove();
        npc.remove(Entity.RemovalReason.DISCARDED);
    }

    public static NPC getNPC(String name) {
        for (NPC npc : npcs) {
            if (npc.getName().getString().equalsIgnoreCase(name)) {
                return npc;
            }
        }
        return null;
    }

    public static List<NPC> getNPCs() {
        return npcs;
    }

    public static void purgeNPCs() {
        for (NPC npc : npcs) {
            despawnNPC(npc);
        }
        npcs.clear();
    }
}
