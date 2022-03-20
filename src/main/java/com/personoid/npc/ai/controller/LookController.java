package com.personoid.npc.ai.controller;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.npc.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class LookController extends NPCTickingComponent {
    private Location facing;

    public LookController(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        if (facing != null) {
            tickFacing();
        }
    }

    private void tickFacing() {
        try {
            Vector dir = facing.toVector().subtract(npc.getLocation().toVector()).normalize();
            Location facing = npc.getLocation().setDirection(dir);
            npc.getBukkitEntity().teleport(facing);
            PacketUtils.send(new ClientboundRotateHeadPacket(npc.getBukkitEntity().getHandle(), (byte) (facing.getYaw() * 256 / 360)));
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
}
