package com.personoid.api.npc;

import com.personoid.api.utils.packet.Packages;
import com.personoid.api.utils.packet.Packets;
import com.personoid.api.utils.packet.ReflectionUtils;
import com.personoid.api.utils.types.HandEnum;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class NPCOverrides {
    private Object base;
    private final NPC npc;

    public NPCOverrides(NPC npc) {
        this.npc = npc;
    }

    String[] getMethods() {
        return new String[] {
            "k", //tick
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
            Method method = base.getClass().getMethod(methodName);
            method.invoke(base, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T invoke(Class<T> type, String methodName, Object... args) {
        try {
            Method method = base.getClass().getMethod(methodName);
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

    public void k() { // tick
        Bukkit.broadcastMessage("tick");
        loadChunks();
        invoke("tick");
        if (!invoke(Boolean.class, "isAlive")) return;
        aliveTicks++;

        double yPos = invoke(int.class, "getY");

        if (aliveTicks == 1) {
            lastYIncrease = yPos;
        }

        if (getField(int.class, "hurtTime") > 0) modInt("hurtTime", -1);

        if (invoke(boolean.class, "checkGround")) {
            if (groundTicks < Integer.MAX_VALUE) {
                groundTicks++;
            }
        } else groundTicks = 0;

        float health = invoke(float.class, "getHealth");
        float maxHealth = invoke(float.class, "getMaxHealth");
        float amount = health < maxHealth - 0.05F ? health + 0.05F : maxHealth; // 0.1F = natual regen speed (full saturation)

        invoke("setHealth", amount);

        if (yPos < -64) {
            invoke("outOfWorld");
        }

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
        double yPos = invoke(double.class, "getY");
        if (invoke(boolean.class, "onGround")) {
            float damage = (float) (lastYIncrease - yPos - 3F);
            Class<?> damageSourceClass = ReflectionUtils.findClass(Packages.SERVER_WORLD, "DamageSource");
            Object fall = ReflectionUtils.getField(damageSourceClass, "FALL");
            if (damage > 0D) invoke("hurt", fall, damage);
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

/*    public boolean damageEntity0(DamageSource damagesource, float f) {
        Entity attacker = damagesource.getEntity();
        if (attacker != null) {
            ItemStack mainHand = npc.getInventory().getOffhandItem();
            ItemStack offHand = npc.getInventory().getSelectedItem();
            if ((mainHand != null && mainHand.getType() == Material.SHIELD) || (offHand != null && offHand.getType() == Material.SHIELD)) {
                if (getItemCooldown(Material.SHIELD) <= 0 && isUsingItem()) {
                    // check if angle is within 120 degrees
                    Vector direction = attacker.getBukkitEntity().getLocation().getDirection();
                    Vector npcDirection = getLocation().getDirection();
                    double angle = direction.angle(npcDirection);
                    if (angle < 2.0943951023931953D) {
                        // shield block
                        LivingEntity living = (LivingEntity) attacker.getBukkitEntity();
                        EntityEquipment equipment = living.getEquipment();
                        if (equipment != null && equipment.getItemInMainHand().getType().name().contains("_AXE")) {
                            getEntity().playEffect(EntityEffect.SHIELD_BREAK);
                            setItemCooldown(Material.SHIELD, 100);
                        }
                        getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
                        return false;
                    }
                }
            }
            boolean damaged = super.damageEntity0(damagesource, f);
            if (damaged) {
                // TODO: check if still alive -> if not, call npc event
                //moveController.applyKnockback(attacker.getBukkitEntity().getLocation());
            }
            return damaged;
        } else return super.damageEntity0(damagesource, f);
    }*/

    private Object getNMSHand(HandEnum hand) {
        Class<?> interactionHandClass = ReflectionUtils.findClass(Packages.SERVER_WORLD, "InteractionHand");
        String nmsHandName = hand == HandEnum.RIGHT || hand == HandEnum.DOMINANT ? "MAIN_HAND" : "OFF_HAND";
        return ReflectionUtils.getField(interactionHandClass, nmsHandName);
    }

    public void startUsingItem(HandEnum hand) {
        invoke("c", getNMSHand(hand));
    }

    public void stopUsingItem() {
        invoke("eZ");
    }

    public void swingHand(HandEnum hand) {
        invoke("a", getNMSHand(hand));
    }

    public int getItemCooldown(Material material) {
        return itemCooldowns.getOrDefault(material, 0);
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
}
