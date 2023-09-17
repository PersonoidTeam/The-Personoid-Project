package com.personoid.api.npc;

import com.google.common.collect.ForwardingMultimap;
import com.personoid.api.npc.injection.CallbackInfo;
import com.personoid.api.npc.injection.InjectionInfo;
import com.personoid.api.utils.Parameter;
import com.personoid.api.utils.types.HandEnum;
import com.personoid.nms.NMS;
import com.personoid.nms.mappings.NMSClass;
import com.personoid.nms.mappings.NMSField;
import com.personoid.nms.mappings.NMSMethod;
import com.personoid.nms.packet.NMSReflection;
import com.personoid.nms.packet.Package;
import com.personoid.nms.packet.Packages;
import com.personoid.nms.packet.Packets;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class NPCOverrides implements Listener {
    public static final NMSClass SERVER_PLAYER = Package.SERVER_PLAYER_CLASS.getMappedClass();
    public static final String METHOD_TICK = getObfuscatedMethodName("tick");

    private Object base;
    private final NPC npc;

    public NPCOverrides(NPC npc) {
        this.npc = npc;
        JavaPlugin userPlugin = JavaPlugin.getProvidingPlugin(NPCOverrides.class);
        Bukkit.getPluginManager().registerEvents(this, userPlugin);
    }

    public void setBase(Object base) {
        this.base = base;
        npc.entity = getEntity();
        init();
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

    private static String getObfuscatedMethodName(String methodName, Parameter... args) {
        Class<?>[] argTypes = Arrays.stream(args).map(Parameter::getType).toArray(Class[]::new);
        NMSClass currentClass = SERVER_PLAYER;
        while (currentClass != null) {
            NMSMethod method = currentClass.getMethod(methodName, argTypes);
            if (method != null) {
                return method.getObfuscatedName();
            } else {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    public void invoke(String methodName, Parameter... args) {
        invoke(null, methodName, args);
    }

    public <T> T invoke(Class<T> returnType, String methodName, Parameter... args) {
        Class<?>[] argTypes = Arrays.stream(args).map(Parameter::getType).toArray(Class[]::new);
        NMSClass currentClass = SERVER_PLAYER;
        while (currentClass != null) {
            NMSMethod method = currentClass.getMethod(methodName, argTypes);
            if (method != null) {
                Object[] argValues = Arrays.stream(args).map(Parameter::getValue).toArray(Object[]::new);
                return method.invoke(base, argValues);
            } else {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    public <T> T getField(Class<T> returnType, String fieldName) {
        NMSClass currentClass = SERVER_PLAYER;
        while (currentClass != null) {
            NMSField field = currentClass.getField(fieldName);
            if (field != null) {
                return field.get(base);
            } else {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }


    public <T> void setField(String fieldName, T value) {
        NMSClass currentClass = SERVER_PLAYER;
        while (currentClass != null) {
            NMSField field = currentClass.getField(fieldName);
            if (field != null) {
                field.set(base, value);
            } else {
                currentClass = currentClass.getSuperclass();
            }
        }
    }

    public void modInt(String fieldName, int modifier) {
        NMSClass currentClass = SERVER_PLAYER;
        while (currentClass != null) {
            NMSField field = currentClass.getField(fieldName);
            if (field != null) {
                field.set(base, (int) field.get(base) + modifier);
            } else {
                currentClass = currentClass.getSuperclass();
            }
        }
    }

    // endregion

    public void onSpawn() {

    }

    public void init() {
        updateSkin();
        npc.getInventory().updateVisuals();
        Packets.updateEntityData(getEntity()).send();
    }

    // NPC METHODS + VARIABLES
    private double lastYIncrease;
    private int aliveTicks;
    private int groundTicks;
    private final Map<Material, Integer> itemCooldowns = new HashMap<>();
    private int handUsing = -1;
    private int itemUsingTicks;
    //private final Set<UUID> playersInRange = new HashSet<>();

    public void tick() {
        loadChunks();
        aliveTicks++;
        if (!invoke(boolean.class, "isAlive")) return;
        //invoke("baseTick"); // baseTick
        double yPos = invoke(double.class, "getY");
        if (aliveTicks == 1) {
            lastYIncrease = yPos;
        }
        //if (getField(int.class, "hurtTime") > 0) modInt("hurtTime", -1);
        if (npc.isOnGround()) {
            if (groundTicks < Integer.MAX_VALUE) {
                groundTicks++;
            }
        } else groundTicks = 0;
        float health = invoke(float.class, "getHealth");
        float maxHealth = invoke(float.class, "getMaxHealth");
        float amount = health < maxHealth - 0.05F ? health + 0.05F : maxHealth; // 0.1F = natual regen speed (full saturation)
        invoke("setHealth", new Parameter(float.class, amount));
        if (yPos < -64) invoke("outOfWorld");
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
        // TODO: hacky workaround (fixes npc spawning invisible when a player is out of view of where it spawned)
/*        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getLocation().distance(npc.getLocation()) <= 48F) {
                if (!playersInRange.contains(player.getUniqueId())) {
                    player.showPlayer(JavaPlugin.getProvidingPlugin(NPCOverrides.class), getEntity());
                    if (!npc.getProfile().isVisibleInTab()) {
                        Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(NPCOverrides.class), () ->
                                Packets.hidePlayer(npc.getEntity()).send(player), 2);
                    }
                    playersInRange.add(player.getUniqueId());
                }
            } else {
                if (playersInRange.contains(player.getUniqueId())) {
                    player.hidePlayer(JavaPlugin.getProvidingPlugin(NPCOverrides.class), getEntity());
                    playersInRange.remove(player.getUniqueId());
                }
            }
        });*/
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
        double yPos = invoke(double.class, "getY");
        if (npc.isOnGround() && !npc.isInWater()) { // onGround
            float damage = (float) (lastYIncrease - yPos - 3F);
            if (damage > 0) {
                getEntity().setLastDamageCause(new EntityDamageEvent(getEntity(), EntityDamageEvent.DamageCause.FALL, damage));
                getEntity().damage(damage);
                //Class<?> damageSourceClass = ReflectionUtils.findClass(Packages.DAMAGE_SOURCE, "DamageSource");
                //Object fall = ReflectionUtils.getField(damageSourceClass, "k"); // FALL
                //if (damage > 0D) invoke("a", fall, damage); // hurt FIXME: method not found?!?!?
            }
            lastYIncrease = yPos;
        }
        if (npc.getMoveController().getVelocity().getY() > 0) lastYIncrease = yPos;
    }

/*    private void g(Entity entity) { // push
        ServerPlayer player = (ServerPlayer) base;
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!player.isPassengerOfSameVehicle(nmsEntity) && !nmsEntity.noPhysics && !player.noPhysics) {
            double d0 = nmsEntity.getX() - nmsEntity.getX();
            double d1 = nmsEntity.getX() - nmsEntity.getZ();
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
                if (!player.isVehicle()) {
                    npc.getMoveController().addVelocity(new Vector(-d0, 0D, -d1));
                }
                if (!nmsEntity.isVehicle()) {
                    nmsEntity.push(d0, 0D, d1);
                }
            }
        }
    }*/

    @EventHandler
    private void damage(EntityDamageByEntityEvent event) {
        if (event.getEntity() != getEntity()) return;
        if (npc.isInvulnerable()) {
            event.setCancelled(true);
            return;
        }
        Entity attacker = event.getDamager();
        double damage = event.getDamage();
        InjectionInfo info = npc.injector.callHookReturn("damage", new CallbackInfo<>(double.class), damage);
        if (info.isModified()) damage = info.getParameter().getValue(Double.class);
        ItemStack mainHand = npc.getInventory().getOffhandItem();
        ItemStack offHand = npc.getInventory().getSelectedItem();
        if ((mainHand != null && mainHand.getType() == Material.SHIELD) || (offHand != null && offHand.getType() == Material.SHIELD)) {
            if (attacker instanceof LivingEntity) {
                if (getItemCooldown(Material.SHIELD) <= 0 && isUsingItem()) {
                    // check if angle is within 120 degrees
                    Vector direction = attacker.getLocation().getDirection();
                    Vector npcDirection = npc.getLocation().getDirection();
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
        }
        event.setDamage(damage);
        npc.getMoveController().applyKnockback(attacker.getLocation());
        //npc.getProfile().setName("Bread and Bacon"); // TESTING
    }

    @EventHandler
    private void damage(EntityDamageEvent event) {
        if (event.getEntity() != getEntity()) return;
        if (npc.isInvulnerable()) {
            event.setCancelled(true);
            return;
        }
        double damage = event.getDamage();
        InjectionInfo info = npc.injector.callHookReturn("damage", new CallbackInfo<>(double.class), damage);
        if (info.isModified()) damage = info.getParameter().getValue(Double.class);
        event.setDamage(damage);
    }

    private Object getNMSHand(HandEnum hand) {
        Class<?> interactionHandClass = NMSReflection.findClass(Packages.INTERACTION_HAND, "EnumHand");
        String nmsHandName = hand == HandEnum.RIGHT || hand == HandEnum.DOMINANT ? "MAIN_HAND" : "OFF_HAND";
        return NMSReflection.getEnum(interactionHandClass, nmsHandName);
    }

    public void startUsingItem(HandEnum hand) {
        invoke("startUsingItem", Parameter.of(getNMSHand(hand)));
        if (handUsing != (hand == HandEnum.LEFT ? 0 : 1)) itemUsingTicks = 0;
        handUsing = hand == HandEnum.LEFT ? 0 : 1;
    }

    public boolean isUsingItem() {
        return invoke(boolean.class, "isUsingItem");
    }

    public void stopUsingItem() {
        invoke("stopUsingItem");
        handUsing = -1;
        itemUsingTicks = 0;
    }

    public int getItemUsingTicks() {
        return itemUsingTicks;
    }

    public void swingHand(HandEnum hand) {
        invoke("swing", Parameter.of(getNMSHand(hand)));
    }

    public int getItemCooldown(Material material) {
        return itemCooldowns.getOrDefault(material, 0);
    }

    public void setItemCooldown(Material material, int ticks) {
        itemCooldowns.put(material, ticks);
    }

    public void setVisibilityTo(Player player, boolean visible) {
        if (visible) {
            Packets.showPlayer(npc.getEntity(), npc.getProfile().isVisibleInTab()).send(player);
        } else {
            Packets.hidePlayer(npc.getEntity()).send(player);
        }
    }

    public Player getEntity() {
        return invoke(Player.class, "getBukkitEntity");
    }

    public void move(Vector vector) {
        NMSClass moverType = Package.minecraft("world.entity.MoverType").getMappedClass();
        Object self = moverType.getField("SELF").get(base);
        invoke("move", Parameter.of(self), Parameter.of(NMS.toVec3(vector)));
    }

    public void setLocation(Location location) {
        getEntity().teleport(location);
    }

    public void setRotation(float yaw, float pitch) {
        setYaw(yaw);
        setPitch(pitch);
    }

    public void setYaw(float yaw) {
        invoke("setYRot", new Parameter(float.class, yaw));
        invoke("setYHeadRot", new Parameter(float.class, yaw));
        invoke("setYBodyRot", new Parameter(float.class, yaw));
    }

    public void setPitch(float pitch) {
        invoke("setXRot", new Parameter(float.class, pitch));
    }

    public float getYaw() {
        return invoke(float.class, "getYRot");
    }

    public float getPitch() {
        return invoke(float.class, "getXRot");
    }

    public void updateSkin() {
        Skin skin = npc.getProfile().getSkin();
        Package gameProfile = Package.mojang("authlib.GameProfile");
        Object profile = invoke(gameProfile.getRawClass(), "getGameProfile");
        Object properties = gameProfile.getMappedClass().getMethod("getProperties").invoke(profile);
        Class<?> propertyClass = NMSReflection.findClass("com.mojang.authlib.properties", "Property");
        try {
            Object property = propertyClass.getConstructor(String.class, String.class, String.class)
                    .newInstance("textures", skin.getTexture(), skin.getSignature());
            ((ForwardingMultimap)properties).put("textures", property);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create property whilst updating skin", e);
        }
        NMS.setEntityData(npc, 17, "byte", (byte) 0x00);
        NMS.setEntityData(npc, 17, "byte", (byte) 0xFF);
    }

    public void setJumping(boolean jumping) {
        invoke("setJumping", new Parameter(boolean.class, jumping));
    }

    public boolean isJumping() {
        return getField(boolean.class, "jumping");
    }

    public void setPose(Pose pose) {
/*
        String nmsName = "";
        switch (pose) {
            case STANDING:
                nmsName = "STANDING";
                break;
            case SNEAKING:
                nmsName = "CROUCHING";
                break;
            case SLEEPING:
                nmsName = "SLEEPING";
                break;
            case SWIMMING:
                nmsName = "SWIMMING";
                break;
            case FLYING:
                nmsName = "FALL_FLYING";
                break;
            case SPINNING:
                nmsName = "SPIN_ATTACK";
                break;
            case DYING:
                nmsName = "DYING";
                break;
        }
        Class<?> entityPose = ReflectionUtils.findClass(Packages.ENTITY, "EntityPose");
        Object nmsEnum = ReflectionUtils.getEnum(entityPose, nmsName);
        invoke("setPose", nmsEnum); // setPose
*/

        // new version
        NMSClass poseClass = Package.ENTITY.sub("Pose").getMappedClass();
        Object nmsPose = poseClass.getField(pose.name()).get(null);
        invoke("setPose", new Parameter(poseClass.getRawClass(), nmsPose));
    }

    public Pose getPose() {
        NMSClass entityPose = Package.ENTITY.sub("Pose").getMappedClass();
        Object pose = invoke(entityPose.getRawClass(), "getPose");
        String nmsName = pose.toString();
        switch (nmsName) {
            case "STANDING":
                return Pose.STANDING;
            case "CROUCHING":
                return Pose.SNEAKING;
            case "SLEEPING":
                return Pose.SLEEPING;
            case "SWIMMING":
                return Pose.SWIMMING;
            case "FALL_FLYING":
                return Pose.FLYING;
            case "SPIN_ATTACK":
                return Pose.SPINNING;
            case "DYING":
                return Pose.DYING;
        }
        return Pose.STANDING;
    }
}
