package com.personoid.v1_18_R2;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.personoid.api.ai.NPCBrain;
import com.personoid.api.ai.looking.LookController;
import com.personoid.api.ai.movement.MoveController;
import com.personoid.api.ai.movement.Navigation;
import com.personoid.api.npc.BlockBreaker;
import com.personoid.api.npc.NPC;
import com.personoid.api.npc.NPCInventory;
import com.personoid.api.npc.Skin;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.packet.Packets;
import com.personoid.api.utils.types.HandEnum;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class NPC_1_18_R2 extends ServerPlayer implements NPC {
    private CraftPlayer cp;

    private final Navigation navigation = new Navigation(this);

    private final MoveController moveController = new MoveController(this);
    private final LookController lookController = new LookController(this);

    private final NPCBrain brain = new NPCBrain(this);
    private final BlockBreaker blockBreaker = new BlockBreaker(this);
    private final NPCInventory inventory = new NPCInventory(this);

    private int deadTicks;
    private int aliveTicks;
    private int groundTicks;

    private boolean sneaking;
    private double lastYIncrease;
    private final Map<Material, Integer> itemCooldowns = new HashMap<>();

    public NPC_1_18_R2(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile) {
        super(minecraftserver, worldserver, gameprofile);
        setSkin(Skin.get("cvjk"));
        entityData.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 0xFF);
        cp = getBukkitEntity();
    }

    public void showToPlayers(Player... players) {
Packets.addPlayer(cp).send(players);
    }

    public void hideToPlayers(Player... players) {
        Packets.removePlayer(cp).send(players);
    }

    @Override
    public void move(Vector vector) {
        this.move(MoverType.SELF, new Vec3(vector.getX(), vector.getY(), vector.getZ()));
    }

    @Override
    public void swingHand(HandEnum handEnum) {
        switch (handEnum) {
            case LEFT -> swing(InteractionHand.OFF_HAND);
            case RIGHT, DOMINANT -> swing(InteractionHand.MAIN_HAND); // TODO: dominant hand
        }
    }

    @Override
    public void setSkin(Skin skin) {
        getGameProfile().getProperties().put("textures", new Property("textures", skin.getTexture(), skin.getSignature()));
    }

    @Override
    public void tick() {
        loadChunks();
        super.tick();
        if (!isAlive()) return;
        aliveTicks++;

        if (aliveTicks == 1) {
            lastYIncrease = getY();
        }

        if (hurtTime > 0) --hurtTime;

        if (checkGround()) {
            if (groundTicks < 5) {
                groundTicks++;
            }
        } else groundTicks = 0;

        float health = getHealth();
        float maxHealth = getMaxHealth();
        float amount = health < maxHealth - 0.005F ? health + 0.005F : maxHealth;

        setHealth(amount);

        if (getY() < -64) {
            outOfWorld();
        }

        fallDamageCheck();

        // FIXME: swimming not working

/*        if (isInWater() && targetLoc.getY() - 1F < getLocation().getY()) {
            if (!isSwimming()) setSwimming(true);
        } else if (isSwimming()) {
            setSwimming(false);
        }*/

        for (Material material : itemCooldowns.keySet()) {
            int cooldown = itemCooldowns.get(material);
            if (cooldown > 0) itemCooldowns.put(material, cooldown - 1);
            else itemCooldowns.remove(material);
        }

        tickComponents();
        updatePose();
    }

    private void tickComponents() {
        navigation.tick();
        moveController.tick();
        lookController.tick();
        brain.tick();
        blockBreaker.tick();
        inventory.tick();
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

    public BlockBreaker getBlockBreaker() {
        return blockBreaker;
    }

    public NPCInventory getNPCInventory() {
        return inventory;
    }

    public NPCBrain getNPCBrain() {
        return brain;
    }

    public Player getEntity() {
        return cp;
    }

    private void loadChunks() {
        World world = cp.getWorld();
        ChunkPos chunkPos = chunkPosition();
        for (int i = chunkPos.x - 1; i <= chunkPos.x + 1; i++) {
            for (int j = chunkPos.z - 1; j <= chunkPos.z + 1; j++) {
                Chunk chunk = world.getChunkAt(i, j);
                if (!chunk.isLoaded()) {
                    chunk.load();
                }
            }
        }
    }

    @Override
    public Location getLocation() {
        return getEntity().getLocation();
    }

    @Override
    public double getXPos() {
        return super.getX();
    }

    @Override
    public double getYPos() {
        return super.getY();
    }

    @Override
    public double getZPos() {
        return super.getZ();
    }

    private void fallDamageCheck() {
        if (onGround()) {
            float damage = (float) (lastYIncrease - getY() - 3F);
            if (damage > 0D) hurt(DamageSource.FALL, damage);
        }
        if (getMoveController().getVelocity().getY() > 0D) lastYIncrease = getY();
    }

    public void sendMessage(String message) {
        // TODO: get working with async
/*        NPCChatEvent event = new NPCChatEvent(this, message);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;*/
        Bukkit.broadcastMessage("<" + getName().getString() + "> " + message);
    }

    public void placeBlock(ItemStack itemStack) {
        Location location = getLocation();
        Block block = LocationUtils.getBlockInFront(location, 1);
        if (block != null) {
            block.setType(itemStack.getType());
            block.setBlockData(itemStack.getType().createBlockData());
            block.getWorld().playSound(block.getLocation(), block.getBlockData().getSoundGroup().getPlaceSound(), 0.5F, 1F);
        }
    }

    @Override
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

    @Override
    public float getYRotation() {
        return super.getYRot();
    }

    @Override
    public void setYRotation(float yRot) {
        super.setYRot(yRot);
    }

    @Override
    public float getXRotation() {
        return super.getXRot();
    }

    @Override
    public void setXRotation(float yRot) {
        super.setYRot(yRot);
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        super.setRot(yaw, pitch);
    }

    @Override
    public void push(Entity entity) {
        // FIXME: direction is always the same
        if (!this.isPassengerOfSameVehicle(entity) && !entity.noPhysics && !this.noPhysics) {
            double d0 = entity.getX() - this.getXPos();
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
                    moveController.addVelocity(new Vector(-d0, 0D, -d1));
                }
                if (!entity.isVehicle()) {
                    entity.push(d0, 0D, d1);
                }
            }
        }
    }

    public int getGroundTicks() {
        return groundTicks;
    }

    public void setGroundTicks(int groundTicks) {
        this.groundTicks = groundTicks;
    }

    @Override
    public boolean onGround() {
        return groundTicks > 0;
    }

    private boolean checkGround() {
        // not mine, some smart person figured this out
        double vy = moveController.getVelocity().getY();
        if (vy > 0) {
            return false;
        }
        World world = getBukkitEntity().getWorld();
        AABB box = getBoundingBox();
        double[] xVals = new double[] { box.minX, box.maxX };
        double[] zVals = new double[] { box.minZ, box.maxZ };
        for (double x : xVals) {
            for (double z : zVals) {
                Location loc = new Location(world, x, getY() - 0.01, z);
                Block block = world.getBlockAt(loc);
                if (block.getType().isSolid() && LocationUtils.solidBoundsAt(loc)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean damageEntity0(DamageSource damagesource, float f) {
        Entity attacker = damagesource.getEntity();
        if (attacker != null) {
            ItemStack mainHand = getNPCInventory().getOffhandItem();
            ItemStack offHand = getNPCInventory().getSelectedItem();
            if (mainHand.getType() == Material.SHIELD || offHand.getType() == Material.SHIELD) {
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
                moveController.applyKnockback(attacker.getBukkitEntity().getLocation());
            }
            return damaged;
        } else return super.damageEntity0(damagesource, f);
    }

    public void remove() {
        blockBreaker.stop();
    }

    // FIXME swimming throws error, npc disappears

    public void setSwimming() {
        setSwimming(true);
    }

    public void setSwimming(boolean swimming) {
        getBukkitEntity().setSwimming(swimming);
        setPose(swimming ? Pose.SWIMMING : Pose.STANDING);
    }

    public void setSneaking() {
        setSneaking(true);
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
        setPose(sneaking ? Pose.CROUCHING : Pose.STANDING);
    }

    public boolean isSneaking() {
        return cp.isSneaking();
    }

    private void updatePose() {
        cp.setSneaking(sneaking);
        ((Player) cp).setSneaking(sneaking);
        //Packets.setEntityData(getId(), entityData, false).send();
        //PacketUtils.send(new ClientboundSetEntityDataPacket(getId(), entityData, false));
    }

    @Override
    public boolean alwaysAccepts() {
        return super.alwaysAccepts();
    }

    @Override
    public int getEntityId() {
        return getId();
    }

    @Override
    public void beginUsingItem(HandEnum hand) {
        startUsingItem(switch (hand) {
            case RIGHT -> InteractionHand.MAIN_HAND;
            case LEFT, DOMINANT -> InteractionHand.OFF_HAND;
        });
    }

    @Override
    public void endUsingItem() {
        super.stopUsingItem();
    }

    @Override
    public void setItemCooldown(Material material, int ticks) {
        itemCooldowns.put(material, ticks);
    }

    @Override
    public int getItemCooldown(Material material) {
        return itemCooldowns.getOrDefault(material, 0);
    }
}
