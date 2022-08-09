package com.personoid.api.npc;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;

public interface NPCHandler {
    NPC createNPCInstance(World world, String name);
    void spawnNPC(NPC npc, Location location);
    void removeNPC(NPC npc);
    void purgeNPCs();
    boolean isNPC(Entity entity);

    List<NPC> getNPCs();
    NPC getNPC(String name);
    NPC getNPC(Entity entity);
}
