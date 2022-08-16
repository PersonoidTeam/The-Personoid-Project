package com.personoid.api.ai.looking;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.math.MathUtils;
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
        try {
            Vector dir = facing.toVector().subtract(npc.getLocation().toVector()).normalize();
            Location facing = npc.getLocation().setDirection(dir);
            npc.getEntity().teleport(facing);
            //PacketUtils.send(new ClientboundRotateHeadPacket(npc.getBukkitEntity().getHandle(), (byte) (facing.getYaw() * 256 / 360)));
            npc.setYRotation(smoothing ? MathUtils.lerpRotation(npc.getYRotation(), facing.getYaw(), 10F) : facing.getYaw());
        } catch (Exception ignored) { }
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
