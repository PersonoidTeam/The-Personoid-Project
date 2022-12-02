package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.BlockTags;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class MoveController {
    private final NPC npc;
    private Vector velocity = new Vector();
    private Vector oldVel = new Vector();

    private int timeoutTicks;
    private int jumpTicks;
    private boolean climbing;
    private MovementType movementType;

    public MoveController(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (--timeoutTicks > 0) timeoutTicks--;
        if (!climbing) tickGravity();
        tickMovement();
        oldVel = velocity.clone();
    }

    private void tickMovement() {
        MathUtils.clean(velocity);
        npc.move(velocity);
        addFriction(npc.isInWater() ? 0.5 : npc.isOnGround() ? 0.8 : 0.7); // 0.8, 0.5
        climbing = BlockTags.CLIMBABLE.is(npc.getLocation().getBlock().getType());
    }

    private void tickGravity() {
        if (!npc.hasGravity()) {
            velocity.setY(0);
            return;
        }
        if (npc.isInWater()) velocity.setY(Math.min(velocity.getY() + 0.02, 0.1));
        else if (npc.isOnGround()) velocity.setY(0);
        else velocity.setY(Math.max(velocity.getY() - 0.09, -3.25));
    }

    private void addFriction(double factor) {
        double min = 0.01;
        double x = velocity.getX();
        double z = velocity.getZ();
        velocity.setX(Math.abs(x) < min ? 0 : x * factor);
        velocity.setZ(Math.abs(z) < min ? 0 : z * factor);
    }

    public void move(Vector velocity, MovementType type) {
        if (timeoutTicks > 0 || !npc.hasAI()) return;
        movementType = type;
        double max;
        switch (type) {
            case SPRINT_JUMPING:
                max = 0.4;
                break;
            case SPRINTING:
                max = 0.33;
                break;
            case WALKING:
                max = 0.22;
                break;
            default:
                max = 0.38;
        }
        Vector sum = this.velocity.clone().add(velocity.clone().setY(0));
        if (sum.length() > max) {
            sum.normalize().multiply(max);
        }
        this.velocity.setX(sum.getX());
        this.velocity.setZ(sum.getZ());
    }

    public void jump() {
        if (npc.hasAI() && npc.isOnGround()) {
            velocity.setY(0.55);
            //npc.setGroundTicks(0);
            jumpTicks = 4;
        }
    }

    public void applyKnockback(Location source) {
        if (!npc.hasAI()) return;
        Vector vel = npc.getLocation().toVector().subtract(source.toVector()).setY(0).normalize().multiply(0.3);
        if (npc.isOnGround()) vel.setY(0.2);
        //timeoutTicks = 10;
        velocity = vel;
    }

    public void step(double force) {
        if (npc.hasAI() && npc.isOnGround()) {
            velocity.setY(force);
            //npc.setGroundTicks(0);
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
        return velocity.clone();
    }

    public Vector getOldVelocity() {
        return oldVel;
    }

    public boolean isClimbing() {
        return climbing;
    }
}
