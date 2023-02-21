package com.personoid.api.npc;

import com.personoid.api.ai.NPCBrain;
import com.personoid.api.ai.looking.LookController;
import com.personoid.api.ai.movement.MoveController;
import com.personoid.api.ai.movement.Navigation;
import com.personoid.api.npc.injection.Feature;
import com.personoid.api.npc.injection.Injector;
import com.personoid.api.pathfinding.utils.BlockPos;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.types.HandEnum;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class NPC {
    private final NPCOverrides overrides = new NPCOverrides(this);
    private final List<Feature> features = new ArrayList<>();
    private final GameProfile profile;

    private final Navigation navigation = new Navigation(this);
    private final MoveController moveController = new MoveController(this);
    private final LookController lookController = new LookController(this);

    private final NPCBrain brain = new NPCBrain(this);
    private final BlockBreaker blockBreaker = new BlockBreaker(this);
    private final NPCInventory inventory = new NPCInventory(this);
    final Injector injector = new Injector(this);

    Player entity;
    private Pose pose = Pose.STANDING;
    private boolean hasAI = true;
    private boolean hasGravity = true;
    private boolean isInvulnerable;
    private boolean isPushable = true;
    private boolean isJumping;
    private boolean canSprint = true;
    private boolean isSprinting;


    public NPC(GameProfile profile) {
        this.profile = profile;
        profile.setNPC(this);
    }

    NPCOverrides getOverrides() {
        return overrides;
    }

    void init() {
        injector.callHook("init");
    }

    public void respawn() {
        overrides.onSpawn();
        overrides.init();
        injector.callHook("respawn");
    }

    public void remove() {
        blockBreaker.stop();
    }

    public void tick() {
        moveController.tick();
        lookController.tick();
        if (hasAI) {
            brain.tick();
            navigation.tick();
            blockBreaker.tick();
            inventory.tick();
        }
        injector.callHook("tick");
    }

    public void addFeature(Feature feature) {
        features.add(feature);
    }

    public void removeFeature(Feature feature) {
        features.remove(feature);
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void teleport(Location location) {
        getEntity().teleport(location);
    }

    public void move(Vector vector) {
        overrides.move(vector);
    }

    public boolean isOnGround() {
        //return ((CraftPlayer) entity).getHandle().onGround;
        double vy = moveController.getVelocity().getY();
        if (vy > 0) return false;
        World world = getEntity().getWorld();
        BoundingBox box = getEntity().getBoundingBox();
        double[] xVals = new double[] { box.getMinX(), box.getMaxX() };
        double[] zVals = new double[] { box.getMinZ(), box.getMaxZ() };
        for (double x : xVals) {
            for (double z : zVals) {
                Location loc = new Location(world, x, getLocation().getY() - 0.01, z);
                Block block = world.getBlockAt(loc);
                if (block.getType().isSolid() && LocationUtils.solidBoundsAt(loc)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInWater() {
        Location loc = getLocation().clone();
        for (int i = 0; i <= 2; i++) {
            Material type = loc.getBlock().getType();
            if (type == Material.WATER || type == Material.LAVA) {
                return true;
            }
            loc.add(0, 0.9, 0);
        }
        return false;
    }

    public boolean isMovingInWater() {
        Location loc = getLocation().clone().add(0, -0.8F, 0);
        for (int i = 0; i <= 2; i++) {
            Material type = loc.getBlock().getType();
            if (type == Material.WATER || type == Material.LAVA) {
                Vector vel = moveController.getVelocity();
                return vel.getX() != 0 || vel.getZ() != 0;
            }
            loc.add(0, 0.9, 0);
        }
        return false;
    }

    public void swingHand(HandEnum hand) {
        overrides.swingHand(hand);
    }

    public void startUsingItem(HandEnum hand) {
        overrides.startUsingItem(hand);
        if (hand == HandEnum.LEFT) {
            if (inventory.getOffhandItem().getType() == Material.SHIELD) {
                canSprint = false;
                setSprinting(false);
            }
        } else {
            if (inventory.getSelectedItem().getType() == Material.SHIELD) {
                canSprint = false;
                setSprinting(false);
            }
        }
    }

    public void stopUsingItem() {
        overrides.stopUsingItem();
        canSprint = true;
    }

    public int getItemCooldown(Material material) {
        return overrides.getItemCooldown(material);
    }

    public void setItemCooldown(Material material, int ticks) {
        overrides.setItemCooldown(material, ticks);
    }

    public int getItemUsingTicks() {
        return overrides.getItemUsingTicks();
    }

    public void setVisibilityTo(Player player, boolean visible) {
        overrides.setVisibilityTo(player, visible);
    }

    // region GETTERS AND SETTERS

    public GameProfile getProfile() {
        return profile;
    }

    public Navigation getNavigation() {
        return navigation;
    }

    public MoveController getMoveController() {
        return moveController;
    }

    public LookController getLookController() {
        return lookController;
    }

    public NPCBrain getBrain() {
        return brain;
    }

    public BlockBreaker getBlockBreaker() {
        return blockBreaker;
    }

    public NPCInventory getInventory() {
        return inventory;
    }

    public Location getLocation() {
        return getEntity().getLocation();
    }

    public double getX() {
        return getEntity().getLocation().getX();
    }

    public double getY() {
        return getEntity().getLocation().getY();
    }

    public double getZ() {
        return getEntity().getLocation().getZ();
    }

    public int getBlockX() {
        return getEntity().getLocation().getBlockX();
    }

    public int getBlockY() {
        return getEntity().getLocation().getBlockY();
    }

    public int getBlockZ() {
        return getEntity().getLocation().getBlockZ();
    }

    public BlockPos getBlockPos() {
        return new BlockPos(getLocation().getX(), getLocation().getY(), getLocation().getZ());
    }

    public World getWorld() {
        return getEntity().getWorld();
    }

    public void setPose(Pose pose) {
        this.pose = pose;
        overrides.setPose(pose);
    }

    public Pose getPose() {
        return pose;
    }

    public Player getEntity() {
        return entity;
    }

    public int getEntityId() {
        return getEntity().getEntityId();
    }

    public void setInvulnerable(boolean invulnerable) {
        isInvulnerable = invulnerable;
    }

    public void setPushable(boolean pushable) {
        isPushable = pushable;
    }

    public void setGravity(boolean gravity) {
        hasGravity = gravity;
    }

    public void setAI(boolean ai) {
        hasAI = ai;
    }

    public boolean isInvulnerable() {
        return isInvulnerable;
    }

    public boolean isPushable() {
        return isPushable;
    }

    public boolean hasGravity() {
        return hasGravity;
    }

    public boolean hasAI() {
        return hasAI;
    }

    public void setRotation(float yaw, float pitch) {
        getOverrides().setRotation(yaw, pitch);
    }

    public void setYaw(float yaw) {
        getOverrides().setYaw(yaw);
    }

    public void setPitch(float pitch) {
        getOverrides().setPitch(pitch);
    }

    public float getYaw() {
        return getLocation().getYaw();
    }

    public float getPitch() {
        return getLocation().getPitch();
    }

    public boolean isSpawned() {
        return getEntity() != null;
    }

    public void setSprinting(boolean sprinting) {
        isSprinting = sprinting;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public void setJumping(boolean jumping) {
        isJumping = jumping;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public boolean canSprint() {
        return canSprint;
    }

    public void chat(String message) {
        Bukkit.broadcastMessage("<" + getProfile().getName() + "> " + message);
    }

    public void placeBlock(Location location, Material material) {
        if (!material.isBlock() && !material.isAir()) {
            throw new IllegalArgumentException("Material must be a block");
        }
        if (location.getWorld() != getWorld()) {
            throw new IllegalArgumentException("Location must be in the same world as the NPC");
        }
        if (location.distance(getLocation()) > 5) {
            throw new IllegalArgumentException("Location must be within 5 blocks of the NPC");
        }
        location.getBlock().setType(material);
        Sound placeSound = material.createBlockData().getSoundGroup().getPlaceSound();
        getWorld().playSound(location, placeSound, 1, 1);
    }

    // endregion
}
