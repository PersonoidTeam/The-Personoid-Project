package com.personoid.api.ai.looking;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.packet.Packets;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class LookController {
    private final NPC npc;
    private final Map<String, Target> targets = new HashMap<>();
    private boolean canLookInDefaultDirection = false;

    public LookController(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (canLookInDefaultDirection) {
            Vector vel = npc.getMoveController().getVelocity();
            Location defaultTarget = npc.getLocation().clone().add(vel.multiply(5));
            if (vel.getX() > 0.01 || vel.getZ() > 0.01) targets.put("default", new Target(defaultTarget, Priority.LOWEST));
        }
        if (targets.isEmpty()) return;
        Location facing = getFacing(getHighestPriorityTarget().getLocation());
        Packets.rotateEntity(npc.getEntity(), facing.getYaw(), facing.getPitch()).send();
        //npc.setRotation(facing.getYaw(), facing.getPitch());
    }

    public void face(Location location) {
        Location facing = getFacing(location);
        Packets.rotateEntity(npc.getEntity(), facing.getYaw(), facing.getPitch()).send();
        //npc.setRotation(facing.getYaw(), facing.getPitch());
    }

    private Location getFacing(Location target) {
        Vector dir = target.clone().subtract(npc.getLocation().clone()).toVector();
        return npc.getLocation().clone().setDirection(dir);
    }

    public Target getHighestPriorityTarget() {
        Target highest = null;
        for (Target target : targets.values()) {
            if (highest == null || target.getPriority().getValue() > highest.getPriority().getValue()) {
                highest = target;
            }
        }
        return highest;
    }

    public boolean hasTarget(String identifier) {
        return targets.containsKey(identifier);
    }

    public boolean addTarget(String identifier, Target target) {
        this.targets.put(identifier, target);
        return !targets.containsKey(identifier);
    }

    public boolean removeTarget(String identifier) {
        if (!targets.containsKey(identifier)) return false;
        this.targets.remove(identifier);
        return true;
    }

    public void purgeTargets() {
        this.targets.clear();
    }

    public void setCanLookInDefaultDirection(boolean value) {
        this.canLookInDefaultDirection = value;
    }

    public Map<String, Target> getTargets() {
        return targets;
    }
}