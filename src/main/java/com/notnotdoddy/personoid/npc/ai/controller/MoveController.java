package com.notnotdoddy.personoid.npc.ai.controller;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.components.NPCTickingComponent;
import com.notnotdoddy.personoid.utils.MathUtils;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class MoveController extends NPCTickingComponent {
    private Vector moveVel = new Vector();
    private Vector oldVel = new Vector();
    private double gravVel;
    private int timeoutTicks;

    private int jumpTicks = 0;

    public MoveController(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        if (--timeoutTicks > 0) timeoutTicks--;
        tickMovement();
        tickWater();
        tickGravity();
        oldVel = moveVel.clone().add(new Vector(0, gravVel, 0));
    }

    private void tickMovement() {
        MathUtils.clean(moveVel);
        npc.move(MoverType.SELF, new Vec3(moveVel.getX(), moveVel.getY(), moveVel.getZ()));
    }

    private void tickGravity() {
        if (npc.isOnGround() || npc.isInWater()) {
            gravVel = 0;
        } else {
            addFriction(0.5);
            gravVel = Math.max(gravVel - 0.1, -3.5);
        }
        npc.move(MoverType.SELF, new Vec3(0, gravVel, 0));
    }

    private void tickWater() {
        if (npc.isInWater()) {
            addFriction(0.8);
            gravVel = Math.min(gravVel + 0.1, 0.1);
            npc.move(MoverType.SELF, new Vec3(0, gravVel, 0));
        }
    }

    private void addFriction(double factor) {
        double min = 0.01;
        double x = moveVel.getX();
        double z = moveVel.getZ();
        moveVel.setX(Math.abs(x) < min ? 0 : x * factor);
        moveVel.setZ(Math.abs(z) < min ? 0 : z * factor);
    }

    public void move(Vector velocity) {
        if (timeoutTicks > 0) return;
        double max = 0.4;
        Vector sum = moveVel.clone().add(velocity.clone().setY(0));
        if (sum.length() > max) {
            sum.normalize().multiply(max);
        }
        moveVel = sum;
    }

    public void jump() {
        if (jumpTicks == 0 && npc.getGroundTicks() > 1) {
            npc.setGroundTicks(0);
            jumpTicks = 4;
            moveVel.setY(0.5); // jump factor
        }
    }

    public void applyKnockback(Location source) {
        Vector vel = npc.getLocation().toVector().subtract(source.toVector()).setY(0).normalize().multiply(0.3);
        if (npc.isOnGround()) vel.multiply(1.7).setY(0.55);
        timeoutTicks = 12;
        moveVel = vel;
    }

    public void addVelocity(Vector velocity) {
        moveVel.add(velocity);
    }

    public boolean isFalling() {
        return gravVel < -0.1F;
    }

    public Vector getVelocity() {
        return moveVel;
    }

    public Vector getOldVelocity() {
        return oldVel;
    }
}
