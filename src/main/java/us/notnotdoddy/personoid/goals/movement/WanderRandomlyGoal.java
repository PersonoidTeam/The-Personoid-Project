package us.notnotdoddy.personoid.goals.movement;

import org.bukkit.Location;
import us.notnotdoddy.personoid.goals.NPCGoal;
import us.notnotdoddy.personoid.npc.NPCTarget;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.utils.LocationUtilities;

public class WanderRandomlyGoal extends NPCGoal {
    private int failSafeTicks;

    public WanderRandomlyGoal() {
        super(false, GoalPriority.LOW);
    }


    @Override
    public void initializeGoal(PersonoidNPC npc) {
        npc.target(new NPCTarget(LocationUtilities.getRandomLoc(npc)));
    }

    @Override
    public void endGoal(PersonoidNPC personoidNPC) {
        personoidNPC.forgetTarget();
        personoidNPC.citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
    }

    @Override
    public boolean canStart(PersonoidNPC personoidNPC) {
        return true;
    }

    @Override
    public void tick(PersonoidNPC npc) {
        failSafeTicks++;
        if (failSafeTicks == 40){
            npc.target(new NPCTarget(LocationUtilities.getRandomLoc(npc)));
            failSafeTicks = 0;
        }
        if (npc.hasTarget()) {
            if (npc.data.target.getTarget(Location.class).distance(npc.getEntity().getLocation()) < 3) {
                npc.target(new NPCTarget(LocationUtilities.getRandomLoc(npc)));
                failSafeTicks = 0;
            }
        }
    }

    @Override
    public boolean shouldStop(PersonoidNPC personoidNPC) {
        return false;
    }
}
