package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.LocationUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class MoveController {
    private final NPC npc;

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

        if (Math.abs(motionY) < 0.005D || npc.isOnGround()) motionY = 0.0D;
        calculateMovement();
        moveEntityWithHeading(moveForward, moveStrafing);
    }

    private void calculateMovement() {
        // if boundingbox of npc collides with blocks in front of it, stop moving
        BoundingBox bb = npc.getEntity().getBoundingBox().clone().expand(0.5D, 0.0D, 0.5D);
        Block block1 = getBlockAtDistance(1, false);
        Block block2 = block1.getRelative(0, 1, 0);
        BoundingBox bb1 = block1.getBoundingBox();
        BoundingBox bb2 = block2.getBoundingBox();
        Bukkit.broadcastMessage("block1Loc: " + LocationUtils.toString(block1.getLocation()));
        Bukkit.broadcastMessage("block1: " + block1.getType().name() + ", isSolid: " + block1.getType().isSolid());
        Bukkit.broadcastMessage("block2: " + block2.getType().name() + ", isSolid: " + block2.getType().isSolid());
        if ((block1.getType().isSolid() && bb.overlaps(bb1)) || (block2.getType().isSolid() && bb.overlaps(bb2))) {
            motionX = 0.0D;
            motionZ = 0.0D;
            Bukkit.broadcastMessage("collided");
        }

        double dX = targetX - npc.getLocation().getX();
        double dZ = targetZ - npc.getLocation().getZ();

        // stop moving if close enough to target
        if (Math.abs(dX) < 0.08 && Math.abs(dZ) < 0.08) {
            moveForward = 0.0D;
            moveStrafing = 0.0D;
            return;
        }

        // look towards target location
/*        float yaw = (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90F) % 360F;
        Packets.rotateEntity(npc.getEntity(), yaw, 0F).send();
        npc.setYaw(yaw);
        npc.setPitch(0F);*/

        npc.getEntity().setSprinting(npc.isSprinting() && moveForward > 0.0D);

        // calculate movement based on yaw
        ItemStack mainHand = npc.getInventory().getSelectedItem();
        ItemStack offHand = npc.getInventory().getOffhandItem();
        boolean hasShield = (mainHand != null && mainHand.getType() == Material.SHIELD) || (offHand != null && offHand.getType() == Material.SHIELD);
        double speed = npc.getItemUsingTicks() > 0 && hasShield ? 0.3 : 1;

        Vec3 direction = new Vec3(dX, 0, dZ).normalize().multiply(speed, speed, speed);
        Vec3 input = movementInputToVelocity(direction, getSlipperiness(), npc.getYaw());
/*        moveForward = input.z;
        moveStrafing = input.x;*/
    }

    private Block getBlockAtDistance(int range, boolean stopAtSolid) {
        Location loc = npc.getLocation().clone().subtract(0, 2, 0);
        loc.setPitch(0F);
        BlockIterator iterator = new BlockIterator(loc, range + 1);
        int i = 0;
        if (iterator.hasNext()) iterator.next();
        while (iterator.hasNext()) {
            i++;
            Block block = iterator.next();
            if (iterator.hasNext()) {
                if (stopAtSolid) {
                    if (block.getType().isSolid()) {
                        return block;
                    }
                } else {
                    return block;
                }
            } else {
                return block;
            }
            if (i > range) break;
        }
        return null;
    }

    public void moveTo(double x, double z) {
        targetX = x;
        targetZ = z;
    }

    public void stop() {
        targetX = npc.getLocation().getX();
        targetZ = npc.getLocation().getZ();
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
        return 0.1F;
    }

    public float getLandMovementFactor() {
        float base = getMovementFactor();
        if (npc.isSprinting()) base *= 1.3F;
        return base;
    }

    public float getAirMovementFactor() {
        float base = getMovementFactor() * 0.2F;
        if (npc.isSprinting()) base *= 1.3F;
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
            if (npc.isSprinting() && npc.isJumping()) {
                float f = npc.getYaw() * 0.017453292F;
                moveForward -= Math.sin(f) * 0.2F;
                moveStrafing += Math.cos(f) * 0.2F;
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
        double minYBounds = npc.getEntity().getBoundingBox().getMinY();
        Block groundBlock = new Location(npc.getWorld(), npc.getX(), minYBounds - 0.5F, npc.getZ()).getBlock();
        return npc.isOnGround() ? groundBlock.getType().getSlipperiness() : 1F;
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

    public double getTargetX() {
        return targetX;
    }

    public double getTargetZ() {
        return targetZ;
    }

    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
    }

    public boolean isClimbing() {
        return climbing;
    }
}
