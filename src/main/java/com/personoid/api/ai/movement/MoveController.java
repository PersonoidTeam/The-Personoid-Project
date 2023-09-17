package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.nms.packet.Packets;
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
    private boolean travelling;

    public MoveController(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        //((CraftPlayer) npc.getEntity()).getHandle().baseTick();
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
        BoundingBox bb = npc.getEntity().getBoundingBox().clone().expand(0.5D, 0D, 0.5D);
        Block block1 = getBlockAtDistance(1, false);
        Block block2 = block1.getRelative(0, 1, 0);
        BoundingBox bb1 = block1.getBoundingBox();
        BoundingBox bb2 = block2.getBoundingBox();
        boolean collided = (block1.getType().isSolid() && bb.overlaps(bb1)) || (block2.getType().isSolid() && bb.overlaps(bb2));
        if (collided && !isInWater(-0.5F)) {
            motionX = 0.0D;
            motionZ = 0.0D;
            moveForward = 0.0D;
            moveStrafing = 0.0D;
        }

        // if npc is moving backwards, stop sprinting
        if (moveForward < 0.0D) {
            npc.setSprinting(false);
        } else {
            npc.getEntity().setSprinting(npc.isSprinting() && moveForward > 0D);
        }

        double dX = targetX - npc.getLocation().getX();
        double dZ = targetZ - npc.getLocation().getZ();

        if (!travelling) return;
        if (Math.abs(dX) < 0.001 && Math.abs(dZ) < 0.001) {
            moveForward = 0.0D;
            moveStrafing = 0.0D;
            travelling = false;
            return;
        }
        if (Math.abs(dX) > 0.01 || Math.abs(dZ) > 0.01) {
            // look towards target location
            float yaw = (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90F) % 360F;
            Packets.rotateEntity(npc.getEntity(), yaw, 0F).send();
            npc.setYaw(yaw);
            npc.setPitch(0F);
        }

        // calculate movement based on yaw
        ItemStack mainHand = npc.getInventory().getSelectedItem();
        ItemStack offHand = npc.getInventory().getOffhandItem();
        boolean hasShield = (mainHand != null && mainHand.getType() == Material.SHIELD) || (offHand != null && offHand.getType() == Material.SHIELD);
        float speed = npc.getItemUsingTicks() > 0 && hasShield ? 0.3F : 1F;

        Vector direction = new Vector(dX, 0, dZ).normalize().multiply(speed);
        Vector input = worldToInputVector(direction, 1F, npc.getYaw());
        moveForward = input.getZ();
        moveStrafing = input.getX();
    }

    private boolean handleWaterMovement() {
        if (npc.isOnGround()) return false;
        if (isInWater(-0.5F)) {
            motionY += 0.02D;
            motionY = Math.min(motionY, 0.15D);
        }
        return npc.isInWater();
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

    private void moveEntityWithHeading(double forward, double strafe) {
        if (timeoutTicks > 0 || !npc.hasAI()) return;

        float mult = 0.91F;
        if (npc.isOnGround()) mult *= getSlipperiness();

        float acceleration = 0.16277136F / (mult * mult * mult);
        float movementFactor = npc.isOnGround() ? getLandMovementFactor() * acceleration : getAirMovementFactor();
        if (isInWater(-0.5F)) {
            movementFactor = getWaterMovementFactor() * acceleration;
        }

        updateMotionXZ(forward, strafe, movementFactor);

        if (!NumberConversions.isFinite(motionX)) motionX = 0;
        if (!NumberConversions.isFinite(motionY)) motionY = 0;
        if (!NumberConversions.isFinite(motionZ)) motionZ = 0;

        npc.move(new Vector(motionX, motionY, motionZ));

        if (isInWater(-0.5F)) {
            motionY += 0.02D;
            motionY = Math.min(motionY, 0.15D);
        } else {
            if (!climbing && npc.hasGravity()) {
                this.motionY -= 0.08D;
                this.motionY *= 0.98D;
            }
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
/*            double lerp = 0.5D;
            motionX = motionX * (1 - lerp) + motionX * lerp;
            motionZ = motionZ * (1 - lerp) + motionZ * lerp;*/
        }
    }

    private float getMovementFactor() {
        return 0.12F;
    }

    private float getLandMovementFactor() {
        float base = getMovementFactor();
        if (npc.isSprinting()) base *= 1.3F;
        return base;
    }

    private float getAirMovementFactor() {
        float base = getMovementFactor() * 0.2F;
        if (npc.isSprinting()) base *= 1.3F;
        return base;
    }

    private float getWaterMovementFactor() {
        float base = getMovementFactor() * 0.25F;
        if (npc.isSprinting()) base *= 1.3F;
        return base;
    }

    /**
     * Makes the NPC jump using a set y velocity of 0.42.
     * <p>This method will only be successful if {@link NPC#hasAI()} and {@link NPC#isOnGround()} are true.</p>
     * <p>It will also not work if the NPC is in a liquid, climbing, or flying.</p>
     * <p>Jump boost effects and sprint jumping is taken into account.</p>
     * @see <a href="https://minecraft.gamepedia.com/Attribute#Movement">Movement Attributes</a>
     */
    public void jump() {
        if (npc.hasAI() && npc.isOnGround() && jumpTicks == 0) {
            jumpTicks = 10;
            this.motionY = 0.42;
            // apply jump boost
            if (npc.getEntity().hasPotionEffect(PotionEffectType.JUMP)) {
                this.motionY += (npc.getEntity().getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1) * 0.1F;
            }
            //((CraftPlayer) npc.getEntity()).getHandle().jumpFromGround();
            // apply sprint jump boost
/*            if (npc.isSprinting() && npc.isJumping()) {
                float f = npc.getYaw() * 0.017453292F;
                moveForward -= Math.sin(f) * 0.2F;
                moveStrafing += Math.cos(f) * 0.2F;
            }*/
        }
    }

    private static Vector worldToInputVector(Vector worldVector, float speed, float yaw) {
        double d = worldVector.length() * worldVector.length();
        if (d < 1.0E-7) {
            return new Vector(0, 0, 0);
        } else {
            Vector vec3d = (d > 1.0 ? worldVector.normalize() : worldVector).multiply(speed);
            float f = (float) Math.sin(yaw * 0.017453292F);
            float g = (float) Math.cos(yaw * 0.017453292F);
            return new Vector(vec3d.getX() * (double) g + vec3d.getZ() * (double) f,
                    vec3d.getY(), vec3d.getZ() * (double) g - vec3d.getX() * (double) f);
        }
    }

    private float getSlipperiness() {
        double minYBounds = npc.getEntity().getBoundingBox().getMinY();
        Block groundBlock = new Location(npc.getWorld(), npc.getX(), minYBounds - 0.5F, npc.getZ()).getBlock();
        return npc.isOnGround() ? groundBlock.getType().getSlipperiness() : 1F;
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

    /**
     * Sets the NPC's target coordinates to the given x and z coordinates.
     * <p>This method will only see results if {@link NPC#hasAI()} is true.</p>
     * @param x The x coordinate to move to
     * @param z The z coordinate to move to
     */
    public void moveTo(double x, double z) {
        targetX = x;
        targetZ = z;
        travelling = true;
    }

    /**
     * Prohibits the NPC from calculating voluntary movement.
     * <p>The method will not instantly stop all movement, as momentum and acceleration will still be taken into account.</p>
     */
    public void stop() {
        moveForward = 0.0D;
        moveStrafing = 0.0D;
        targetX = npc.getLocation().getX();
        targetZ = npc.getLocation().getZ();
    }

    /**
     * Applies a 'knockback' velocity to the NPC.
     * <p>This method will only be successful if {@link NPC#hasAI()} is true.</p>
     */
    public void applyKnockback(Location source) {
        if (!npc.hasAI()) return;
        Vector vel = npc.getLocation().toVector().subtract(source.toVector()).setY(0).normalize().multiply(0.4);
        if (npc.isOnGround()) vel.setY(0.15);
        //timeoutTicks = 10;
        motionX += vel.getX();
        motionY += vel.getY();
        motionZ += vel.getZ();
    }

    /**
     * Applies a specified force to the NPC's y velocity.
     * <p>This method will only be successful if {@link NPC#hasAI()} is true.</p>
     * @implNote This is only used by the {@link Navigation} class to step up blocks. It is not recommended to use this method.
     * @param force The force to apply
     */
    public void applyUpwardForce(double force) {
        if (npc.hasAI()) {
            motionY = force;
        }
    }

    /**
     * Applies a specified vector to the NPC's velocity.
     * @param velocity The vector to apply
     */
    public void addVelocity(Vector velocity) {
        this.motionX += velocity.getX();
        this.motionY += velocity.getY();
        this.motionZ += velocity.getZ();
    }

    /**
     * Gets the NPC's current velocity as a vector.
     * @return The NPC's current velocity
     */
    public Vector getVelocity() {
        return new Vector(motionX, motionY, motionZ);
    }

    /**
     * Gets the target x coordinate.
     * @return The target x coordinate
     */
    public double getTargetX() {
        return targetX;
    }

    /**
     * Gets the target z coordinate.
     * @return The target z coordinate
     */
    public double getTargetZ() {
        return targetZ;
    }

    /**
     * Sets the NPC's state to climbing.
     * <p>This only adjusts how movement is calculated.</p>
     * @implNote This method is only used by the {@link Navigation} class and is not recommended to be used.
     * @param climbing Whether the NPC is climbing
     */
    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
    }

    /**
     * Gets whether the NPC is climbing.
     * @return Whether the NPC is climbing
     */
    public boolean isClimbing() {
        return climbing;
    }
}
