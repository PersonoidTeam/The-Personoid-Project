package us.notnotdoddy.personoid.npc;

import me.definedoddy.fluidapi.FluidPlugin;
import me.definedoddy.fluidapi.tasks.DelayedTask;
import me.definedoddy.fluidapi.tasks.RepeatingTask;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import us.notnotdoddy.personoid.goals.NPCGoal;
import us.notnotdoddy.personoid.goals.defense.AttackMeanPlayersGoal;
import us.notnotdoddy.personoid.goals.movement.WanderRandomlyGoal;
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceTypes;
import us.notnotdoddy.personoid.player.PlayerInfo;
import us.notnotdoddy.personoid.status.Behavior;
import us.notnotdoddy.personoid.status.RemovalReason;
import us.notnotdoddy.personoid.utils.ChatMessage;
import us.notnotdoddy.personoid.utils.DebugMessage;
import us.notnotdoddy.personoid.utils.LocationUtilities;

import java.util.*;

public class PersonoidNPC {
    public NPC citizen;
    public NPCData data;
    public boolean initialised;
    private final Random random = new Random();
    private RepeatingTask tickingTask;
    private int groundedTicks;
    public boolean sprinting;
    public boolean jumping;
    public boolean sneaking;

    public PersonoidNPC(String name) {
        citizen = NPCHandler.registry.createNPC(EntityType.PLAYER, name);
        citizen.setProtected(false);
        citizen.getNavigator().getLocalParameters().stuckAction(null);
        citizen.getNavigator().getLocalParameters().attackRange(10);
        citizen.getNavigator().getLocalParameters().baseSpeed(1.15F);
        citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
        citizen.getNavigator().getLocalParameters().attackDelayTicks(15);
        citizen.getNavigator().getLocalParameters().useNewPathfinder(true);
        data = new NPCData(this);
        initGoals();
    }

    public void initGoals() {
        data.goals.add(new AttackMeanPlayersGoal());
        data.goals.add(new WanderRandomlyGoal());
    }

    public PersonoidNPC spawn(Location location) {
        citizen.spawn(location);
        NPCHandler.getNPCs().put(citizen, this);
        data.spawnPoint = location.getWorld().getSpawnLocation();
        new DelayedTask(60) {
            @Override
            public void run(){
                initialised = true;
                onInitialised();
            }
        };
        return this;
    }

    public void onInitialised() {
        data.lastLocation = getEntity().getLocation().clone();
        data.tickLocation = getEntity().getLocation();
        //getPlayer().setSprinting(true);
        //data.resourceManager.attemptCraft(Material.IRON_HELMET);
    }

    public void reset() {
        data.foodLevel = 7F;
        sneaking = false;
        sprinting = false;
        jumping = false;
    }

    public PersonoidNPC remove() {
        tickingTask.cancel();
        NPCHandler.getNPCs().remove(citizen);
        citizen.despawn();
        data.removalReason = RemovalReason.FULLY_REMOVED;
        NPCHandler.registry.deregister(citizen);
        return this;
    }

    public PersonoidNPC pause() {
        data.paused = true;
        citizen.getNavigator().setPaused(true);
        return this;
    }

    public PersonoidNPC resume() {
        data.paused = false;
        citizen.getNavigator().setPaused(false);
        return this;
    }

    public void startTicking() {
        tickingTask = new RepeatingTask(0, 1) {
            @Override
            public void run() {
                data.resourceManager.tick();
                if (initialised && citizen.isSpawned()) {
                    if (data.cooldownTicks > 0){
                        data.cooldownTicks--;
                    }
                    if (getNPC().data.target != null){
                        if (getNPC().data.target.getTargetType() == NPCTarget.BlockTargetType.BREAK){
                            if (getNPC().getBlockTarget().getLocation().distance(getEntity().getLocation()) < 3){
                                setToolFromBlock(getNPC().data.target.block);
                                breakBlock(getNPC().getBlockTarget().getLocation());
                            }
                        }
                    }
                    if (!data.paused){
                        if (citizen.getNavigator().isNavigating()){
                            data.flocker.run();
                        }
                        data.closestPlayer = LocationUtilities.getClosestPlayer(getEntity().getLocation()).getUniqueId();
                        updateLocationOrAssumeStuck();
                        selectGoal();
                        if (data.currentGoal != null){
                            data.currentGoal.tick(getNPC());
                            if (data.currentGoal.shouldStop(getNPC())){
                                data.currentGoal.endGoal(getNPC());
                                data.currentGoal = null;
                                data.resourceManager.isPaused = false;
                                jumping = false;
                                sneaking = false;
                                sprinting = false;
                            }
                        }
                        else {
                            data.resourceManager.isPaused = false;
                        }
                        for (Map.Entry<UUID, PlayerInfo> entry : data.players.entrySet()) {
                            for (Behavior.Mood mood : Behavior.Mood.values()) {
                                entry.getValue().decrementMoodStrength(mood, data.behavior.type().retentionDecrement);
                            }
                        }
                        if (getPlayer().isInWater() && !getPlayer().isSwimming()) {
                            getPlayer().setSwimming(true);
                        } else if (!getPlayer().isInWater() && getPlayer().isSwimming()) {
                            getPlayer().setSwimming(false);
                        }
                        if (getPlayer().isOnGround() && jumping) {
                            jump();
/*                            groundedTicks++;
                            if (groundedTicks >= 3) {
                                jump();
                                groundedTicks = 0;
                            }*/
                        }
                        if (sneaking && !getPlayer().isSneaking()) {
                            PlayerAnimation.SNEAK.play(getPlayer());
                        } else if (!sneaking && getPlayer().isSneaking()) {
                            PlayerAnimation.STOP_SNEAKING.play(getPlayer());
                        }
                        if (sneaking) {
                            getNavigator().getLocalParameters().baseSpeed(0.5F);
                        } else if (sprinting && jumping) {
                            getNavigator().getLocalParameters().baseSpeed(1.6F);
                        } else if (sprinting) {
                            getNavigator().getLocalParameters().baseSpeed(1.25F);
                        } else {
                            getNavigator().getLocalParameters().baseSpeed(1.15F);
                        }
                    }
                    data.moving = data.tickLocation.distance(getEntity().getLocation()) >= 0.00001F;
                    data.tickLocation = getEntity().getLocation();
                    if (data.moving) {
                        if (sprinting) {
                            data.saturation = Math.max(data.saturation - 0.002F, 0);
                            if (data.saturation <= 0) {
                                data.foodLevel = Math.max(data.foodLevel - 0.002F, 0);
                            }
                            if (data.foodLevel <= 6) {
                                sprinting = false;
                                jumping = false;
                            }
                        } else {
                            data.saturation = Math.max(data.saturation - 0.00035F, 0);
                            if (data.saturation <= 0) {
                                data.foodLevel = Math.max(data.foodLevel - 0.0035F, 0);
                            }
                        }
                    }
                    if (data.saturation >= 0) {
                        getEntity().setHealth(Math.min(getEntity().getHealth() + 0.001F, 20));
                        data.saturation = Math.max(data.saturation - 0.001F, 0);
                    }
                    DebugMessage.attemptMessage("food", "Saturation: " + data.saturation);
                    DebugMessage.attemptMessage("food", "Food level: " + data.foodLevel);
                    data.target.tick();
                }
                else if (initialised){
                    if (data.hibernating){
                        unloadedEntityTicking();
                    }
                    if (data.removalReason == RemovalReason.DIED) {
                        if (data.currentGoal != null){
                            data.currentGoal.endGoal(getNPC());
                            data.currentGoal = null;
                            jumping = false;
                            sneaking = false;
                            sprinting = false;
                        }
                    }
                    else if (data.removalReason == RemovalReason.FULLY_REMOVED) {
                        data.currentGoal = null;
                        cancel();
                    }
                    else {
                        data.currentGoal = null;
                        data.hibernating = true;
                        data.originalLastLocation = data.lastLocation.clone();
                    }
                    initialised = false;
                }
            }
        };
    }

    public boolean breakBlock(Location location) {
        BlockBreaker.BlockBreakerConfiguration config = new BlockBreaker.BlockBreakerConfiguration();
        config.item(((Player) getEntity()).getInventory().getItemInMainHand());
        config.radius(3);
        if (!location.getBlock().getType().isAir()) {
            BlockBreaker breaker = citizen.getBlockBreaker(location.getBlock(), config);
            if (breaker.shouldExecute()) {
                BlockBreakerTask run = new BlockBreakerTask(breaker, getNPC(), location);
                run.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(FluidPlugin.getPlugin(), run, 0, 1);
                return true;
            }
        }
        return false;
    }

    public boolean hitTarget(LivingEntity livingEntity, double damage, int cooldownTicks){
        if (data.cooldownTicks == 0){
            PlayerAnimation.ARM_SWING.play((Player) getEntity());
            livingEntity.damage(damage, getEntity());
            data.cooldownTicks = cooldownTicks;
            return true;
        }
        return false;
    }

    public void jump() {
        getPlayer().setVelocity(getPlayer().getVelocity().add(new Vector(0F, 0.4F, 0F)));
    }

    //region get/set/checks

    public LivingEntity getEntity() {
        return (LivingEntity) citizen.getEntity();
    }

    public Player getPlayer() {
        return (Player) citizen.getEntity();
    }

    public void target(NPCTarget target) {
        target.target(this);
    }

    public <T> T getTarget(Class<T> type) {
        return data.target.getTarget(type);
    }

    public boolean hasTarget() {
        return data.target != null;
    }

    public Location getLocationTarget() {
        return data.target.getTarget(Location.class);
    }

    public LivingEntity getEntityTarget() {
        return data.target.getTarget(LivingEntity.class);
    }

    public Block getBlockTarget() {
        return data.target.getTarget(Block.class);
    }

    public void forgetTarget() {
        data.target = null;
        citizen.getNavigator().cancelNavigation();
    }

    public void sendMessage(String message) {
        ChatMessage.send(getNPC(), message);
    }

    public void sendMessage(Behavior.Mood mood, String tag) {
        ChatMessage.send(getNPC(), ChatMessage.getResponse(mood, tag));
    }

    private PersonoidNPC getNPC(){
        return this;
    }

    public Player getClosestPlayer(){
        return Bukkit.getPlayer(data.closestPlayer);
    }

    public NPCInventory getInventory() {
        return data.inventory;
    }

    public Navigator getNavigator() {
        return citizen.getNavigator();
    }

    public boolean isHibernating(){
        return data.hibernating;
    }

    public void setHibernating(boolean hibernating){
        data.hibernating = hibernating;
    }

    public Location getLastLocation(){
        return data.lastLocation;
    }

    public void setItemInMainHand(ItemStack item){
        getInventory().setItemInMainHand(item);
    }

    public boolean isTarget(Player player) {
        return data.players.get(player.getUniqueId()).isTarget();
    }

    //endregion

    public void setToolFromBlock(Block block){
        if (ResourceTypes.LOG.contains(block.getType())){
            setItemInMainHand(new ItemStack(Material.DIAMOND_AXE));
        }
        if (ResourceTypes.ORES.contains(block.getType())){
            setItemInMainHand(new ItemStack(Material.DIAMOND_PICKAXE));
        }
    }

    //region hibernation

    private void updateLocationOrAssumeStuck() {
        if (!data.updatedLocationThisTick) {
            data.updatedLocationThisTick = true;
            data.lastLocation = getEntity().getLocation().clone();
        } else {
            data.stuck = LocationUtilities.withinMargin(data.lastLocation.clone(), getEntity().getLocation().clone(), 0.05);
            data.updatedLocationThisTick = false;
        }
    }

    // This is specifically for when the NPCs are not loaded by any player, but we still want them to function in some way. Be that a counter for logging
    // Back in or something else.
    private void unloadedEntityTicking(){
        data.hibernationTicks++;
        attemptLoad();
        offsetLastLocationByTime();
    }

    private void attemptLoad(){
        if (data.playerLoaded){
            spawn(data.lastLocation);
            setHibernating(false);
            data.hibernationTicks = 0;
        }
    }

    // How many blocks do player usually travel in a second? Ive got no idea, Google it is.
    // Back from Google, its 5 blocks per second at full sprint speed.
    // We'll go with three, since players usually stop and investigate something on their journey.
    private void offsetLastLocationByTime(){
        if (data.hibernationTicks % 20 == 0){
            int randomX = random.nextInt((3));
            int randomZ = random.nextInt((3));
            if (random.nextBoolean()){
                randomX *= -1;
            }
            if (random.nextBoolean()){
                randomZ *= -1;
            }
            data.lastLocation.add(randomX, 0, randomZ);
            data.lastLocation.setY(data.lastLocation.getWorld().getHighestBlockYAt(data.lastLocation.clone()));
        }
    }

    //endregion

    //region goals

    public void selectGoal(){
        // We start from the lowest goal priority for comparison
        NPCGoal.GoalPriority highestPriorityFound = NPCGoal.GoalPriority.LOWEST;
        // Keep personoid goals that have matched the highest priority at the time of the check
        HashMap<NPCGoal.GoalPriority, NPCGoal> priorityWithGoal = new HashMap<>();
        // Loop through all goals and changes highest found goal priority accordingly.
        // Makes sure the goal can start first before adding it to the list of potential selected goals.
        for (NPCGoal goal : data.goals){
            if ( !goal.equals(data.currentGoal)){
                if (goal.canStart(getNPC())){
                    if (goal.getPriority().isHigherThan(highestPriorityFound)){
                        highestPriorityFound = goal.getPriority();
                        priorityWithGoal.put(highestPriorityFound, goal);
                    }
                    else if (goal.getPriority() == highestPriorityFound){
                        priorityWithGoal.put(goal.getPriority(), goal);
                    }
                }
            }
        }
        // Final sweep.
        List<NPCGoal> finalGoals = new ArrayList<>();
        for (NPCGoal.GoalPriority goalPriority : priorityWithGoal.keySet()){
            if (goalPriority == highestPriorityFound){
                finalGoals.add(priorityWithGoal.get(goalPriority));
            }
        }
        if (!finalGoals.isEmpty()){
            NPCGoal finalSelectedGoal = finalGoals.get(random.nextInt(finalGoals.size()));
            if (!data.resourceManager.isDoingSomething || finalSelectedGoal.getPriority().isHigherThan(NPCGoal.GoalPriority.LOW)){
                if (data.currentGoal == null || finalSelectedGoal.shouldOverrideExisting()){
                    if (data.currentGoal != null) {
                        data.currentGoal.endGoal(getNPC());
                        jumping = false;
                        sneaking = false;
                        sprinting = false;
                    }
                    DebugMessage.attemptMessage("goal", "Selected a new goal!");
                    data.currentGoal = finalSelectedGoal;
                    data.currentGoal.initializeGoal(getNPC());
                    if (!data.resourceManager.isPaused){
                        data.resourceManager.isPaused = true;
                    }
                }
            }
        }
    }

    //endregion

    private static class BlockBreakerTask implements Runnable {
        private int taskId;
        private final BlockBreaker breaker;
        private final Location location;
        private final PersonoidNPC personoidNPC;
        int breakTicks = 0;

        public BlockBreakerTask(BlockBreaker breaker, PersonoidNPC personoidNPC, Location location) {
            this.location = location;
            this.breaker = breaker;
            this.personoidNPC = personoidNPC;
        }

        @Override
        public void run() {
            breakTicks++;
            if (breakTicks >= 60){
                location.getBlock().breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
                Bukkit.getScheduler().cancelTask(taskId);
            }
            else {
                PlayerAnimation.ARM_SWING.play(personoidNPC.getPlayer());
            }
        }
    }
}
