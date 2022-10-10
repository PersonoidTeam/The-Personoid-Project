package com.personoid.api.npc;

import com.personoid.api.ai.NPCBrain;
import com.personoid.api.ai.looking.LookController;
import com.personoid.api.ai.movement.MoveController;
import com.personoid.api.ai.movement.Navigation;
import com.personoid.api.npc.injection.CallbackInfo;
import com.personoid.api.npc.injection.Feature;
import com.personoid.api.npc.injection.InjectionInfo;
import com.personoid.api.npc.injection.Injector;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.types.HandEnum;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BoundingBox;

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
    private final Injector injector = new Injector(this);

    private Player entity;
    private Location location;
    private Pose pose = Pose.STANDING;
    private boolean hasAI = true;
    private boolean hasGravity = true;
    private boolean isInvulnerable;
    private boolean isPushable = true;


    public NPC(GameProfile profile) {
        this.profile = profile;
    }

    NPCOverrides getOverrides() {
        return overrides;
    }

    void init() {
        entity = overrides.getEntity();
        injector.callHook("init");
    }

    void tick() {
        brain.tick();
        moveController.tick();
        lookController.tick();
        blockBreaker.tick();
        injector.callHook("tick");
    }

    double damage(EntityDamageEvent.DamageCause cause, double damage) {
        InjectionInfo info = injector.callHookReturn("damage", new CallbackInfo(double.class), cause, damage);
        return info.isModified() ? info.getParameter().getValue(double.class) : damage;
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
        // internal
        this.location = location;
    }

    public boolean inWater() {
        Location loc = getLocation();
        for (int i = 0; i <= 2; i++) {
            Material type = loc.getBlock().getType();
            if (type == Material.WATER || type == Material.LAVA) {
                return true;
            }
            loc.add(0, 0.9, 0);
        }
        return false;
    }

    public boolean onGround() {
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

    public void swingHand(HandEnum hand) {
        overrides.swingHand(hand);
    }

    public void startUsingItem(HandEnum hand) {
        overrides.startUsingItem(hand);
    }

    public void stopUsingItem() {
        overrides.stopUsingItem();
    }

    public int getItemCooldown(Material material) {
        return overrides.getItemCooldown(material);
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
        return location;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
        // internal update
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

    // endregion
}
