package us.notnotdoddy.personoid.goals.movement;

import us.notnotdoddy.personoid.goals.PersonoidGoal;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.TargetHandler;
import us.notnotdoddy.personoid.utils.LocationUtilities;

public class WanderRandomlyGoal extends PersonoidGoal {
    public WanderRandomlyGoal() {
        super(false, GoalPriority.LOW);
    }

    int failSafeTicks = 0;

    @Override
    public void initializeGoal(PersonoidNPC personoidNPC) {
        TargetHandler.setNothingTarget(personoidNPC, LocationUtilities.getRandomLoc(personoidNPC));
        personoidNPC.citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(0);
    }

    @Override
    public void endGoal(PersonoidNPC personoidNPC) {
        personoidNPC.forgetCurrentTarget();
        personoidNPC.citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
    }

    @Override
    public boolean canStart(PersonoidNPC personoidNPC) {
        return true;
    }

    @Override
    public void tick(PersonoidNPC personoidNPC) {
        failSafeTicks++;
        if (failSafeTicks == 40){
            TargetHandler.setNothingTarget(personoidNPC, LocationUtilities.getRandomLoc(personoidNPC));
            failSafeTicks = 0;
        }
        if (personoidNPC.getTargetLocation().distance(personoidNPC.getLivingEntity().getLocation()) < 3) {
            TargetHandler.setNothingTarget(personoidNPC, LocationUtilities.getRandomLoc(personoidNPC));
            failSafeTicks = 0;
        }

    }

    @Override
    public boolean shouldStop(PersonoidNPC personoidNPC) {
        return false;
    }
}
