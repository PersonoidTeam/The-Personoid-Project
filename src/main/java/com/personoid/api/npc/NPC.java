package com.personoid.api.npc;

import com.personoid.api.ai.NPCBrain;
import com.personoid.api.ai.looking.LookController;
import com.personoid.api.ai.movement.MoveController;
import com.personoid.api.ai.movement.Navigation;
import com.personoid.api.npc.injection.Feature;
import com.personoid.api.npc.injection.Hook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NPC {
    private final NPCFields fields = new NPCFields();
    private final List<Feature> features = new ArrayList<>();
    private final GameProfile profile;

    private final Navigation navigation = new Navigation(this);
    private final MoveController moveController = new MoveController(this);
    private final LookController lookController = new LookController(this);

    private final NPCBrain brain = new NPCBrain(this);
    private final BlockBreaker blockBreaker = new BlockBreaker(this);
    private final NPCInventory inventory = new NPCInventory(this);

    public NPC(GameProfile profile) {
        this.profile = profile;
    }

    NPCFields getFields() {
        return fields;
    }

    // regions UTILS

    private void callHook(String hook, Object... args) {
        for (Feature feature : features) {
            for (Method method : feature.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Hook.class)) {
                    Hook methodHook = method.getAnnotation(Hook.class);
                    if (methodHook.value().equals(hook)) {
                        try {
                            method.invoke(feature, args);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    // endregion

    void tick() {
        brain.tick();
        moveController.tick();
        lookController.tick();
        blockBreaker.tick();
        callHook("tick");
    }

    void damage(EntityDamageEvent.DamageCause cause, double damage) {
        callHook("damage", cause, damage);
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

    public void setInvulnerable(boolean invulnerable) {
        // can't take damage
    }

    public void setPushable(boolean pushable) {
        // can be pushed by other entities
    }

    public void setGravity(boolean gravity) {
        // can be pushed by gravity
    }

    public void setAI(boolean ai) {
        // can use AI
    }

    public void teleport(Location location) {
        // internal
    }

    public boolean inWater() {
        // internal
        return false;
    }

    public boolean onGround() {
        // internal
        return false;
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
        // internal
        return null;
    }

    public Pose getPose() {
        // internal
        return null;
    }

    public void setPose(Pose pose) {
        // internal
    }

    public Player getEntity() {
        return null;
    }

    public int getEntityId() {
        return getEntity().getEntityId();
    }

    // endregion
}
