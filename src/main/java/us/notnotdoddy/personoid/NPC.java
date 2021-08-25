package us.notnotdoddy.personoid;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class NPC {
    public net.citizensnpcs.api.npc.NPC entity;
    public Map<Player, PlayerInfo> players = new HashMap<>();
    public Player damagedByPlayer;
    public boolean paused;

    public NPC(String name) {
        entity = NPCHandler.registry.createNPC(EntityType.PLAYER, name);
        entity.setProtected(false);
        //change whatever you like here, this was mainly just for testing purposes
        entity.getNavigator().getLocalParameters().attackRange(10);
        entity.getNavigator().getLocalParameters().baseSpeed(1.15F);
        entity.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
        entity.getNavigator().getLocalParameters().attackDelayTicks(15);
    }

    public NPC spawn(Location location) {
        entity.spawn(location);
        NPCHandler.getNPCs().put(entity, this);
        return this;
    }

    public NPC remove() {
        NPCHandler.getNPCs().remove(entity);
        entity.despawn();
        NPCHandler.registry.deregister(entity);
        return this;
    }

    public NPC pause() {
        paused = true;
        entity.getNavigator().cancelNavigation();
        return this;
    }

    public NPC resume() {
        paused = false;
        return this;
    }
}
