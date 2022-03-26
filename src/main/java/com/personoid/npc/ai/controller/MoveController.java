package com.personoid.npc.ai.controller;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.MathUtils;
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
        tickGravity();
        oldVel = moveVel.clone().add(new Vector(0, gravVel, 0));
    }

    private void tickMovement() {
        MathUtils.clean(moveVel);
        npc.move(MoverType.SELF, new Vec3(moveVel.getX(), moveVel.getY(), moveVel.getZ()));
        addFriction(npc.isInWater() ? 0.8 : 0.5);
    }

    private void tickGravity() {
        if (npc.isInWater()) gravVel = Math.min(gravVel + 0.1, 0.1);
        else if (npc.isOnGround()) gravVel = 0;
        else gravVel = Math.max(gravVel - 0.1, -3.5);
        npc.move(MoverType.SELF, new Vec3(0, gravVel, 0));
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

    public void move(Vector velocity, boolean includeY) {
        if (timeoutTicks > 0) return;
        double max = 0.4;
        Vector vel = velocity.clone();
        if (!includeY) vel.setY(0);
        Vector sum = moveVel.clone().add(vel);
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
        timeoutTicks = 10;
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
