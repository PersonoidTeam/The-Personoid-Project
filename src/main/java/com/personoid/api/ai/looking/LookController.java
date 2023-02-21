package com.personoid.api.ai.looking;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfinding.utils.BlockPos;
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
        Target target = getHighestPriorityTarget();
        if (target != null) {
            //tryFace(target.getLocation());
            Location facing = getFacing(getHighestPriorityTarget().getLocation());
            Packets.rotateEntity(npc.getEntity(), facing.getYaw(), facing.getPitch()).send();
            npc.setRotation(facing.getYaw(), facing.getPitch());
        }
    }

    public void tryFace(BlockPos blockPos) {
        tryFace(blockPos.toLocation(npc.getWorld()));
    }

    public void tryFace(Location location) {
        Location facing = getFacing(location);
        float yaw = facing.getYaw();

        // try and look at the target location
        // if the target location is more than 45 degrees away from the moveTarget location and the npc is moving, clamp the yaw to 45 degrees

        double moveTargetX = npc.getMoveController().getTargetX();
        double moveTargetZ = npc.getMoveController().getTargetZ();
        Location moveTarget = new Location(npc.getWorld(), moveTargetX, npc.getLocation().getY(), moveTargetZ);
        float moveTargetYaw = getFacing(moveTarget).getYaw();

        Vector vel = npc.getMoveController().getVelocity();
        if (Math.abs(vel.getX()) > 0.005F || Math.abs(vel.getZ()) > 0.05F) {
            if (Math.abs(yaw - moveTargetYaw) > 45F) {
                yaw = moveTargetYaw + (yaw > moveTargetYaw ? 45F : -45F);
            }
        }

        if (npc.getLocation().distance(moveTarget) < 1F) {
            return;
        }

        facing.setYaw(yaw);
        Packets.rotateEntity(npc.getEntity(), yaw, facing.getPitch()).send();
        npc.setRotation(yaw, facing.getPitch());
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

    public String getLastTarget() {
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
