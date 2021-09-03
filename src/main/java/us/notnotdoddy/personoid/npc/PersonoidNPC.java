package us.notnotdoddy.personoid.npc;

import me.definedoddy.fluidapi.FluidPlugin;
import me.definedoddy.fluidapi.tasks.DelayedTask;
import me.definedoddy.fluidapi.tasks.RepeatingTask;
import net.citizensnpcs.api.ai.flocking.Flocker;
import net.citizensnpcs.api.ai.flocking.RadiusNPCFlock;
import net.citizensnpcs.api.ai.flocking.SeparationBehavior;
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
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceManager;
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceTypes;
import us.notnotdoddy.personoid.status.Behavior;
import us.notnotdoddy.personoid.status.RemovalReason;
import us.notnotdoddy.personoid.utils.ChatMessage;
import us.notnotdoddy.personoid.utils.LocationUtilities;
import us.notnotdoddy.personoid.utils.PlayerInfo;

import java.util.*;

public class PersonoidNPC implements InventoryHolder {
    Random random = new Random();

    public NPC citizen;
    public Map<UUID, PlayerInfo> players = new HashMap<>();
    public NPCInventory inventory = new NPCInventory(this);
    public final ResourceManager resourceManager;

    public Player damagedByPlayer;
    private UUID livingEntityTarget = null;
    private boolean isWandering = false;
    public PersonoidGoal selectedGoal = null;
    private Location currentTargetLocation;
    public Player closestPlayerToNPC = null;
    public TargetHandler.TargetType activeTargetType = TargetHandler.TargetType.NOTHING;
    public boolean paused;
    private int cooldownTicks = 0;
    private final List<PersonoidGoal> allGoals = new ArrayList<>();
    private Flocker flock;
    public Location spawnLocation = null;
    private RemovalReason lastRemovalReason = null;
    private RepeatingTask repeatingTask;
    public boolean isFullyInitialized = false;
    public Behavior.Type behaviourType = Behavior.Type.BUILDER;
    private Location lastUpdatedLocation = null;
    private boolean updatedLocationThisTick = false;
    public boolean locationIsLoadedByPlayer = false;

    private boolean isInHibernationState = false;
    private Location originalLastLocation;
    private boolean isStuck = false;
    private int ticksSinceHibernation = 0;


    // Home
    Location homeLocation = null;


    public PersonoidNPC(String name) {
        citizen = PersonoidNPCHandler.registry.createNPC(EntityType.PLAYER, name);
        citizen.setProtected(false);
        citizen.getNavigator().getLocalParameters().stuckAction(null);
        citizen.getNavigator().getLocalParameters().attackRange(10);
        citizen.getNavigator().getLocalParameters().baseSpeed(1.15F);
        citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
        citizen.getNavigator().getLocalParameters().attackDelayTicks(15);
        citizen.getNavigator().getLocalParameters().useNewPathfinder(true);
        resourceManager = new ResourceManager(getPersonoid());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!players.containsKey(player.getUniqueId())) {
                players.put(player.getUniqueId(), new PlayerInfo());
            }
        }
        initGoals();
    }

    public void breakBlock(Location location){
        BlockBreaker.BlockBreakerConfiguration config = new BlockBreaker.BlockBreakerConfiguration();
        config.item(((Player) getLivingEntity()).getInventory().getItemInMainHand());
        config.radius(3);

        if (!location.getBlock().getType().isAir()){
            BlockBreaker breaker = citizen.getBlockBreaker(location.getBlock(), config);
            if (breaker.shouldExecute()) {
                pause();
                TaskRunnable run = new TaskRunnable(breaker, getPersonoid(), location);
                run.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(FluidPlugin.getPlugin(), run, 0, 1);
            }
        }
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

    public boolean isInHibernationState(){
        return isInHibernationState;
    }

    public void setInHibernationState(boolean hibernationState){
        isInHibernationState = hibernationState;
    }

    public Location getLastUpdatedLocation(){
        return lastUpdatedLocation;
    }

    public boolean hitTarget(LivingEntity livingEntity, double damage, int cooldownTicks){
        if (this.cooldownTicks == 0){
            PlayerAnimation.ARM_SWING.play((Player) getLivingEntity());
            livingEntity.damage(damage, getLivingEntity());
            this.cooldownTicks = cooldownTicks;
            return true;
        }
        return false;
    }

    public void setLivingEntityTarget(LivingEntity livingEntity){
        this.livingEntityTarget = livingEntity.getUniqueId();
    }

    public LivingEntity getLivingEntityTarget() {
        return (LivingEntity) Bukkit.getEntity(livingEntityTarget);
    }

    public void forgetCurrentTarget(){
        citizen.getNavigator().cancelNavigation();
        currentTargetLocation = null;
        livingEntityTarget = null;
        activeTargetType = TargetHandler.TargetType.NOTHING;
    }

    public void initGoals(){
        allGoals.add(new AttackMeanPlayersGoal());
        allGoals.add(new WanderRandomlyGoal());
    }

    public void sendChatMessage(String message){
        ChatMessage.send(getPersonoid(), message);
    }

    public void setCurrentTargetLocation(Location location){
        currentTargetLocation = location.clone();
    }

    public Location getCurrentTargetLocation(){
        return currentTargetLocation;
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
        this.flock = new Flocker(citizen, new RadiusNPCFlock(4.0D, 0), new SeparationBehavior(1.0D));
        spawnLocation = location.getWorld().getSpawnLocation();
        new DelayedTask(60) {
            @Override
            public void run(){
                isFullyInitialized = true;
                onInitialised();
            }
        };
        return this;
    }

    private void updateLocationOrAssumeStuck(){
        if (!updatedLocationThisTick){
            updatedLocationThisTick = true;
            lastUpdatedLocation = getLivingEntity().getLocation().clone();
        }
        else {
            isStuck = LocationUtilities.withinMargin(lastUpdatedLocation.clone(), getLivingEntity().getLocation().clone(), 0.05);
            updatedLocationThisTick = false;
        }
    }

    public void onInitialised() {
        lastUpdatedLocation = getLivingEntity().getLocation().clone();
        getPersonoid().resourceManager.attemptCraft(Material.IRON_HELMET);
    }

    public PersonoidNPC remove() {
        repeatingTask.cancel();
        PersonoidNPCHandler.getNPCs().remove(citizen);
        citizen.despawn();
        lastRemovalReason = RemovalReason.FULLY_REMOVED;
        PersonoidNPCHandler.registry.deregister(citizen);
        return this;
    }

    public PersonoidNPC pause() {
        paused = true;
        citizen.getNavigator().setPaused(true);
        return this;
    }

    public PersonoidNPC resume() {
        paused = false;
        citizen.getNavigator().setPaused(false);
        return this;
    }

    private PersonoidNPC getPersonoid(){
        return this;
    }

    // This is for goals and whatnot, prevents having to check for closest player eeeverrryy time we want to do something in relation.
    public Player getClosestPlayerToNPC(){
        return closestPlayerToNPC;
    }

    // Moved it here as I didnt see the need for the ticking to be universal.
    public void startNPCTicking() {
        repeatingTask = new RepeatingTask(0, 1) {
            @Override
            public void run() {
                resourceManager.tick();
                if (isFullyInitialized && citizen.isSpawned()) {
                    if (cooldownTicks > 0){
                        cooldownTicks--;
                    }
                    if (!paused){
                        if (citizen.getNavigator().isNavigating()){
                            flock.run();
                        }
                        closestPlayerToNPC = LocationUtilities.getClosestPlayer(getLivingEntity().getLocation());
                        updateLocationOrAssumeStuck();
                        selectGoal();
                        if (selectedGoal != null){
                            selectedGoal.tick(getPersonoid());
                            if (selectedGoal.shouldStop(getPersonoid())){
                                selectedGoal.endGoal(getPersonoid());
                                selectedGoal = null;
                                resourceManager.isPaused = false;
                            }
                        }
                        for (Map.Entry<UUID, PlayerInfo> entry : players.entrySet()) {
                            for (Behavior.Mood mood : Behavior.Mood.values()) {
                                entry.getValue().decrementMoodStrength(mood, behaviourType.retentionDecrement);
                            }
                        }
                    }
                }
                else {
                    if (isFullyInitialized){
                        if (isInHibernationState){
                            unloadedEntityTicking();
                        }
                        if (lastRemovalReason == RemovalReason.DIED) {
                            if (selectedGoal != null){
                                selectedGoal.endGoal(getPersonoid());
                                selectedGoal = null;
                            }
                        }
                        else if (lastRemovalReason == RemovalReason.FULLY_REMOVED) {
                            selectedGoal = null;
                            cancel();
                        }
                        else {
                            selectedGoal = null;
                            isInHibernationState = true;
                            originalLastLocation = lastUpdatedLocation.clone();
                        }
                        isFullyInitialized = false;
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
        ticksSinceHibernation++;
        attemptLoad();
        offsetLastLocationByTime();
    }

    private void attemptLoad(){
        if (locationIsLoadedByPlayer){
            spawn(lastUpdatedLocation);
            setInHibernationState(false);
            ticksSinceHibernation = 0;
        }
    }


    // How many blocks do player usually travel in a second? Ive got no idea, Google it is.
    // Back from Google, its 5 blocks per second at full sprint speed.
    // We'll go with three, since players usually stop and investigate something on their journey.
    private void offsetLastLocationByTime(){
        if (ticksSinceHibernation % 20 == 0){
            int randomX = random.nextInt((3));
            int randomZ = random.nextInt((3));

            if (random.nextBoolean()){
                randomX *= -1;
            }
            if (random.nextBoolean()){
                randomZ *= -1;
            }

            lastUpdatedLocation.add(randomX, 0, randomZ);
            lastUpdatedLocation.setY(lastUpdatedLocation.getWorld().getHighestBlockYAt(lastUpdatedLocation.clone()));
        }
    }

    public void selectGoal(){

        // We start from the lowest goal priority for comparison
        PersonoidGoal.GoalPriority highestPriorityFound = PersonoidGoal.GoalPriority.LOW;

        // Keep personoid goals that have matched the highest priority at the time of the check
        HashMap<PersonoidGoal.GoalPriority, PersonoidGoal> priorityWithGoal = new HashMap<>();

        // Loop through all goals and changes highest found goal prio accordingly.
        // Makes sure the goal can start first before adding it to the list of potential selected goals.
        for (PersonoidGoal goal : allGoals){
            if ( !goal.equals(selectedGoal)){
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
            if (!resourceManager.isDoingSomething || finalSelectedGoal.getGoalPriority().isHigherThan(PersonoidGoal.GoalPriority.LOW)){
                if (selectedGoal == null || finalSelectedGoal.shouldOverrideExisting()){
                    if (selectedGoal != null) {
                        selectedGoal.endGoal(getPersonoid());
                    }
                    selectedGoal = finalSelectedGoal;
                    selectedGoal.initializeGoal(getPersonoid());
                    resourceManager.isPaused = true;
                }
            }
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory.getInventory();
    }

    @NotNull
    public NPCInventory getNPCInventory() {
        return inventory;
    }
}
