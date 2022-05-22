package com.personoid.npc.ai.controller;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.pathfinding.MovementType;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.MathUtils;
import com.personoid.utils.values.BlockTypes;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class MoveController extends NPCTickingComponent {
    private Vector velocity = new Vector();
    private Vector oldVel = new Vector();

    private int timeoutTicks;
    private int jumpTicks;
    private boolean climbing;

    public MoveController(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        if (--timeoutTicks > 0) timeoutTicks--;
        if (!climbing) tickGravity();
        tickMovement();
        oldVel = velocity.clone();
    }

    private void tickMovement() {
        MathUtils.clean(velocity);
        npc.move(MoverType.SELF, new Vec3(velocity.getX(), velocity.getY(), velocity.getZ()));
        addFriction(npc.isInWater() ? 0.8 : 0.5);
        climbing = BlockTypes.isClimbable(npc.getLocation().getBlock().getType());
    }

    private void tickGravity() {
        if (npc.isInWater()) velocity.setY(Math.min(velocity.getY() + 0.1, 0.1));
        else if (npc.isOnGround()) velocity.setY(0);
        else velocity.setY(Math.max(velocity.getY() - 0.1, -3.5));
    }

    private void addFriction(double factor) {
        double min = 0.01;
        double x = velocity.getX();
        double z = velocity.getZ();
        velocity.setX(Math.abs(x) < min ? 0 : x * factor);
        velocity.setZ(Math.abs(z) < min ? 0 : z * factor);
    }

    public void move(Vector velocity, MovementType type) {
        if (timeoutTicks > 0) return;
        double max = type.name().contains("SPRINT") ? 0.425 : 0.325;
        Vector sum = this.velocity.clone().add(velocity.clone().setY(0));
        if (sum.length() > max) {
            sum.normalize().multiply(max);
        }
        this.velocity.setX(sum.getX());
        this.velocity.setZ(sum.getZ());
    }

    public void jump() {
        if (npc.isOnGround()) {
            velocity.setY(0.55);
            npc.setGroundTicks(0);
            jumpTicks = 4;
        }
    }

    public void applyKnockback(Location source) {
        Vector vel = npc.getLocation().toVector().subtract(source.toVector()).setY(0).normalize().multiply(0.3);
        if (npc.isOnGround()) vel.multiply(1.7).setY(0.35);
        timeoutTicks = 10;
        velocity = vel;
    }

    public void step(double force) {
        if (npc.isOnGround()) {
            velocity.setY(force);
            npc.setGroundTicks(0);
            jumpTicks = 4;
        }
    }

    public void addVelocity(Vector velocity) {
        this.velocity.add(velocity);
    }

    public boolean isFalling() {
        return velocity.getY() < -0.8F;
    }

    public boolean wasFalling() {
        return oldVel.getY() < -0.8F;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public Vector getOldVelocity() {
        return oldVel;
    }

    public boolean isClimbing() {
        return climbing;
    }
}
