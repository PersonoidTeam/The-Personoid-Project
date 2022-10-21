package com.personoid.api.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.personoid.api.npc.injection.CallbackInfo;
import com.personoid.api.npc.injection.InjectionInfo;
import com.personoid.api.utils.packet.Packages;
import com.personoid.api.utils.packet.Packets;
import com.personoid.api.utils.packet.ReflectionUtils;
import com.personoid.api.utils.types.HandEnum;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCOverrides implements Listener {
    private Object base;
    private final NPC npc;

    public NPCOverrides(NPC npc) {
        this.npc = npc;
        JavaPlugin userPlugin = JavaPlugin.getProvidingPlugin(NPCOverrides.class);
        Bukkit.getPluginManager().registerEvents(this, userPlugin);
    }

    String[] getMethods() {
        // TRANSLATIONS: tick
        return new String[] {
            "k"
        };
    }

    public void setBase(Object base) {
        this.base = base;
        npc.init();
    }

    public Object getBase() {
        return base;
    }

    public Method get(String method) {
        try {
            Method methodField = getClass().getMethod(method);
            methodField.setAccessible(true);
            return methodField;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    // region UTILS

    public void invoke(String methodName, Object... args) {
        try {
            List<Class<?>> argTypes = new ArrayList<>();
            for (Object arg : args) argTypes.add(arg.getClass());
            Method method = base.getClass().getMethod(methodName, argTypes.toArray(new Class[0]));
            method.setAccessible(true);
            method.invoke(base, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T invoke(Class<T> type, String methodName, Object... args) {
        try {
            List<Class<?>> argTypes = new ArrayList<>();
            for (Object arg : args) argTypes.add(arg.getClass());
            Method method = base.getClass().getMethod(methodName, argTypes.toArray(new Class[0]));
            method.setAccessible(true);
            return (T) method.invoke(base, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getField(Class<T> type, String fieldName, Object... args) {
        try {
            Field field = base.getClass().getField(fieldName);
            field.setAccessible(true);
            return (T) field.get(base);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void setField(String fieldName, T value) {
        try {
            Field field = base.getClass().getField(fieldName);
            field.setAccessible(true);
            field.set(base, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void modInt(String fieldName, int modifier) {
        try {
            Field field = base.getClass().getField(fieldName);
            field.setAccessible(true);
            field.set(base, getField(int.class, fieldName) + modifier);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // endregion

    // NPC METHODS + VARIABLES
    private double lastYIncrease;
    private int aliveTicks;
    private int groundTicks;
    private final Map<Material, Integer> itemCooldowns = new HashMap<>();
    private int handUsing = -1;
    private int itemUsingTicks;

    public void k() { // tick
        loadChunks();
        aliveTicks++;
        if (aliveTicks % 5 != 0) {
            updateSkin();
        }
        //invoke("k"); // tick
        if (!invoke(Boolean.class, "bo")) return; // isAlive
        double yPos = invoke(double.class, "dh"); // getY
        if (aliveTicks == 1) {
            lastYIncrease = yPos;
        }
        if (getField(int.class, "aK") > 0) modInt("aK", -1); // hurtTime
        if (npc.onGround()) {
            if (groundTicks < Integer.MAX_VALUE) {
                groundTicks++;
            }
        } else groundTicks = 0;
        float health = invoke(float.class, "ef"); // getHealth
        float maxHealth = invoke(float.class, "et"); // getMaxHealth
        float amount = health < maxHealth - 0.05F ? health + 0.05F : maxHealth; // 0.1F = natual regen speed (full saturation)
        getEntity().setHealth(amount);
        //invoke("c", amount); // setHealth, FIXME: method not found?!?!?
        if (yPos < -64) invoke("av"); // outOfWorld
        fallDamageCheck();
        // FIXME: swimming not working
/*        if (inWater() && targetLoc.getY() - 1F < getLocation().getY()) {
            if (!isSwimming()) setSwimming(true);
        } else if (isSwimming()) {
            setSwimming(false);
        }*/
        for (Material material : itemCooldowns.keySet()) {
            int cooldown = itemCooldowns.get(material);
            if (cooldown > 0) itemCooldowns.put(material, cooldown - 1);
            else itemCooldowns.remove(material);
        }
        if (handUsing != -1) itemUsingTicks++;
        Packets.updateEntityData(getEntity()).send();
        npc.tick();
    }

    private void loadChunks() {
        World world = getEntity().getWorld();
        Chunk chunk = getEntity().getLocation().getChunk();
        for (int i = chunk.getX() - 1; i <= chunk.getX() + 1; i++) {
            for (int j = chunk.getZ() - 1; j <= chunk.getZ() + 1; j++) {
                Chunk neighbor = world.getChunkAt(i, j);
                if (!neighbor.isLoaded()) {
                    neighbor.load();
                }
            }
        }
    }

    private void fallDamageCheck() {
        // FIXME: still a little broken
        double yPos = invoke(double.class, "dh"); // getY
        if (invoke(boolean.class, "aw")) { // onGround
            float damage = (float) (lastYIncrease - yPos - 3F);
            if (damage > 0) {
                getEntity().setLastDamageCause(new EntityDamageEvent(getEntity(), EntityDamageEvent.DamageCause.FALL, damage));
                getEntity().damage(damage);
                //Class<?> damageSourceClass = ReflectionUtils.findClass(Packages.DAMAGE_SOURCE, "DamageSource");
                //Object fall = ReflectionUtils.getField(damageSourceClass, "k"); // FALL
                //if (damage > 0D) invoke("a", fall, damage); // hurt FIXME: method not found?!?!?
            }
        }
        if (npc.getMoveController().getVelocity().getY() > 0) lastYIncrease = yPos;
    }

/*    private void push(Entity entity) {
        if (!this.isPassengerOfSameVehicle(entity) && !entity.noPhysics && !this.noPhysics) {
            double d0 = entity.getX() - this.getX();
            double d1 = entity.getX() - this.getZ();
            double d2 = Mth.absMax(d0, d1);
            if (d2 >= 0.009999999776482582D) {
                d2 = Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;
                if (d3 > 1D) {
                    d3 = 1D;
                }
                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;
                if (!this.isVehicle()) {
                    //moveController.addVelocity(new Vector(-d0, 0D, -d1));
                }
                if (!entity.isVehicle()) {
                    entity.push(d0, 0D, d1);
                }
            }
        }
    }*/

    @EventHandler
    private void damage(EntityDamageByEntityEvent event) {
        if (event.getEntity() != getEntity()) return;
        Entity attacker = event.getDamager();
        double damage = event.getDamage();
        InjectionInfo info = npc.injector.callHookReturn("damage", new CallbackInfo<>(double.class), damage);
        if (info.isModified()) damage = info.getParameter().getValue(Double.class);
        ItemStack mainHand = npc.getInventory().getOffhandItem();
        ItemStack offHand = npc.getInventory().getSelectedItem();
        if ((mainHand != null && mainHand.getType() == Material.SHIELD) || (offHand != null && offHand.getType() == Material.SHIELD)) {
            if (getItemCooldown(Material.SHIELD) <= 0 && isUsingItem()) {
                // check if angle is within 120 degrees
                Vector direction = attacker.getLocation().getDirection();
                Vector npcDirection = getLocation().getDirection();
                double angle = direction.angle(npcDirection);
                if (angle < 2.0943951023931953D && itemUsingTicks >= 5) {
                    // shield block
                    LivingEntity living = (LivingEntity) attacker;
                    EntityEquipment equipment = living.getEquipment();
                    if (equipment != null && equipment.getItemInMainHand().getType().name().contains("_AXE")) {
                        getEntity().playEffect(EntityEffect.SHIELD_BREAK);
                        setItemCooldown(Material.SHIELD, 100);
                    }
                    getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
                    event.setCancelled(true);
                    return;
                }
            }
        }
        event.setDamage(damage);
        npc.getMoveController().applyKnockback(attacker.getLocation());
    }

    @EventHandler
    private void damage(EntityDamageEvent event) {
        if (event.getEntity() != getEntity()) return;
        double damage = event.getDamage();
        InjectionInfo info = npc.injector.callHookReturn("damage", new CallbackInfo<>(double.class), damage);
        if (info.isModified()) damage = info.getParameter().getValue(Double.class);
        event.setDamage(damage);
    }

    private Object getNMSHand(HandEnum hand) {
        Class<?> interactionHandClass = ReflectionUtils.findClass(Packages.INTERACTION_HAND, "EnumHand");
        String nmsHandName = hand == HandEnum.RIGHT || hand == HandEnum.DOMINANT ? "MAIN_HAND" : "OFF_HAND";
        return ReflectionUtils.getEnum(interactionHandClass, nmsHandName);
    }

    public void startUsingItem(HandEnum hand) {
        invoke("c", getNMSHand(hand));
        if (handUsing != (hand == HandEnum.LEFT ? 0 : 1)) itemUsingTicks = 0;
        handUsing = hand == HandEnum.LEFT ? 0 : 1;
    }

    public boolean isUsingItem() {
        return invoke(boolean.class, "eT");
    }

    public void stopUsingItem() {
        invoke("eZ");
        handUsing = -1;
        itemUsingTicks = 0;
    }

    public int getItemUsingTicks() {
        return itemUsingTicks;
    }

    public void swingHand(HandEnum hand) {
        invoke("a", getNMSHand(hand));
    }

    public int getItemCooldown(Material material) {
        return itemCooldowns.getOrDefault(material, 0);
    }

    public void setItemCooldown(Material material, int ticks) {
        itemCooldowns.put(material, ticks);
    }

    public void setVisibilityTo(Player player, boolean visible) {
        if (visible) {
            Packets.showPlayer(npc.getEntity()).send(player);
        } else {
            Packets.hidePlayer(npc.getEntity()).send(player);
        }
    }

    public Player getEntity() {
        return invoke(Player.class, "getBukkitEntity");
    }

    public void move(Vector vector) {
        ((ServerPlayer)base).move(MoverType.SELF, new Vec3(vector.getX(), vector.getY(), vector.getZ()));
    }

    // convert bukkit vector to nms vector
    public Object getVec3(Vector vector) {
        return new Vec3(vector.getX(), vector.getY(), vector.getZ());
    }

    public Location getLocation() {
        return getEntity().getLocation();
    }

    public void setLocation(Location location) {
        getEntity().teleport(location);
    }

    public void setRotation(float yaw, float pitch) {
        ((ServerPlayer)base).setXRot(pitch);
        ((ServerPlayer)base).setYRot(yaw);
    }

    public void updateSkin() {
        Skin skin = npc.getProfile().getSkin();
        GameProfile profile = ((ServerPlayer)base).getGameProfile();
        profile.getProperties().put("textures", new Property("textures", skin.getTexture(), skin.getSignature()));
    }
}
