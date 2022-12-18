package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.packet.Packets;
import com.personoid.api.utils.types.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class MoveController {
    private final NPC npc;
    private Vector velocity = new Vector();
    private Vector oldVel = new Vector();
    private final Options options = new Options();
    private MovementType movementType;

    private int jumpTicks;
    private int timeoutTicks;
    private boolean climbing;

    public MoveController(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (jumpTicks > 0) --jumpTicks;
        if (--timeoutTicks > 0) timeoutTicks--;
        if (!climbing) tickGravity();
        tickMovement();
        oldVel = velocity.clone();
    }

    private void tickMovement() {
        //MathUtils.clean(velocity);
        double friction = isInWater(0.15) ? 0.35 : npc.isOnGround() ? 0.8 : 0.6;
        //addFriction(friction);
        npc.move(velocity);
        climbing = BlockTags.CLIMBABLE.is(npc.getLocation().getBlock().getType());
    }

    private void tickGravity() {
        if (!npc.hasGravity()) {
            velocity.setY(0);
            return;
        }
        if (isInWater(0.2)) velocity.setY(Math.min(velocity.getY() + 0.015, 0.1));
        else if (npc.isOnGround()) velocity.setY(0);
        else {
            // fall at 1.6m/s, then multiply by 0.98 to account for friction
            velocity.setY(Math.max(velocity.getY() - 0.08, -0.4) * 0.98);
        }
    }

    private void addFriction(double factor) {
        double min = 0.01;
        double x = velocity.getX();
        double z = velocity.getZ();
        velocity.setX(Math.abs(x) < min ? 0 : x * factor);
        velocity.setZ(Math.abs(z) < min ? 0 : z * factor);
    }

    protected float rotLerp(float a, float b, float t) {
        float deg = MathUtils.wrapDegrees(b - a);
        if (deg > t) deg = t;
        if (deg < -t) deg = -t;
        float var4 = a + deg;
        if (var4 < 0.0F) var4 += 360.0F;
        else if (var4 > 360.0F) var4 -= 360.0F;
        return var4;
    }

    public void moveTo(double x, double z, MovementType movementType) {
        double dX = x - npc.getLocation().getX();
        double dZ = z - npc.getLocation().getZ();
        if (isInWater(0.2)) movementType = MovementType.WALKING;
        this.movementType = movementType;
        double max;
        switch (movementType) {
            case SPRINT_JUMPING:
                max = 0.38;
                break;
            case SPRINTING:
                max = 0.13;
                break;
            case WALKING:
                max = 0.28;
                break;
            default:
                max = 0.5;
        }
        Vec3 input = getInputVector(new Vec3(dX, 0, dZ), (float) max, npc.getYaw());
        move(input.x, input.z, movementType);
        Bukkit.broadcastMessage("input: " + String.format("%.2f, %.2f", input.x, input.z));
        float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90;
        npc.setYaw(yaw);
        Packets.rotateEntity(npc.getEntity(), yaw, npc.getPitch()).send();
    }

    private static Vec3 getInputVector(Vec3 vec3d, float f, float f1) {
        double d0 = vec3d.lengthSqr();
        if (d0 < 1.0E-7) {
            return Vec3.ZERO;
        } else {
            Vec3 vec3d1 = (d0 > 1.0 ? vec3d.normalize() : vec3d).scale(f);
            float f2 = Mth.sin(f1 * 0.017453292F);
            float f3 = Mth.cos(f1 * 0.017453292F);
            return new Vec3(vec3d1.x * (double)f3 - vec3d1.z * (double)f2, vec3d1.y, vec3d1.z * (double)f3 + vec3d1.x * (double)f2);
        }
    }

    private void move(double forward, double strafe, MovementType type) {
        if (timeoutTicks > 0 || !npc.hasAI()) return;
/*        double mag = Math.sqrt(forward * forward + strafe * strafe);
        if (mag > max) {
            double ratio = max / mag;
            forward *= ratio;
            strafe *= ratio;
        }*/
        float mult = 0.91F; // depends on block slipperiness
        float acceleration = 0.16277136F / (mult * mult * mult);
        float movementFactor = npc.isOnGround() ? getLandMovementFactor() * acceleration : getAirMovementFactor();

        // smooth out any sudden changes
        double maxChange = options.maxTurn; // 0.2
        double oldX = oldVel.getX();
        double oldZ = oldVel.getZ();
        double changeX = forward - oldX;
        double changeZ = strafe - oldZ;
        if (Math.abs(changeX) > maxChange) {
            forward = oldX + Math.signum(changeX) * maxChange;
        }
        if (Math.abs(changeZ) > maxChange) {
            strafe = oldZ + Math.signum(changeZ) * maxChange;
        }

/*        float distance = (float) Math.sqrt(forward * forward + strafe * strafe);
        if (distance >= 1.0E-4F) {
            distance = (float) Math.sqrt(distance);
            if (distance < 1.0F) distance = 1.0F;
            distance = movementFactor / distance;
            forward *= distance;
            strafe *= distance;
            double sinYaw = Math.sin(npc.getLocation().getYaw() * Math.PI / 180.0D);
            double cosYaw = Math.cos(npc.getLocation().getYaw() * Math.PI / 180.0D);
            this.velocity.setX(this.velocity.getX() + forward * cosYaw - strafe * sinYaw);
            this.velocity.setZ(this.velocity.getZ() + strafe * cosYaw + forward * sinYaw);
        }*/

        this.velocity.setX(forward * options.speedMultiplier);
        this.velocity.setZ(strafe * options.speedMultiplier);
        this.velocity.setX(this.velocity.getX() * mult);
        this.velocity.setZ(this.velocity.getZ() * mult);
    }

    public float getLandMovementFactor() {
        float base = 0.1F;
        if (movementType.name().contains("SPRINT")) base *= 1.3F;
        return base;
    }

    public float getAirMovementFactor() {
        float base = 0.02F;
        if (movementType.name().contains("SPRINT")) base *= 1.3F;
        return base;
    }

    public void jump() {
        if (npc.hasAI() && npc.isOnGround() && jumpTicks == 0) {
            jumpTicks = 10;
            velocity.setY(0.42);
            if (movementType == MovementType.SPRINT_JUMPING) {
                float f = npc.getYaw() * 0.017453292F; // multiply by pi/180 to convert to radians
                // decrement velocity x by MathHelper.sin(f) * 0.2F
                velocity.setX(velocity.getX() - Math.sin(f) * 0.2);
                // increment velocity z by MathHelper.cos(f) * 0.2F
                velocity.setZ(velocity.getZ() + Math.cos(f) * 0.2);
            }
        }
    }

    public void applyKnockback(Location source) {
        if (!npc.hasAI()) return;
/*        Vector vel = npc.getLocation().toVector().subtract(source.toVector()).setY(0).normalize().multiply(0.4);
        if (npc.isOnGround()) vel.setY(0.23);
        //timeoutTicks = 10;
        velocity = vel;*/
    }

    public void step(double force) {
        Validate.isTrue(force < 0.5, "Force must be less than 0.5");
        if (npc.hasAI() && npc.isOnGround()) {
            velocity.setY(force);
        }
    }

    private boolean isInWater(double yOffset) {
        Location loc = npc.getLocation().clone().add(0, yOffset, 0);
        for (int i = 0; i <= 2; i++) {
            Material type = loc.getBlock().getType();
            if (type == Material.WATER || type == Material.LAVA) {
                return true;
            }
            loc.add(0, 0.9, 0);
        }
        return false;
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

    public Options getOptions() {
        return options;
    }

    public static class Options {
        private float maxTurn = 0.15F;
        private float speedMultiplier = 1F;

        private Options() {}

        public float getMaxTurn() {
            return maxTurn;
        }

        public void setMaxTurn(float maxTurn) {
            this.maxTurn = maxTurn;
        }

        public float getSpeed() {
            return speedMultiplier;
        }

        public void setSpeed(float speed) {
            this.speedMultiplier = speed;
        }
    }
}
