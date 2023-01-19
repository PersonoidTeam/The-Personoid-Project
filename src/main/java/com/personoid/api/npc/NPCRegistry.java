package com.personoid.api.npc;

import com.personoid.nms.NMSBridge;
import com.personoid.nms.packet.Packages;
import com.personoid.nms.packet.Packets;
import com.personoid.nms.NPCBuilder;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.personoid.nms.packet.ReflectionUtils.*;

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
        NMSBridge.respawn(npc);
        NMSBridge.setPos(npc, new Vector(location.getX(), location.getY(), location.getZ()));
        //sp.getLevel().addRespawnedPlayer(sp);
        npc.getEntity().setHealth(20);
        //sp.unsetRemoved();
        NMSBridge.setArrowCount(npc, 0);
        Packets.removePlayer(npc.getEntity()).send();
        Packets.addPlayer(npc.getEntity()).send();
        npc.respawn();
    }

    public void spawnNPC(NPC npc, Location location) {
        NMSBridge.setPos(npc, new Vector(location.getX(), location.getY(), location.getZ()));
        Packets.addPlayer(npc.getEntity()).send();
        try {
            Class<?> serverPlayerClass = findClass(Packages.SERVER_LEVEL, "EntityPlayer");
            Object serverPlayer = getEntityPlayer(npc.getEntity());
            Class<?> connClass = findClass(Packages.SERVER_NETWORK, "PlayerConnection"); // ServerGamePacketListenerImpl

            Object server = invoke(Bukkit.getServer(), "getServer");
            Class<?> minecraftServerClass = findClass(Packages.SERVER_VERSION_MOD, "MinecraftServer");

            // network manager
            Class<?> networkManagerClassBase = findClass(Packages.NETWORK, "NetworkManager"); // Connection
            Class<?> networkManagerClass = new ByteBuddy().subclass(networkManagerClassBase)
                    .method(ElementMatchers.named("a")).intercept(MethodCall.run(() -> {})).make()
                    .load(networkManagerClassBase.getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();

            Class<?> packetFlowClass = findClass(Packages.NETWORK.plus("protocol"), "EnumProtocolDirection");
            Object clientboundPacketFlow = getEnum(packetFlowClass, "CLIENTBOUND"); // CLIENTBOUND
            Object networkManager = networkManagerClass.getConstructor(packetFlowClass).newInstance(clientboundPacketFlow);

            // connection class
            Object conn = connClass.getConstructor(minecraftServerClass, networkManagerClassBase, serverPlayerClass)
                    .newInstance(server, networkManager, serverPlayer);

            setField(serverPlayer, "b", conn);
            NMSBridge.addToWorld(npc, location.getWorld());
            npc.getOverrides().onSpawn();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeNPC(NPC npc) {
        npc.remove();
        despawnNPC(npc);
        NMSBridge.removeCachedData(npc);
        NPCs.remove(npc);
    }

    private void despawnNPC(NPC npc) {
        Packets.removePlayer(npc.getEntity()).send();
        NMSBridge.remove(npc);
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

    public boolean isNPC(org.bukkit.entity.Entity entity) {
        for (NPC npc : NPCs) {
            if (npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public NPC getNPC(org.bukkit.entity.Entity entity) {
        for (NPC npc : NPCs) {
            if (npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return npc;
            }
        }
        return null;
    }
}
