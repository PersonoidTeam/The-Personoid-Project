package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.packet.Packets;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class MoveController {
    private final NPC npc;
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

    private boolean climbing;

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
    }

    private void calculateMovement() {
        double dX = targetX - npc.getLocation().getX();
        double dZ = targetZ - npc.getLocation().getZ();

        // stop moving if close enough to target
        if (Math.abs(dX) < 0.05 && Math.abs(dZ) < 0.05) {
            moveForward = 0;
            moveStrafing = 0;
            motionX *= 0.8;
            motionZ *= 0.8;
            return;
        }

        // look towards target location
        float yaw = (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90F) % 360F;
        Packets.rotateEntity(npc.getEntity(), yaw, 0F).send();
        npc.setYaw(yaw);
        npc.setPitch(0F);

        // calculate movement based on yaw
        Vec3 input = movementInputToVelocity(new Vec3(0, 0, 1), getSlipperiness(), yaw);
        moveForward = input.z;
        moveStrafing = input.x;
    }

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

        if (!NumberConversions.isFinite(motionX)) motionX = 0;
        if (!NumberConversions.isFinite(motionY)) motionY = 0;
        if (!NumberConversions.isFinite(motionZ)) motionZ = 0;

        npc.move(new Vector(motionX, motionY, motionZ));

        if (!climbing && npc.hasGravity()) {
            this.motionY -= 0.08D;
            this.motionY *= 0.98D;
        }

        this.motionX *= mult;
        this.motionZ *= mult;
    }

    private void updateMotionXZ(double forward, double strafe, float movementFactor) {
        double distance = strafe * strafe + forward * forward;
        if (distance >= 1.0E-4F) {
            distance = Math.sqrt(distance);
            if (distance < 1.0F) distance = 1.0F;
            distance = movementFactor / distance;
            strafe *= distance;
            forward *= distance;
            double sinYaw = Math.sin(npc.getYaw() * Math.PI / 180.0F);
            double cosYaw = Math.cos(npc.getYaw() * Math.PI / 180.0F);
            this.motionX += strafe * cosYaw - forward * sinYaw;
            this.motionZ += forward * cosYaw + strafe * sinYaw;
        }
    }

    public float getMovementFactor() {
        return 0.2F;
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
            if (movementType == MovementType.SPRINT_JUMPING) {
                moveForward *= 1.5;
                moveStrafing *= 1.5;
            }
        }
    }

    private static Vec3 movementInputToVelocity(Vec3 movementInput, float speed, float yaw) {
        double d = movementInput.lengthSqr();
        if (d < 1.0E-7) {
            return Vec3.ZERO;
        } else {
            Vec3 vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).scale(speed);
            float f = Mth.sin(yaw * 0.017453292F);
            float g = Mth.cos(yaw * 0.017453292F);
            return new Vec3(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
        }
    }

    private float getSlipperiness() {
        return npc.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().getSlipperiness();
    }

    public void applyKnockback(Location source) {
        if (!npc.hasAI()) return;
        Vector vel = npc.getLocation().toVector().subtract(source.toVector()).setY(0).normalize().multiply(0.4);
        if (npc.isOnGround()) vel.setY(0.15);
        //timeoutTicks = 10;
        motionX += vel.getX();
        motionY += vel.getY();
        motionZ += vel.getZ();
    }

    public void applyUpwardForce(double force) {
        if (npc.hasAI()) {
            motionY = force;
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
        this.motionX += velocity.getX();
        this.motionY += velocity.getY();
        this.motionZ += velocity.getZ();
    }

    public Vector getVelocity() {
        return new Vector(motionX, motionY, motionZ);
    }

    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
    }

    public boolean isClimbing() {
        return climbing;
    }
}
