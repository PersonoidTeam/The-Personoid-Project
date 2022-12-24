package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.packet.Packets;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class MoveController {
    private final NPC npc;
    private final Options options = new Options();
    private MovementType movementType = MovementType.SPRINTING;

    private int jumpTicks;
    private int timeoutTicks;
    private boolean initTick;

    private double motionX;
    private double motionY;
    private double motionZ;

    private double moveForward;
    private double moveStrafing;

    private double targetX;
    private double targetZ;

    public MoveController(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (jumpTicks > 0) --jumpTicks;
        if (timeoutTicks > 0) timeoutTicks--;

        if (!initTick) {
            targetX = npc.getLocation().getX();
            targetZ = npc.getLocation().getZ();
            initTick = true;
        }

        moveForward *= 0.98;
        moveStrafing *= 0.98;
        if (Math.abs(motionY) < 0.005D || npc.isOnGround()) motionY = 0.0D;
        calculateMovement();
        moveEntityWithHeading(moveForward, moveStrafing);

        //tickGravity();
        //tickMovement();
    }

    private void tickMovement() {
        //Vec3 input = getInputVector(new Vec3(motionX, 0, motionZ), 1F, npc.getYaw());
        //Bukkit.broadcastMessage("input: " + String.format("%.2f, %.2f", input.x, input.z));
        npc.move(new Vector(motionX, motionY, motionZ));
        double friction = isInWater(0.15) ? 0.35 : 0.6; //npc.isOnGround() ? 0.8 : 0.6
        addFriction(friction);
        //climbing = BlockTags.CLIMBABLE.is(npc.getLocation().getBlock().getType());
    }

    private void tickGravity() {
        if (!npc.hasGravity()) {
            motionY = 0;
            return;
        }
        if (npc.isOnGround()) {
            motionY = 0D;
        } else {
            motionY -= 0.08;
            motionY *= 0.98;
            if (Math.abs(motionY) < 0.005D) motionY = 0.0D;
        }
        //if (isInWater(0.2)) motionY = Math.min(motionY + 0.015, 0.1);
    }

    private void addFriction(double factor) {
        double min = 0.005D;
        motionX = Math.abs(motionX) < min ? 0 : motionX * factor;
        motionZ = Math.abs(motionZ) < min ? 0 : motionZ * factor;
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

    private void calculateMovement() {
        double dX = targetX - npc.getLocation().getX();
        double dZ = targetZ - npc.getLocation().getZ();

        if (Math.abs(dX) < 0.005D && Math.abs(dZ) < 0.005D) { // FIXME: not working
            moveForward = 0;
            moveStrafing = 0;
            return;
        }

        // look towards target location
        float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90F;
        Packets.rotateEntity(npc.getEntity(), yaw, npc.getPitch()).send();
        npc.setYaw(yaw);

        Vec3 input = getInputVector(new Vec3(Math.abs(dX), 0, Math.abs(dZ)), 10F, yaw).scale(1000);
        //Bukkit.broadcastMessage("input: " + String.format("%.2f, %.2f", input.x, input.z));
        moveForward = input.z;
        moveStrafing = input.x;
    }

    // WALKING: 0.4, SPRINTING: 0.6, SPRING_JUMPING: 0.8

    public void moveTo(double x, double z, MovementType movementType) {
        targetX = x;
        targetZ = z;
        this.movementType = movementType;
    }

    private void moveEntityWithHeading(double forward, double strafe) {
        if (timeoutTicks > 0 || !npc.hasAI()) return;

        float mult = 0.91F;
        if (npc.isOnGround()) mult *= getSlipperiness();

        float acceleration = 0.16277136F / (mult * mult * mult);
        float movementFactor = npc.isOnGround() ? getLandMovementFactor() * acceleration : getAirMovementFactor();

        updateMotionXZ(forward, strafe, movementFactor);

        this.motionY -= 0.08D;
        this.motionY *= 0.98D;

        this.motionX *= mult;
        this.motionZ *= mult;

        npc.move(new Vector(motionX, motionY, motionZ));
    }

    private void updateMotionXZ(double forward, double strafe, float movementFactor) {
        double distance = strafe * strafe + forward * forward;
        if (distance >= 1.0E-4F) {
            distance = Math.sqrt(distance);
            if (distance < 1.0F) distance = 1.0F;
            distance = movementFactor / distance;
            strafe = strafe * distance;
            forward = forward * distance;
            double sinYaw = Math.sin(npc.getYaw() * Math.PI / 180.0F);
            double cosYaw = Math.cos(npc.getYaw() * Math.PI / 180.0F);
            this.motionX += strafe * cosYaw - forward * sinYaw;
            this.motionZ += forward * cosYaw + strafe * sinYaw;
        }
    }

    public float getMovementFactor() {
        return 0.1F;
    }

    public float getLandMovementFactor() {
        float base = getMovementFactor();
        if (movementType.name().contains("SPRINT")) base *= 1.3F;
        return base;
    }

    public float getAirMovementFactor() {
        float base = getMovementFactor() / 5F;
        if (movementType.name().contains("SPRINT")) base *= 1.3F;
        return base;
    }

    public void jump() {
        if (npc.hasAI() && npc.isOnGround() && jumpTicks == 0) {
            jumpTicks = 10;
            this.motionY = 0.42;
            // apply jump boost
            if (npc.getEntity().hasPotionEffect(PotionEffectType.JUMP)) {
                this.motionY += (npc.getEntity().getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1) * 0.1F;
            }
            // apply sprint jump boost
/*            if (movementType == MovementType.SPRINT_JUMPING) {
                float f = npc.getYaw() * 0.017453292F; // radians
                motionX -= Math.sin(f) * 0.2;
                motionZ += Math.cos(f) * 0.2;
            }*/
        }
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

    private float getSlipperiness() {
        return npc.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().getSlipperiness();
    }

    public void applyKnockback(Location source) {
        if (!npc.hasAI()) return;
/*        Vector vel = npc.getLocation().toVector().subtract(source.toVector()).setY(0).normalize().multiply(0.4);
        if (npc.isOnGround()) vel.setY(0.23);
        //timeoutTicks = 10;
        velocity = vel;*/
    }

    public void step(double force) {
/*        Validate.isTrue(force < 0.5, "Force must be less than 0.5");
        if (npc.hasAI() && npc.isOnGround()) {
            velocity.setY(force);
        }*/
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
        this.motionX += velocity.getX();
        this.motionY += velocity.getY();
        this.motionZ += velocity.getZ();
    }

    public Vector getVelocity() {
        return new Vector(motionX, motionY, motionZ);
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
