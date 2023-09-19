package com.personoid.api.npc;

import com.personoid.nms.NMS;
import com.personoid.nms.mappings.NMSClass;
import com.personoid.nms.packet.Package;
import com.personoid.nms.packet.Packages;
import com.personoid.nms.packet.Packets;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.personoid.nms.packet.NMSReflection.*;

public class NPCRegistry {
    private static final List<NPC> NPCs = new ArrayList<>();

    public NPC createNPCInstance(String name) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        NPC npc = NPCBuilder.create(profile);
        NPCs.add(npc);
        return npc;
    }

    public NPC createNPCInstance(String name, Skin skin) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name, skin);
        NPC npc = NPCBuilder.create(profile);
        NPCs.add(npc);
        return npc;
    }

    public void respawnNPC(NPC npc, Location location) {
        //ServerPlayer sp = ((ServerPlayer)npc.getOverrides().getBase());
/*        sp.connection.send(new ClientboundRespawnPacket(sp.getLevel().dimensionTypeId(), sp.getLevel().dimension(),
                BiomeManager.obfuscateSeed(sp.getLevel().getSeed()), sp.gameMode.getGameModeForPlayer(), sp.gameMode.getPreviousGameModeForPlayer(),
                sp.getLevel().isDebug(), sp.getLevel().isFlat(), true, sp.getLastDeathLocation()));*/
        NMS.respawn(npc);
        NMS.setPos(npc, new Vector(location.getX(), location.getY(), location.getZ()));
        //sp.getLevel().addRespawnedPlayer(sp);
        npc.getEntity().setHealth(20);
        //sp.unsetRemoved();
        NMS.setArrowCount(npc, 0);
        Packets.removePlayer(npc.getEntity()).send();
        Packets.addPlayer(npc.getEntity(), npc.getProfile().isVisibleInTab()).send();
        npc.respawn();
    }

    public void spawnNPC(NPC npc, Location location) {
        NMS.setPos(npc, new Vector(location.getX(), location.getY(), location.getZ()));
        Packets.addPlayer(npc.getEntity(), npc.getProfile().isVisibleInTab()).send();
        try {
            Class<?> serverPlayerClass = findClass(Packages.SERVER_LEVEL, "EntityPlayer");
            Object serverPlayer = getHandle(npc.getEntity());
            Class<?> connClass = findClass(Packages.SERVER_NETWORK, "PlayerConnection"); // ServerGamePacketListenerImpl

            Object server = invoke(Bukkit.getServer(), "getServer");
            Class<?> minecraftServerClass = findClass(Packages.SERVER_VERSION_MOD, "MinecraftServer");

            // network manager
            NMSClass connectionClass = Package.NETWORK.sub("Connection").getMappedClass();
            String genericsFtw = connectionClass.getMethod("genericsFtw").getObfuscatedName();

            Class<?> networkManagerClassBase = findClass(Packages.NETWORK, "NetworkManager"); // Connection
            Class<?> networkManagerClass = new ByteBuddy().subclass(networkManagerClassBase)
                    .method(ElementMatchers.named(genericsFtw)).intercept(MethodCall.run(() -> {})).make()
                    .load(networkManagerClassBase.getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();

            Class<?> packetFlowClass = findClass(Packages.NETWORK.plus("protocol"), "EnumProtocolDirection");
            Object clientboundPacketFlow = getEnum(packetFlowClass, "CLIENTBOUND"); // CLIENTBOUND
            Object networkManager = networkManagerClass.getConstructor(packetFlowClass).newInstance(clientboundPacketFlow);

            // connection class
            Object conn = connClass.getConstructor(minecraftServerClass, networkManagerClassBase, serverPlayerClass)
                    .newInstance(server, networkManager, serverPlayer);

            String connName = Package.SERVER_PLAYER_CLASS.getMappedClass().getField("connection").getObfuscatedName();
            setField(serverPlayer, connName, conn);
            NMS.addToWorld(npc, location.getWorld());
            npc.getOverrides().onSpawn();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeNPC(NPC npc) {
        npc.remove();
        despawnNPC(npc);
        NMS.removeCachedData(npc);
        NPCs.remove(npc);
    }

    private void despawnNPC(NPC npc) {
        Packets.removePlayer(npc.getEntity()).send();
        NMS.remove(npc);
    }

    public NPC getNPC(String name) {
        return NPCs.stream().filter(npc -> npc.getProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<NPC> getNPCs() {
        return NPCs;
    }

    public void purgeNPCs() {
        NPCs.forEach(this::despawnNPC);
        NPCs.clear();
    }

    public boolean isNPC(Entity entity) {
        for (NPC npc : NPCs) {
            if (npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public NPC getNPC(Entity entity) {
        for (NPC npc : NPCs) {
            if (npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return npc;
            }
        }
        return null;
    }
}
