package us.notnotdoddy.personoid.npc;

import me.definedoddy.fluidapi.FluidPlugin;
import me.definedoddy.fluidapi.tasks.DelayedTask;
import me.definedoddy.fluidapi.tasks.RepeatingTask;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.notnotdoddy.personoid.goals.PersonoidGoal;
import us.notnotdoddy.personoid.goals.defense.AttackMeanPlayersGoal;
import us.notnotdoddy.personoid.goals.movement.WanderRandomlyGoal;
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceTypes;
import us.notnotdoddy.personoid.player.NPCTarget;
import us.notnotdoddy.personoid.player.PlayerInfo;
import us.notnotdoddy.personoid.status.Behavior;
import us.notnotdoddy.personoid.status.RemovalReason;
import us.notnotdoddy.personoid.utils.ChatMessage;
import us.notnotdoddy.personoid.utils.DebugMessage;
import us.notnotdoddy.personoid.utils.LocationUtilities;

import java.util.*;

public class PersonoidNPC implements InventoryHolder {
    public PersonoidNPCData data;
    private final Random random = new Random();
    public NPC citizen;
    private RepeatingTask repeatingTask;
    public boolean initialised;


    public PersonoidNPC(String name) {
        citizen = PersonoidNPCHandler.registry.createNPC(EntityType.PLAYER, name);
        citizen.setProtected(false);
        citizen.getNavigator().getLocalParameters().stuckAction(null);
        citizen.getNavigator().getLocalParameters().attackRange(10);
        citizen.getNavigator().getLocalParameters().baseSpeed(1.15F);
        citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
        citizen.getNavigator().getLocalParameters().attackDelayTicks(15);
        citizen.getNavigator().getLocalParameters().useNewPathfinder(true);
        data = new PersonoidNPCData(this);
        data.makeResourceManager();
        initGoals();
    }

    public boolean breakBlock(Location location){
        BlockBreaker.BlockBreakerConfiguration config = new BlockBreaker.BlockBreakerConfiguration();
        config.item(((Player) getLivingEntity()).getInventory().getItemInMainHand());
        config.radius(3);
        if (!location.getBlock().getType().isAir()){
            BlockBreaker breaker = citizen.getBlockBreaker(location.getBlock(), config);
            if (breaker.shouldExecute()) {
                TaskRunnable run = new TaskRunnable(breaker, getPersonoid(), location);
                run.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(FluidPlugin.getPlugin(), run, 0, 1);
                return true;
            }
        }
        return false;
    }

    private static class TaskRunnable implements Runnable {
        private int taskId;
        private final BlockBreaker breaker;
        private final Location location;
        private final PersonoidNPC personoidNPC;

        public TaskRunnable(BlockBreaker breaker, PersonoidNPC personoidNPC ,Location location) {
            this.location = location;
            this.breaker = breaker;
            this.personoidNPC = personoidNPC;
        }

        @Override
        public void run() {
            if (breaker.run() != BehaviorStatus.RUNNING) {
                Bukkit.getScheduler().cancelTask(taskId);
                breaker.reset();
                personoidNPC.resume();
            }
        }
    }

    public boolean isHibernating(){
        return data.hibernating;
    }

    public void setHibernating(boolean hibernationState){
        data.hibernating = hibernationState;
    }

    public Location getLastLocation(){
        return data.lastLocation;
    }

    public boolean hitTarget(LivingEntity livingEntity, double damage, int cooldownTicks){
        if (data.cooldownTicks == 0){
            PlayerAnimation.ARM_SWING.play((Player) getLivingEntity());
            livingEntity.damage(damage, getLivingEntity());
            data.cooldownTicks = cooldownTicks;
            return true;
        }
        return false;
    }

    public void setEntityTarget(LivingEntity entity){
        data.target = new NPCTarget(entity);
    }

    public LivingEntity getEntityTarget() {
        return data.target.getTarget(LivingEntity.class);
    }

    public void forgetCurrentTarget(){
        citizen.getNavigator().cancelNavigation();
        data.targetLocation = null;
        data.target = null;
        data.targetType = TargetHandler.TargetType.NOTHING;
    }

    public void initGoals(){
        data.goals.add(new AttackMeanPlayersGoal());
        data.goals.add(new WanderRandomlyGoal());
    }

    public void sendChatMessage(String message){
        ChatMessage.send(getPersonoid(), message);
    }

    public void setTargetLocation(Location location){
        data.targetLocation = location.clone();
    }

    public Location getTargetLocation(){
        return data.targetLocation;
    }

    public void setMainHandItem(ItemStack item){
        getNPCInventory().setItemInMainHand(item);
    }

    // For when we need the living entity rather than generic, saves precious casting time haha
    public LivingEntity getLivingEntity() {
        return (LivingEntity) citizen.getEntity();
    }

    public Player getPlayer() {
        return (Player) citizen.getEntity();
    }

    public PersonoidNPC spawn(Location location) {
        citizen.spawn(location);
        PersonoidNPCHandler.getNPCs().put(citizen, this);
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

    private void updateLocationOrAssumeStuck(){
        if (!data.updatedLocationThisTick){
            data.updatedLocationThisTick = true;
            data.lastLocation = getLivingEntity().getLocation().clone();
        }
        else {
            data.stuck = LocationUtilities.withinMargin(data.lastLocation.clone(), getLivingEntity().getLocation().clone(), 0.05);
            data.updatedLocationThisTick = false;
        }
    }

    public void onInitialised() {
        data.lastLocation = getLivingEntity().getLocation().clone();
        //getPersonoid().resourceManager.attemptCraft(Material.CRAFTING_TABLE);
    }

    public PersonoidNPC remove() {
        repeatingTask.cancel();
        PersonoidNPCHandler.getNPCs().remove(citizen);
        citizen.despawn();
        data.removalReason = RemovalReason.FULLY_REMOVED;
        PersonoidNPCHandler.registry.deregister(citizen);
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

    private PersonoidNPC getPersonoid(){
        return this;
    }

    // This is for goals and whatnot, prevents having to check for closest player eeeverrryy time we want to do something in relation.
    public Player getClosestPlayer(){
        return data.getClosestPlayer();
    }

    // Moved it here as I didnt see the need for the ticking to be universal.
    public void startNPCTicking() {
        repeatingTask = new RepeatingTask(0, 1) {
            @Override
            public void run() {
                data.resourceManager.tick();
                if (initialised && citizen.isSpawned()) {
                    if (data.cooldownTicks > 0){
                        data.cooldownTicks--;
                    }
                    if (!data.paused){
                        if (citizen.getNavigator().isNavigating()){
                            data.flocker.run();
                        }
                        data.closestPlayer = LocationUtilities.getClosestPlayer(getLivingEntity().getLocation()).getUniqueId();
                        updateLocationOrAssumeStuck();
                        selectGoal();
                        if (data.currentGoal != null){
                            data.currentGoal.tick(getPersonoid());
                            if (data.currentGoal.shouldStop(getPersonoid())){
                                data.currentGoal.endGoal(getPersonoid());
                                data.currentGoal = null;
                                data.resourceManager.isPaused = false;
                            }
                        }
                        for (Map.Entry<UUID, PlayerInfo> entry : data.players.entrySet()) {
                            for (Behavior.Mood mood : Behavior.Mood.values()) {
                                entry.getValue().decrementMoodStrength(mood, data.behavior.getType().retentionDecrement);
                            }
                        }
                    }
                }
                else {
                    if (initialised){
                        if (data.hibernating){
                            unloadedEntityTicking();
                        }
                        if (data.removalReason == RemovalReason.DIED) {
                            if (data.currentGoal != null){
                                data.currentGoal.endGoal(getPersonoid());
                                data.currentGoal = null;
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
            }
        };
    }

    public void getProperMiningTool(Block block){
        if (ResourceTypes.LOG.contains(block.getType())){
            setMainHandItem(new ItemStack(Material.DIAMOND_AXE));
        }
        if (ResourceTypes.ORES.contains(block.getType())){
            setMainHandItem(new ItemStack(Material.DIAMOND_PICKAXE));
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

    public void selectGoal(){

        // We start from the lowest goal priority for comparison
        PersonoidGoal.GoalPriority highestPriorityFound = PersonoidGoal.GoalPriority.LOW;

        // Keep personoid goals that have matched the highest priority at the time of the check
        HashMap<PersonoidGoal.GoalPriority, PersonoidGoal> priorityWithGoal = new HashMap<>();

        // Loop through all goals and changes highest found goal prio accordingly.
        // Makes sure the goal can start first before adding it to the list of potential selected goals.
        for (PersonoidGoal goal : data.goals){
            if ( !goal.equals(data.currentGoal)){
                if (goal.canStart(getPersonoid())){
                    if (goal.getGoalPriority().isHigherThan(highestPriorityFound)){
                        highestPriorityFound = goal.getGoalPriority();
                        priorityWithGoal.put(highestPriorityFound, goal);
                    }
                    else if (goal.getGoalPriority() == highestPriorityFound){
                        priorityWithGoal.put(goal.getGoalPriority(), goal);
                    }
                }
            }
        }

        // Final sweep.
        List<PersonoidGoal> finalGoals = new ArrayList<>();
        for (PersonoidGoal.GoalPriority goalPriority : priorityWithGoal.keySet()){
            if (goalPriority == highestPriorityFound){
                finalGoals.add(priorityWithGoal.get(goalPriority));
            }
        }

        if (!finalGoals.isEmpty()){
            PersonoidGoal finalSelectedGoal = finalGoals.get(random.nextInt(finalGoals.size()));
            if (!data.resourceManager.isDoingSomething || finalSelectedGoal.getGoalPriority().isHigherThan(PersonoidGoal.GoalPriority.LOW)){
                if (data.currentGoal == null || finalSelectedGoal.shouldOverrideExisting()){
                    if (data.currentGoal != null) {
                        data.currentGoal.endGoal(getPersonoid());
                    }
                    DebugMessage.attemptMessage("Selected a new goal!");
                    data.currentGoal = finalSelectedGoal;
                    data.currentGoal.initializeGoal(getPersonoid());
                    data.resourceManager.isPaused = true;
                }
            }
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return data.inventory.getInventory();
    }

    @NotNull
    public PersonoidNPCInventory getNPCInventory() {
        return data.inventory;
    }
}
