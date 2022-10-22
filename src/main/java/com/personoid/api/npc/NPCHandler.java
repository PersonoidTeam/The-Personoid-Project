package com.personoid.api.npc;

import com.personoid.api.utils.packet.Packages;
import com.personoid.api.utils.packet.Packets;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.personoid.api.utils.packet.ReflectionUtils.*;

public class NPCHandler {
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
        ServerPlayer sp = ((ServerPlayer)npc.getOverrides().getBase());
/*        sp.connection.send(new ClientboundRespawnPacket(sp.getLevel().dimensionTypeId(), sp.getLevel().dimension(),
                BiomeManager.obfuscateSeed(sp.getLevel().getSeed()), sp.gameMode.getGameModeForPlayer(), sp.gameMode.getPreviousGameModeForPlayer(),
                sp.getLevel().isDebug(), sp.getLevel().isFlat(), true, sp.getLastDeathLocation()));*/
        sp.respawn();
        sp.setPos(new Vec3(location.getX(), location.getY(), location.getZ()));
        //sp.getLevel().addRespawnedPlayer(sp);
        npc.getEntity().setHealth(20);
        //sp.unsetRemoved();
        Packets.removePlayer(npc.getEntity()).send();
        Packets.addPlayer(npc.getEntity()).send();
        npc.respawn();
    }

    public void spawnNPC(NPC npc, Location location) {
        ((CraftPlayer) npc.getEntity()).getHandle().setPos(new Vec3(location.getX(), location.getY(), location.getZ()));
        Packets.addPlayer(npc.getEntity()).send();
        Connection playerConn = ((CraftPlayer)Bukkit.getPlayer("DefineDoddy")).getHandle().connection.connection;
        //Bukkit.broadcastMessage("hashcode: " + ((CraftPlayer)Bukkit.getPlayer("DefineDoddy")).getHandle().connection.hashCode());
        //Bukkit.broadcastMessage("hashcode2: " + playerConn.hashCode());
        //Bukkit.broadcastMessage("address: " + playerConn.address.toString());
        try {
            Class<?> serverPlayerClass = findClass(Packages.SERVER_LEVEL, "EntityPlayer");
            Object serverPlayer = getEntityPlayer(npc.getEntity());
            Class<?> connClass = findClass(Packages.SERVER_NETWORK, "PlayerConnection");

            Object server = invoke(Bukkit.getServer(), "getServer");
            Class<?> minecraftServerClass = findClass(Packages.SERVER_VERSION_MOD, "MinecraftServer");

            // network manager
            Class<?> networkManagerClassBase = findClass(Packages.NETWORK, "NetworkManager");
            Class<?> networkManagerClass = new ByteBuddy().subclass(networkManagerClassBase)
                    .method(ElementMatchers.named("a")).intercept(MethodCall.run(() -> {

                    })).make()
                    .load(networkManagerClassBase.getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();

            Class<?> packetFlowClass = findClass(Packages.NETWORK.plus("protocol"), "EnumProtocolDirection");
            Object clientboundPacketFlow = getEnum(packetFlowClass, "CLIENTBOUND"); // CLIENTBOUND
            Object networkManager = networkManagerClass.getConstructor(packetFlowClass).newInstance(clientboundPacketFlow);

            // connection class
            Object conn = connClass.getConstructor(minecraftServerClass, networkManagerClassBase, serverPlayerClass)
                    .newInstance(server, networkManager, serverPlayer);
/*            setField(networkManager, "n", new SocketAddress() {
                private static final long serialVersionUID = 6994835504305404545L;
            });*/
            setField(serverPlayer, "b", conn);

            Object level = invoke(serverPlayer, "W"); // getLevel
            //level.getClass().getMethod("c", serverPlayerClass).invoke(level, serverPlayer); // addPlayer
            ((CraftPlayer) npc.getEntity()).getHandle().getLevel().addNewPlayer(((CraftPlayer) npc.getEntity()).getHandle());
            ((ServerPlayer)npc.getOverrides().getBase()).connection.connection.address = playerConn.address;
            npc.getOverrides().onSpawn();
            //Bukkit.broadcastMessage("npc address: " + ((ServerPlayer)npc.getOverrides().getBase()).connection.connection.address.toString());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeNPC(NPC npc) {
        despawnNPC(npc);
        NPCs.remove(npc);
    }

    private void despawnNPC(NPC npc) {
        Packets.removePlayer(npc.getEntity()).send();
        Class<?> removalReasonClass = findClass(Packages.ENTITY, "Entity$RemovalReason");
        //Object removalReason = getEnum(removalReasonClass, "b"); // DISCARDED FIXME: no enum constant found ?!?!?
        //invoke(getEntityPlayer(npc.getEntity()), "a", removalReason); // remove
        //((ServerPlayer)npc.getOverrides().getBase()).remove(Entity.RemovalReason.DISCARDED); // TODO: implement
        ((CraftPlayer) npc.getEntity()).getHandle().getLevel().removePlayerImmediately(((ServerPlayer)npc.getOverrides().getBase()), Entity.RemovalReason.DISCARDED);
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
