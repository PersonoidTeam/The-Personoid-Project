package us.notnotdoddy.personoid.goals.movement;

import org.bukkit.Location;
import us.notnotdoddy.personoid.goals.PersonoidGoal;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.utils.LocationUtilities;

public class WanderRandomlyGoal extends PersonoidGoal {
    public WanderRandomlyGoal() {
        super(false, GoalPriority.LOW);
    }

    @Override
    public void initializeGoal(PersonoidNPC personoidNPC) {
        personoidNPC.sendChatMessage("Im wandering!");
        Location chosenDestination = LocationUtilities.getRandomLoc(personoidNPC);
        personoidNPC.citizen.getNavigator().setTarget(chosenDestination);
        personoidNPC.setCurrentTargetLocation(chosenDestination);
    }

    @Override
    public void endGoal(PersonoidNPC personoidNPC) {

    }

    @Override
    public boolean canStart(PersonoidNPC personoidNPC) {
        return true;
    }

    @Override
    public void tick(PersonoidNPC personoidNPC) {
        if (personoidNPC.getCurrentTargetLocation().distance(personoidNPC.getLivingEntity().getLocation()) < 3){
            //TargetHandler.setNothingTarget(personoidNPC, LocationUtilities.getRandomLoc(personoidNPC));
            Location chosenDestination = LocationUtilities.getRandomLoc(personoidNPC);
            personoidNPC.citizen.getNavigator().setTarget(chosenDestination);
            personoidNPC.setCurrentTargetLocation(chosenDestination);
        }
    }

    @Override
    public boolean shouldStop(PersonoidNPC personoidNPC) {
        return false;
    }
}
