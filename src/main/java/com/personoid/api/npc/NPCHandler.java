package com.personoid.api.npc;

import com.personoid.api.utils.packet.Packets;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPCHandler {
    private static final List<NPC> npcs = new ArrayList<>();

    public NPC createNPCInstance(String name) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        NPC npc = NPCBuilder.create(profile);
        npcs.add(npc);
        return npc;
    }

    public NPC createNPCInstance(String name, Skin skin) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name, skin);
        NPC npc = NPCBuilder.create(profile);
        npcs.add(npc);
        return npc;
    }

    public void spawnNPC(NPC npc, Location location) {
        npc.teleport(location);
        Packets.addPlayer(npc.getEntity()).send();
        //npc.getLevel().addNewPlayer(npc); // TODO: implement
    }

    public void removeNPC(NPC npc) {
        despawnNPC(npc);
        npcs.remove(npc);
    }

    private void despawnNPC(NPC npc) {
        Packets.removePlayer(npc.getEntity()).send();
        //npc.remove(Entity.RemovalReason.DISCARDED); // TODO: implement
    }

    public NPC getNPC(String name) {
        return npcs.stream().filter(npc -> npc.getProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<NPC> getNPCs() {
        return npcs;
    }

    public void purgeNPCs() {
        npcs.forEach(this::despawnNPC);
        npcs.clear();
    }

    public boolean isNPC(org.bukkit.entity.Entity entity) {
        for (NPC npc : npcs) {
            if (npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public NPC getNPC(org.bukkit.entity.Entity entity) {
        for (NPC npc : npcs) {
            if (npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return npc;
            }
        }
        return null;
    }
}
