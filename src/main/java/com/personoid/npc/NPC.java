package com.personoid.npc;

import com.mojang.authlib.GameProfile;
import com.personoid.activites.DanceActivity;
import com.personoid.activites.MineTreeActivity;
import com.personoid.enums.LogType;
import com.personoid.npc.ai.NPCBrain;
import com.personoid.npc.ai.controller.LookController;
import com.personoid.npc.ai.controller.MoveController;
import com.personoid.npc.ai.pathfinding.Navigation;
import com.personoid.utils.BlockBreaker;
import com.personoid.utils.LocationUtils;
import com.personoid.utils.PacketUtils;
import com.personoid.utils.SkinUtils;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class NPC extends ServerPlayer {
    private final CraftPlayer cp;
    public UUID spawner;

    private final Navigation navigation = new Navigation(this);
    private final PathFinder pathFinder;

    private final MoveController moveController = new MoveController(this);
    private final LookController lookController = new LookController(this);
    private final NPCBrain brain = new NPCBrain(this);
    private final BlockBreaker blockBreaker = new BlockBreaker(this);
    private final NPCInventory inventory = new NPCInventory(this);

    private int aliveTicks;
    private int knockbackTicks;
    private int groundTicks;
    private int jumpTicks;
    private Pose pose = Pose.STANDING;
    private boolean sneaking;

    public NPC(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile, @NotNull Player spawner) {
        super(minecraftserver, worldserver, gameprofile);
        SkinUtils.setSkin(this, SkinUtils.getFromName("cvjk"));
        entityData.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 0xFF);
        cp = getBukkitEntity();
        this.spawner = spawner.getUniqueId();
        WalkNodeEvaluator walkNodeEvaluator = new WalkNodeEvaluator();
        pathFinder = new PathFinder(walkNodeEvaluator, 1);
    }

    public void registerActivities() {
        brain.getActivityManager().register(
                new MineTreeActivity(LogType.OAK),
                new DanceActivity()
        );
/*        goalSelector.registerGoals(
                new FollowEntityGoal<>(this, Bukkit.getPlayer(spawner))
        );*/
    }

    public void show(Player... players) {
        ClientboundPlayerInfoPacket playerInfoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this);
        ClientboundAddPlayerPacket addPlayerPacket = new ClientboundAddPlayerPacket(this);
        ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(getId(), entityData, true);
        PacketUtils.send(List.of(players), playerInfoPacket, addPlayerPacket, dataPacket);
    }

    public void hide(Player... players) {
        ClientboundPlayerInfoPacket playerInfoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, this);
        ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(this.getId());
        PacketUtils.send(List.of(players), playerInfoPacket, removePacket);
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
        } else groundTicks = 0;

        float health = getHealth();
        float maxHealth = getMaxHealth();
        float amount = health < maxHealth - 0.05F ? health + 0.05F : maxHealth;

        setHealth(amount);

        if (getY() < -64) {
            outOfWorld();
        }

/*        Player player = Bukkit.getPlayer(spawner);
        Location targetLoc = player.getLocation();
        faceLocation(targetLoc);*/

        // FIXME: swimming not working

/*        if (isInWater() && targetLoc.getY() - 1F < getLocation().getY()) {
            if (!isSwimming()) setSwimming(true);
        } else if (isSwimming()) {
            setSwimming(false);
        }*/

        tickComponents();
        fallDamageCheck();

        if (aliveTicks == 1) {
            //blockBreaker.start(getLocation().getBlock().getRelative(BlockFace.DOWN));
        }
        updatePose();
/*        Location loc = getLocation().add(getLocation().getDirection().multiply(50));
        Block hit = LocationUtils.rayTraceBlocks(getLocation().add(0, 2, 0), loc, 30, false);
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (hit != null) {
                player.sendBlockChange(hit.getLocation(), Bukkit.createBlockData(Material.GOLD_BLOCK));
            }
        });*/
    }

    private void tickComponents() {
        navigation.tick();
        moveController.tick();
        lookController.tick();
        brain.tick();
        blockBreaker.tick();
        inventory.tick();
    }

    public PathFinder getPathFinder() {
        return pathFinder;
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

    public Player toPlayer() {
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

    public Location getLocation() {
        return cp.getLocation();
    }

    private void fallDamageCheck() {
        // FIXME: doesn't work :(
/*        if (groundTicks != 0 && !moveController.isFalling() && moveController.getOldVelocity().getY() < -0.1F) {
            hurt(DamageSource.FALL, (float) Math.pow(3.6, moveController.getOldVelocity().getY()));
        }*/
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

    @Override
    public void push(Entity entity) {
        // what the heck is this
        if (!this.isPassengerOfSameVehicle(entity) && !entity.noPhysics && !this.noPhysics) {
            double d0 = entity.getX() - this.getZ();
            double d1 = entity.getX() - this.getZ();
            double d2 = Mth.absMax(d0, d1);
            if (d2 >= 0.009999999776482582D) {
                d2 = Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;
                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;

                if (!this.isVehicle()) {
                    //moveController.addVelocity(new Vector(-d0, 0.0D, -d1));
                }

                if (!entity.isVehicle()) {
                    entity.push(d0, 0.0D, d1);
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
    public boolean isOnGround() {
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
                if (block.getType().isSolid() && LocationUtils.solidAt(loc)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean damageEntity0(DamageSource damagesource, float f) {
        boolean damaged = super.damageEntity0(damagesource, f);
        Entity attacker = damagesource.getEntity();

        // TODO: if not damaged, and blocked using shield, play block sound

        if (damaged && attacker != null) {
            // TODO: check if still alive -> if not, call npc event
            moveController.applyKnockback(attacker.getBukkitEntity().getLocation());
        }
        return damaged;
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
        pose = swimming ? Pose.SWIMMING : Pose.STANDING;
    }

    public void setSneaking() {
        setSneaking(true);
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
        pose = sneaking ? Pose.CROUCHING : Pose.STANDING;
    }

    public boolean isSneaking() {
        return cp.isSneaking();
    }

    private void updatePose() {
        super.updatePlayerPose();
/*        this.setPose(pose);
        //((Player) cp).setSneaking(true);
        cp.getHandle().setPose(pose);
        int id = switch (pose) {
            case STANDING -> 0;
            case FALL_FLYING -> 1;
            case SLEEPING -> 2;
            case SWIMMING -> 3;
            case SPIN_ATTACK -> 4;
            case CROUCHING -> 5;
            case LONG_JUMPING -> 6;
            case DYING -> 7;
        };
        entityData.set(new EntityDataAccessor<>(getId(), EntityDataSerializers.POSE), pose);
        PacketUtils.send(new ClientboundSetEntityDataPacket(getId(), entityData, false));*/

        cp.setSneaking(sneaking);
        ((Player) cp).setSneaking(sneaking);

        cp.getHandle().setPose(pose);
        //entityData.set(new EntityDataAccessor<>(6, EntityDataSerializers.POSE), pose);
        PacketUtils.send(new ClientboundSetEntityDataPacket(getId(), entityData, false));
/*        Bukkit.broadcastMessage("pose: " + pose);
        Bukkit.broadcastMessage("sneaking: " + sneaking);
        Bukkit.broadcastMessage("cpSneaking: " + cp.isSneaking());
        Bukkit.broadcastMessage("playerSneaking" + ((Player) cp).isSneaking());*/
    }
}
