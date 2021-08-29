package us.notnotdoddy.personoid.npc;

import me.definedoddy.fluidapi.FluidMessage;
import me.definedoddy.fluidapi.tasks.RepeatingTask;
import net.citizensnpcs.api.ai.flocking.Flocker;
import net.citizensnpcs.api.ai.flocking.RadiusNPCFlock;
import net.citizensnpcs.api.ai.flocking.SeparationBehavior;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.notnotdoddy.personoid.goals.PersonoidGoal;
import us.notnotdoddy.personoid.goals.defense.AttackMeanPlayersGoal;
import us.notnotdoddy.personoid.goals.movement.WanderRandomlyGoal;
import us.notnotdoddy.personoid.status.RemovalReason;
import us.notnotdoddy.personoid.utils.ChatMessage;
import us.notnotdoddy.personoid.utils.LocationUtilities;
import us.notnotdoddy.personoid.utils.PlayerInfo;

import java.util.*;
import java.util.logging.Level;

public class PersonoidNPC {

    Random random = new Random();

    public net.citizensnpcs.api.npc.NPC citizen;
    public Map<Player, PlayerInfo> players = new HashMap<>();
    public Player damagedByPlayer;
    private UUID livingEntityTarget = null;
    private boolean isWandering = false;
    private PersonoidGoal selectedGoal = null;
    private Location currentTargetLocation;
    public Player closestPlayerToNPC = null;
    public TargetHandler.TargetType activeTargetType = TargetHandler.TargetType.NOTHING;
    public boolean paused;
    private int cooldownTicks = 0;
    private List<PersonoidGoal> allGoals = new ArrayList<>();
    private Flocker flock;
    public Location spawnLocation = null;
    private RemovalReason lastRemovalReason = null;
    private RepeatingTask repeatingTask;


    public PersonoidNPC(String name) {
        citizen = PersonoidNPCHandler.registry.createNPC(EntityType.PLAYER, name);
        citizen.setProtected(false);
        // Prevents teleportation on far targets.
        citizen.getNavigator().getLocalParameters().stuckAction(null);
        //change whatever you like here, this was mainly just for testing purposes
        citizen.getNavigator().getLocalParameters().attackRange(10);
        citizen.getNavigator().getLocalParameters().baseSpeed(1.15F);
        citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
        citizen.getNavigator().getLocalParameters().attackDelayTicks(15);
        initGoals();
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
        // Demonstration Goals
        // Simple stuff
        allGoals.add(new AttackMeanPlayersGoal());
        allGoals.add(new WanderRandomlyGoal());
    }

    public void sendChatMessage(String message){
        new ChatMessage(message, getPersonoid());
    }

    public void setCurrentTargetLocation(Location location){
        currentTargetLocation = location.clone();
    }

    public Location getCurrentTargetLocation(){
        return currentTargetLocation;
    }

    public void setMainHandItem(ItemStack item){
        getLivingEntity().getEquipment().setItemInMainHand(item);
    }


    // For when we need the living entity rather than generic, saves precious casting time haha
    public LivingEntity getLivingEntity(){
        return (LivingEntity) citizen.getEntity();
    }

    public PersonoidNPC spawn(Location location) {
        citizen.spawn(location);
        PersonoidNPCHandler.getNPCs().put(citizen, this);
        this.flock = new Flocker(citizen, new RadiusNPCFlock(4.0D, 0), new SeparationBehavior(1.0D));
        spawnLocation = location.getWorld().getSpawnLocation();
        return this;
    }

    public PersonoidNPC remove() {
        PersonoidNPCHandler.getNPCs().remove(citizen);
        citizen.despawn();
        lastRemovalReason = RemovalReason.FULLY_REMOVED;
        PersonoidNPCHandler.registry.deregister(citizen);
        repeatingTask.cancel();
        return this;
    }

    public PersonoidNPC pause() {
        paused = true;
        citizen.getNavigator().cancelNavigation();
        return this;
    }

    public PersonoidNPC resume() {
        paused = false;
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
                if (citizen.isSpawned()) {
                    if (cooldownTicks > 0){
                        cooldownTicks--;
                    }
                    if (!paused){
                        if (citizen.getNavigator().isNavigating()){
                            flock.run();
                        }
                        closestPlayerToNPC = LocationUtilities.getClosestPlayer(getLivingEntity().getLocation());
                        if (closestPlayerToNPC != null){
                            if (!players.containsKey(closestPlayerToNPC)) {
                                players.put(closestPlayerToNPC, new PlayerInfo(closestPlayerToNPC));
                            }
                        }
                        //getProperMovementAction();
                        selectGoal();
                        if (selectedGoal != null){
                            selectedGoal.tick(getPersonoid());
                            if (selectedGoal.shouldStop(getPersonoid())){
                                selectedGoal.endGoal(getPersonoid());
                                selectedGoal = null;
                            }
                        }
                    }
                }
                else {
                    if (lastRemovalReason == RemovalReason.DIED) {
                        if (selectedGoal != null){
                            selectedGoal.endGoal(getPersonoid());
                            selectedGoal = null;
                        }
                    }
                    if (lastRemovalReason == RemovalReason.FULLY_REMOVED) {
                        selectedGoal = null;
                        cancel();
                    }
                }
            }
        };
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
            if (selectedGoal == null || finalSelectedGoal.shouldOverrideExisting()){
                selectedGoal = finalSelectedGoal;
                selectedGoal.initializeGoal(getPersonoid());
            }
        }
    }

    public void getProperMovementAction(){
        /*
            Swipez Note - Switching to goals for stuff like this. Keeping this juuust incase it turns out my goal system isnt good enough, probably wont be LOL.

            if (players.get(player).mood == Behavior.Mood.ANGRY) {
                isWandering = false;
                TargetHandler.setLivingEntityTarget(getPersonoid(), player);
            }   */

        if (activeTargetType.equals(TargetHandler.TargetType.NOTHING)) {
            if (!isWandering){
                //forgetCurrentTarget();
                Bukkit.getLogger().log(Level.WARNING, "NPC set to wander.");
                TargetHandler.setNothingTarget(getPersonoid(), Bukkit.getPlayer("notnotnotswipez").getLocation());
                new FluidMessage("set nothing target", "DefineDoddy").send();
                isWandering = true;
            }
/*            else {
                if (getCurrentTargetLocation().distance(getLivingEntity().getLocation()) < 3) {
                    TargetHandler.setNothingTarget(getPersonoid(), LocationUtilities.getRandomLoc(getPersonoid()));
                    new FluidMessage("set nothing target 1", "DefineDoddy").send();
                }
                else {
                    new FluidMessage("distance >=", "DefineDoddy").send();
                }
            }*/
            new FluidMessage("nothing", "DefineDoddy").send();
        }
        else {
            isWandering = false;
            citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
            new FluidMessage("not nothing", "DefineDoddy").send();
        }
    }
}
