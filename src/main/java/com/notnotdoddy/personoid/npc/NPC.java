package com.notnotdoddy.personoid.npc;

import com.mojang.authlib.GameProfile;
import com.notnotdoddy.personoid.npc.ai.pathfinding.GoalSelector;
import com.notnotdoddy.personoid.npc.ai.pathfinding.Navigation;
import com.notnotdoddy.personoid.npc.ai.pathfinding.goals.FollowEntityGoal;
import com.notnotdoddy.personoid.utils.npc.SkinUtils;
import com.notnotdoddy.personoid.utils.other.MathUtils;
import com.notnotdoddy.personoid.utils.other.NPCUtils;
import com.notnotdoddy.personoid.utils.packet.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class NPC extends ServerPlayer {
    private final CraftPlayer cp;
    private Vector velocity = new Vector();
    public UUID spawner;

    private final GoalSelector goalSelector = new GoalSelector(this);
    private final Navigation navigation = new Navigation(this, goalSelector);

    private int aliveTicks;
    private int knockbackTicks;
    private int groundTicks;
    private int jumpTicks;

    public NPC(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile, @NotNull Player player) {
        super(minecraftserver, worldserver, gameprofile);
        SkinUtils.setSkin(this, SkinUtils.getFromName("Chalin"));
        entityData.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 0xFF);
        cp = getBukkitEntity();
        spawner = player.getUniqueId();
        registerGoals();
    }

    public void registerGoals() {
        goalSelector.registerGoals(
                new FollowEntityGoal<>(this, Bukkit.getPlayer(spawner))
        );
    }

    public GoalSelector getGoalSelector() {
        return goalSelector;
    }

    public Navigation getNavigation() {
        return navigation;
    }

    public void sendSpawnPackets() {
        PacketUtils.sendAll(new ClientboundSetEntityDataPacket(getId(), entityData, true));
    }

    public void render() {
        PacketUtils.sendAll(
                new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this),
                new ClientboundAddPlayerPacket(this),
                new ClientboundSetEntityDataPacket(getId(), entityData, true)
        );
    }

    public void setVelocity(Vector vector) {
        velocity = vector;
    }

    public void addVelocity(Vector vector) {
        if (MathUtils.isNotFinite(vector)) {
            velocity = vector;
            return;
        }
        velocity.add(vector);
    }

    @Override
    public void tick() {
        loadChunks();
        super.tick();
        if (!isAlive()) return;
        aliveTicks++;

        if (hurtTime > 0) --hurtTime;
        if (knockbackTicks > 0) --knockbackTicks;
        if (jumpTicks > 0) --jumpTicks;

        if (isOnGround()) {
            if (groundTicks < 5) {
                groundTicks++;
            }
        } else {
            groundTicks = 0;
        }

        updateLocation();

        float health = getHealth();
        float maxHealth = getMaxHealth();
        float amount = health < maxHealth - 0.05F ? health + 0.05F : maxHealth;

        setHealth(amount);

        if (getY() < -64) {
            outOfWorld();
        }

        Player player = Bukkit.getPlayer(spawner);
        Location targetLoc = player.getLocation();
        faceLocation(targetLoc);

        // FIXME: swimming not working

/*        if (isInWater() && targetLoc.getY() - 1F < getLocation().getY()) {
            if (!isSwimming()) setSwimming(true);
        } else if (isSwimming()) {
            setSwimming(false);
        }*/

        navigation.tick();
        goalSelector.tick();
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

    public void walk(Vector vel) {
        double max = 0.4;
        Vector sum = velocity.clone().add(vel.setY(0));
        if (sum.length() > max) {
            sum.normalize().multiply(max);
        }
        velocity = sum;
    }

    public boolean isFalling() {
        return velocity.getY() < -0.8F;
    }

    public Location getLocation() {
        return cp.getLocation();
    }

    public void updateLocation() {
        double y;
        MathUtils.clean(velocity);
        if (isInWater()) {
            y = Math.min(velocity.getY() + 0.1, 0.1);
            addFriction(0.8);
            velocity.setY(y);
        } else {
            if (groundTicks != 0) {
                velocity.setY(0);
                addFriction(0.5);
                y = 0;
            } else {
                y = velocity.getY();
                velocity.setY(Math.max(y - 0.1, -3.5));
            }
        }

        this.move(MoverType.SELF, new Vec3(velocity.getX(), y, velocity.getZ()));
    }

    @Override
    public boolean isInWater() {
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

    public void jump() {
        jump(new Vector(0, 0.5, 0));
    }

    public void jump(Vector vel) {
        if (jumpTicks == 0 && groundTicks > 1) {
            jumpTicks = 4;
            velocity = vel;
        }
    }

    @Override
    public boolean isOnGround() {
        // not mine, some smart person figured this out
        double vy = velocity.getY();
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
                if (block.getType().isSolid() && NPCUtils.solidAt(loc)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addFriction(double factor) {
        double min = 0.01;
        double x = velocity.getX();
        double z = velocity.getZ();
        velocity.setX(Math.abs(x) < min ? 0 : x * factor);
        velocity.setZ(Math.abs(z) < min ? 0 : z * factor);
    }

    public void despawn() {
        getBukkitEntity().remove();
    }

    @Override
    public boolean damageEntity0(DamageSource damagesource, float f) {
        boolean damaged = super.damageEntity0(damagesource, f);
        Entity attacker = damagesource.getEntity();

        // TODO: if not damaged, and blocked using shield, play block sound

        if (damaged && attacker != null) {
            // TODO: check if still alive -> if not, call npc event
            knockback(attacker.getBukkitEntity().getLocation());
        }
        return damaged;
    }

    private void knockback(Location from) {
        Vector vel = getLocation().toVector().subtract(from.toVector()).normalize();
        vel.multiply(0.25F).setY(0.5F);
        velocity.add(vel);
    }

    public void faceLocation(Location loc) {
        try {
            Vector dir = loc.toVector().subtract(cp.getLocation().toVector()).normalize();
            Location facing = cp.getLocation().setDirection(dir);
            cp.teleport(facing);
            PacketUtils.sendAll(new ClientboundRotateHeadPacket(cp.getHandle(), (byte) (facing.getYaw() * 256 / 360)));
        } catch (IllegalArgumentException ignored) { }
    }

    // FIXME swimming throws error, npc disappears

    public void setSwimming() {
        setSwimming(true);
    }

    public void setSwimming(boolean swimming) {
        getBukkitEntity().setSwimming(swimming);
        updatePose(swimming ? Pose.SWIMMING : Pose.STANDING);
    }

    public void setSneaking() {
        setSneaking(true);
    }

    public void setSneaking(boolean sneaking) {
        getBukkitEntity().setSneaking(sneaking);
        updatePose(sneaking ? Pose.CROUCHING : Pose.STANDING);
    }

    private void updatePose(Pose pose) {
        setPose(pose);
        entityData.set(new EntityDataAccessor<>(6, EntityDataSerializers.POSE), pose);
        PacketUtils.sendAll(new ClientboundSetEntityDataPacket(getId(), entityData, false));
    }
}
