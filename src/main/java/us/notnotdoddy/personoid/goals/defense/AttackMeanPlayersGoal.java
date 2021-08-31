package us.notnotdoddy.personoid.goals.defense;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.notnotdoddy.personoid.goals.PersonoidGoal;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.TargetHandler;
import us.notnotdoddy.personoid.status.Behavior;

public class AttackMeanPlayersGoal extends PersonoidGoal {

    // Goal system example.
    int botRange = 10;

    public AttackMeanPlayersGoal() {
        super(true, GoalPriority.HIGH);
    }

    @Override
    public void initializeGoal(PersonoidNPC personoidNPC) {
        personoidNPC.sendChatMessage("You.... I hate you!");
        personoidNPC.setMainHandItem(new ItemStack(Material.IRON_SWORD));
        TargetHandler.setLivingEntityTarget(personoidNPC, personoidNPC.getClosestPlayerToNPC(), true);
    }

    @Override
    public void endGoal(PersonoidNPC personoidNPC) {
        personoidNPC.sendChatMessage("You live to see another day, it seems....");
        personoidNPC.setMainHandItem(null);
        personoidNPC.forgetCurrentTarget();
    }

    @Override
    public boolean canStart(PersonoidNPC personoidNPC) {
        Bukkit.broadcastMessage(personoidNPC.players.get(personoidNPC.getClosestPlayerToNPC()).getMoodValue(Behavior.Mood.ANGRY) + "");
        return personoidNPC.players.get(personoidNPC.getClosestPlayerToNPC()).isTarget() &&
                personoidNPC.getClosestPlayerToNPC().getLocation().distance(personoidNPC.getLivingEntity().getLocation()) <= botRange;
    }

    @Override
    public void tick(PersonoidNPC personoidNPC) {
        if (personoidNPC.getLivingEntityTarget().getLocation().distance(personoidNPC.getLivingEntity().getLocation()) < 3){
            personoidNPC.hitTarget(personoidNPC.getLivingEntityTarget(), 6, 20);
        }
    }

    @Override
    public boolean shouldStop(PersonoidNPC personoidNPC) {
        return personoidNPC.getLivingEntity().getLocation().distance(personoidNPC.getLivingEntityTarget().getLocation()) > (botRange+5);
    }
}
