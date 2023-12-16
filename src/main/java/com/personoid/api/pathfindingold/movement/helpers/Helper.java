package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.CacheManager;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.movement.Movement;
import org.bukkit.World;

public class Helper {
    private final NPC npc;
    private final Movement movement;

    public Helper(NPC npc, Movement movement) {
        this.npc = npc;
        this.movement = movement;
    }

    public double getCost() {
        return 0;
    }

    public NPC getNPC() {
        return npc;
    }

    public Movement getMovement() {
        return movement;
    }

    public Node getSource() {
        return movement.getSource();
    }

    public Node getDestination() {
        return movement.getDestination();
    }

    public CacheManager getCacheManager() {
        return CacheManager.get(npc.getWorld());
    }

    public World getWorld() {
        return npc.getWorld();
    }
}
