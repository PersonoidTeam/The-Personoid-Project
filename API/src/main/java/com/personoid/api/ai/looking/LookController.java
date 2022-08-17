package com.personoid.api.ai.looking;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.packet.Packets;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class LookController {
    private final NPC npc;
    private Location facing;
    private boolean smoothing;


    public LookController(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (facing != null) {
            tickFacing();
        }
    }

    private void tickFacing() {
        Vector dir = facing.clone().subtract(npc.getLocation().clone()).toVector();
        Location facing = npc.getLocation().clone().setDirection(dir);
        //facing.setYaw(smoothing ? MathUtils.lerpRotation(npc.getLocation().getYaw(), facing.getYaw(), 10F) : facing.getYaw());
        //facing.setPitch(smoothing ? MathUtils.lerpRotation(npc.getLocation().getPitch(), facing.getPitch(), 10F) : facing.getPitch());
        Packets.rotateEntity(npc.getEntity(), facing.getYaw(), facing.getPitch()).send();
        npc.setRotation(facing.getYaw(), facing.getPitch());
    }

    public void face(Entity entity) {
        facing = entity.getLocation();
    }

    public void face(Location location) {
        facing = location;
    }

    public void reset() {
        facing = npc.getLocation().add(npc.getLocation().getDirection().multiply(2));
        tickFacing();
        forget();
    }

    public void forget() {
        facing = null;
    }

    public boolean isSmoothing() {
        return smoothing;
    }

    public void setSmoothing(boolean smoothing) {
        this.smoothing = smoothing;
    }

    public Location getFacing() {
        return facing;
    }
}
