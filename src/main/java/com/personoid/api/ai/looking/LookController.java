package com.personoid.api.ai.looking;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.bukkit.BlockPos;
import com.personoid.nms.packet.Packets;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class LookController {
    private final NPC npc;
    private final Map<String, Target> targets = new HashMap<>();
    private boolean lookAhead = false;

    public LookController(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (targets.isEmpty() || lookAhead) return;
        Location facing = getFacing(getHighestPriorityTarget().getLocation());
        Packets.rotateEntity(npc.getEntity(), facing.getYaw(), facing.getPitch()).send();
        npc.setRotation(facing.getYaw(), facing.getPitch());
    }

    public void face(BlockPos blockPos) {
        face(blockPos.toLocation(npc.getWorld()));
    }

    public void face(Location location) {
        Location facing = getFacing(location);
        // if target facing is greater than 45 degrees away from the direction of player's velocity, clamp to 45 degrees looking towards target
        Vector vel = npc.getMoveController().getVelocity();
        if (vel.lengthSquared() > 0.01) {
            Vector facingVec = facing.toVector().subtract(npc.getLocation().toVector());
            double angle = facingVec.angle(vel);
            if (angle > Math.toRadians(45)) {
                facingVec = facingVec.normalize().multiply(vel.length());
                facing = npc.getLocation().clone().add(facingVec);
            }
        }
        Packets.rotateEntity(npc.getEntity(), facing.getYaw(), facing.getPitch()).send();
        //npc.setRotation(facingTarget.getYaw(), facingTarget.getPitch());
    }

    private Location getFacing(Location target) {
        Vector dir = target.clone().subtract(npc.getLocation().clone()).toVector();
        return npc.getLocation().clone().setDirection(dir);
    }

    public Target getHighestPriorityTarget() {
        Target highest = null;
        for (Target target : targets.values()) {
            if (highest == null || target.getPriority().isHigherThan(highest.getPriority())) {
                highest = target;
            }
        }
        return highest;
    }

    public String getCurrentTarget() {
        String identifier = null;
        for (Map.Entry<String, Target> entry : targets.entrySet()) {
            if (entry.getValue().getPriority() == getHighestPriorityTarget().getPriority()) {
                identifier = entry.getKey();
                break;
            }
        }
        return identifier;
    }

    public boolean hasTarget(String identifier) {
        return targets.containsKey(identifier);
    }

    public boolean addTarget(String identifier, Target target) {
        boolean exists = targets.containsKey(identifier);
        this.targets.put(identifier, target);
        return !exists;
    }

    public boolean removeTarget(String identifier) {
        if (!targets.containsKey(identifier)) return false;
        this.targets.remove(identifier);
        return true;
    }

    public Vector getDirection() {
        return npc.getLocation().getDirection();
    }

    public void purgeTargets() {
        this.targets.clear();
    }

    public void setLookAhead(boolean value) {
        this.lookAhead = value;
    }

    public Map<String, Target> getTargets() {
        return targets;
    }

    public Target getTarget(String identifier) {
        return targets.get(identifier);
    }
}
